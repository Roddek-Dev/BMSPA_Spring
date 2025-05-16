package com.sena.barberspa.controller;

import java.io.IOException;
import java.util.Collections; // Necesario para listas vacías
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sena.barberspa.model.*;
import com.sena.barberspa.service.*;
import org.slf4j.Logger; // Asegúrate que sea org.slf4j.Logger
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Importante

@Controller
@RequestMapping("/administrador") // Ruta base para todos los métodos aquí
public class administradorController {

    // Hacer el LOGGER final es una buena práctica
    private final Logger LOGGER = LoggerFactory.getLogger(administradorController.class);

    // --- Inyección de Dependencias (Considera usar inyección por constructor) ---
    @Autowired
    private IProductoService productoService;
    @Autowired
    private IUsuarioService usuarioService;
    @Autowired
    private IOrdenService ordenService;
    @Autowired
    private IServiciosService servicioService; // Asegúrate que el nombre coincida (IServiciosService vs IServicioService)
    @Autowired
    private ISucursalesService sucursalService; // Asegúrate que el nombre coincida
    @Autowired
    private UploadFileService upload;
    @Autowired
    private IRecordatorioService recordatorioService;

    // --- Atributos Globales para el Modelo ---
    // Se ejecuta ANTES de cada método @GetMapping/@PostMapping en este controlador
    @ModelAttribute
    public void addGlobalAttributesToModel(Model model, HttpSession session) {
        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        LOGGER.debug("Intentando añadir atributos globales para idUsuario en sesión: {}", idUsuario);
        if (idUsuario != null) {
            Usuario usuario = usuarioService.findById(idUsuario).orElse(null);
            if (usuario != null) {
                model.addAttribute("usuario", usuario);
                // Procesar agendamientos próximos y convertirlos en recordatorios
                try {
                    recordatorioService.procesarAgendamientosProximos(usuario, 3); // El '3' podría ser configurable
                } catch (Exception e) {
                    LOGGER.error("Error procesando agendamientos próximos para usuario {}", idUsuario, e);
                    // Opcional: Añadir un mensaje de error al modelo
                }

                // Obtener recordatorios para mostrar en la barra lateral
                List<Recordatorio> recordatorios = recordatorioService.findByUsuario(usuario);
                model.addAttribute("recordatorios", recordatorios);
                LOGGER.debug("Añadidos {} recordatorios para usuario {}", recordatorios.size(), idUsuario);

                // Contar las órdenes utilizando el servicio
                long numeroDeOrdenes = ordenService.countAll();
                model.addAttribute("totalOrdenes", numeroDeOrdenes); // Añadir el conteo al modelo
                LOGGER.debug("Añadido conteo total de órdenes: {}", numeroDeOrdenes);

            } else {
                LOGGER.warn("Usuario no encontrado para el idUsuario {} en sesión.", idUsuario);
            }
        } else {
            LOGGER.warn("No se encontró 'idUsuario' en la sesión.");
        }
    }

    // --- Método GET para el Home del Admin / Resultados de Búsqueda ---
    // Mapeado a "/administrador" (debido al @RequestMapping a nivel de clase)
    @GetMapping("") // Este método ahora maneja tanto la carga inicial como los resultados post-búsqueda
    public String showAdminHome(
            @ModelAttribute("searchTerm") String busqueda, // Recibe el término del flash attribute (si existe)
            Model model) { // La sesión y atributos globales se manejan en addGlobalAttributesToModel

        LOGGER.info("GET /administrador - Cargando dashboard. Término de búsqueda recibido: '{}'", busqueda);

        List<Producto> productos;
        List<Servicio> servicios;
        List<Sucursal> sucursales;

        // *** INICIO LÓGICA DE BÚSQUEDA CONDICIONAL ***
        // Realiza la búsqueda AQUÍ, solo si 'busqueda' no es nulo/vacío (viene del redirect)
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            LOGGER.info("Realizando búsqueda filtrada para: {}", busqueda);
            String upperBusqueda = busqueda.toUpperCase(); // Convertir a mayúsculas una vez

            // Filtrar las listas
            productos = productoService.findAll().stream()
                    .filter(p -> p.getNombreproducto().toUpperCase().contains(upperBusqueda))
                    .collect(Collectors.toList());
            servicios = servicioService.findAll().stream()
                    .filter(s -> s.getNombre().toUpperCase().contains(upperBusqueda)) // Asume método getNombre()
                    .collect(Collectors.toList());
            sucursales = sucursalService.findAll().stream()
                    .filter(suc -> suc.getNombre().toUpperCase().contains(upperBusqueda)) // Asume método getNombre()
                    .collect(Collectors.toList());

            // IMPORTANTE: Añade el término de búsqueda de nuevo al modelo
            // para que el input en la vista muestre lo que se buscó.
            model.addAttribute("busqueda", busqueda);
            LOGGER.info("Resultados filtrados - Productos: {}, Servicios: {}, Sucursales: {}", productos.size(), servicios.size(), sucursales.size());

        } else {
            LOGGER.info("No se proporcionó término de búsqueda, cargando todos los items.");
            // Si no hay término de búsqueda (carga inicial), carga todo
            productos = productoService.findAll();
            servicios = servicioService.findAll();
            sucursales = sucursalService.findAll();
        }
        // *** FIN LÓGICA DE BÚSQUEDA CONDICIONAL ***

