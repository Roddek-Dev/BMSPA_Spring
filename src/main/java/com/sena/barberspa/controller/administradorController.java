package com.sena.barberspa.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
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

import com.sena.barberspa.model.Orden;
import com.sena.barberspa.model.Producto;
import com.sena.barberspa.model.Servicio;
import com.sena.barberspa.model.Sucursal;
import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.service.IOrdenService;
import com.sena.barberspa.service.IProductoService;
import com.sena.barberspa.service.IServiciosService;
import com.sena.barberspa.service.ISucursalesService;
import com.sena.barberspa.service.IUsuarioService;
import com.sena.barberspa.service.UploadFileService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/administrador")
public class administradorController {

	// Instancia de LOGGER para ver datos por consola
	private final Logger LOGGER = (Logger) LoggerFactory.getLogger(administradorController.class);

	@Autowired
	private IProductoService productoService;

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IOrdenService ordenService;

	@Autowired
	private IServiciosService servicioService;

	@Autowired
	private ISucursalesService sucursalService;

	// microservicio de imagenes
	@Autowired
	private UploadFileService upload;

	// Método para agregar el usuario a todos los modelos
	@ModelAttribute
	public void addUsuarioToModel(Model model, HttpSession session) {
		Integer idUsuario = (Integer) session.getAttribute("idUsuario");
		if (idUsuario != null) {
			Usuario usuario = usuarioService.findById(idUsuario).orElse(null);
			if (usuario != null) {
				model.addAttribute("usuario", usuario);
			}
		}
	}

	@GetMapping("")
	public String home(Model model) {
		List<Producto> productos = productoService.findAll();
		model.addAttribute("productos", productos);
		List<Servicio> servicios = servicioService.findAll();
		model.addAttribute("servicios", servicios);
		List<Sucursal> sucursales = sucursalService.findAll();
		model.addAttribute("sucursales", sucursales);

		return "administrador/home";

	}

	@GetMapping("/ordenes")
	public String ordenes(Model model) {
		model.addAttribute("ordenes", ordenService.findAll());
		return "administrador/ordenes";

	}

	// metodo para llenar los inputs para llenar la lista edit
	@GetMapping("/profile")
	public String editProfile(Model model, HttpSession session) {
		Integer idUsuario = (Integer) session.getAttribute("idUsuario");
		Usuario usuario = usuarioService.findById(idUsuario).get(); // Se asume que siempre existe
		model.addAttribute("usuario", usuario);
		return "administrador/profile";
	}

	@PostMapping("/updateProfile")
	public String updateProfile(HttpSession session, @RequestParam("nombre") String nombre,
			@RequestParam("email") String email, @RequestParam("telefono") String telefono,
			@RequestParam("direccion") String direccion,
			@RequestParam(value = "img", required = false) MultipartFile file) throws IOException {

		Integer idUsuario = (Integer) session.getAttribute("idUsuario");
		Usuario u = usuarioService.findById(idUsuario).get();
		LOGGER.info("Actualizando perfil del usuario con ID {}", idUsuario);

		// Actualizar datos del usuario
		u.setNombre(nombre);
		u.setEmail(email);
		u.setTelefono(telefono);
		u.setDireccion(direccion);

		// Manejo de imagen
		if (file != null && !file.isEmpty()) {
			if (!"default.jpg".equals(u.getImagen())) {
				upload.deleteImage(u.getImagen());
			}
			String nombreImagen = upload.saveImages(file, u.getNombre());
			u.setImagen(nombreImagen);
		}

		// Guardar cambios
		usuarioService.update(u);
		LOGGER.info("Perfil actualizado correctamente para el usuario {}", nombre);

		return "redirect:/administrador/profile";

	}

	@GetMapping("/detalle")
	public String detalleOrden(@PathVariable Integer id, Model model) {
		LOGGER.info("id de la orden: {}", id);
		Optional<Orden> orden = ordenService.findById(id);
		model.addAttribute("detalles", orden.get().getDetalle());
		return "administrador/detalleorden";

	}

	// metodo post para buscar en la vista del home de usuarios
	@PostMapping("/searchA")
	public String searchProducto(@RequestParam String busqueda, Model model) {
		LOGGER.info("nombre de la busqueda: {}", busqueda);
		List<Producto> productos = productoService.findAll().stream()
				.filter(p -> p.getNombreproducto().toUpperCase().contains(busqueda.toUpperCase()))
				.collect(Collectors.toList());
		List<Sucursal> sucursales = sucursalService.findAll().stream()
				.filter(s -> s.getNombre().toUpperCase().contains(busqueda.toUpperCase())).collect(Collectors.toList());
		List<Servicio> servicios = servicioService.findAll().stream()
				.filter(su -> su.getNombre().toUpperCase().contains(busqueda.toUpperCase()))
				.collect(Collectors.toList());
		model.addAttribute("productos", productos);
		model.addAttribute("sucursales", sucursales);
		model.addAttribute("servicios", servicios);
		return "administrador/home";
	}

	// metodo detelles de una orden
	@GetMapping("/detalle/{id}")
	public String detalleorden(@PathVariable Integer id, Model model) {
		LOGGER.info("id de la orden: {}", id);
		Optional<Orden> orden = ordenService.findById(id);
		model.addAttribute("detalles", orden.get().getDetalle());
		return "administrador/detalleorden";

	}
	// Agrega este método al controlador administradorController.java
	@GetMapping("/nuevo")
	public String nuevoAdmin() {
		return "redirect:/admin/usuarios";
	}
}