        // Añade las listas (filtradas o completas) al modelo
        model.addAttribute("productos", productos);
        model.addAttribute("servicios", servicios);
        model.addAttribute("sucursales", sucursales);

        // Los atributos globales (usuario, recordatorios, totalOrdenes) ya fueron añadidos por addGlobalAttributesToModel
        return "administrador/home"; // Devuelve el nombre de la vista
    }


    // --- Método POST para la Búsqueda (Implementa PRG) ---
    // Mapeado a "/administrador/searchA"
    @PostMapping("/searchA")
    public String handleSearchPost(@RequestParam String busqueda, RedirectAttributes redirectAttributes) {
        LOGGER.info("POST /administrador/searchA - Recibida búsqueda para: '{}'", busqueda);

        // 1. Pasa el término de búsqueda como Flash Attribute al método GET.
        redirectAttributes.addFlashAttribute("searchTerm", busqueda);

        // 2. Redirige al método GET que está mapeado a "/administrador" (el método showAdminHome).
        //    La redirección limpia la URL en el navegador a ".../administrador".
        return "redirect:/administrador"; // Correcto: redirige a la raíz del controlador
    }


    // --- RESTO DE MÉTODOS (sin cambios relevantes para el problema de búsqueda) ---

    @GetMapping("/ordenes")
    public String ordenes(Model model) {
        model.addAttribute("ordenes", ordenService.findAll());
        return "administrador/ordenes";
    }

    @GetMapping("/profile")
    public String editProfile(Model model, HttpSession session) {
        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        if (idUsuario == null) {
            LOGGER.warn("Acceso a perfil denegado - idUsuario no encontrado en sesión.");
            return "redirect:/login"; // O una página de error/acceso denegado
        }
        // Es más seguro usar orElseThrow para manejar el caso de usuario no encontrado
        Usuario usuario = usuarioService.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + idUsuario));
        model.addAttribute("usuario", usuario); // Asegura que el usuario esté explícitamente para esta vista
        return "administrador/profile";
    }

    @PostMapping("/updateProfile")
    public String updateProfile(HttpSession session,
                                @RequestParam("nombre") String nombre,
                                @RequestParam("email") String email,
                                @RequestParam("telefono") String telefono,
                                @RequestParam("direccion") String direccion,
                                @RequestParam(value = "img", required = false) MultipartFile file) throws IOException {

        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        if (idUsuario == null) {
            LOGGER.warn("Actualización de perfil fallida - idUsuario no encontrado en sesión.");
            return "redirect:/login";
        }

        Usuario u = usuarioService.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + idUsuario));

        LOGGER.info("Actualizando perfil del usuario con ID {}", idUsuario);

        // Actualizar datos básicos
        u.setNombre(nombre);
        u.setEmail(email);
        u.setTelefono(telefono);
        u.setDireccion(direccion);

        // Manejo de imagen
        if (file != null && !file.isEmpty()) {
            // Validar tipo de archivo
            if (!file.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("Solo se permiten archivos de imagen");
            }

            String oldImage = u.getImagen();
            String nombreImagen = upload.saveImages(file, u.getNombre());
            u.setImagen(nombreImagen);

            // Borrar imagen antigua si existe y no es la predeterminada
            if (oldImage != null && !oldImage.isEmpty() && !"default.jpg".equals(oldImage)) {
                upload.deleteImage(oldImage);
            }
        }

        usuarioService.update(u);
        LOGGER.info("Perfil actualizado correctamente para el usuario {}", nombre);

        return "redirect:/administrador/profile";
    }


    // metodo detalles de una orden (Combinado)
    @GetMapping("/detalle/{id}")
    public String detalleorden(@PathVariable Integer id, Model model) {
        LOGGER.info("GET /administrador/detalle/{} - Solicitando detalles de orden", id);
        Optional<Orden> ordenOpt = ordenService.findById(id);
        if (ordenOpt.isPresent()) {
            Orden orden = ordenOpt.get();
            model.addAttribute("orden", orden); // Puedes pasar la orden completa si la necesitas
            model.addAttribute("detalles", orden.getDetalle());
            return "administrador/detalleorden";
        } else {
            LOGGER.warn("Orden con ID {} no encontrada.", id);
            return "redirect:/administrador/ordenes"; // Redirige a la lista de órdenes si no se encuentra
        }
    }

    // Este método redirige fuera del contexto /administrador
    @GetMapping("/nuevo")
    public String nuevoAdmin() {
        LOGGER.info("Redirigiendo desde /administrador/nuevo hacia /admin/usuarios");
        return "redirect:/admin/usuarios"; // Asegúrate que esta ruta sea manejada por otro controlador
    }
}