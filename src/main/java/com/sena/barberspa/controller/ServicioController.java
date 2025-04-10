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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sena.barberspa.model.Servicio;
import com.sena.barberspa.model.Sucursal;
import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.service.IServiciosService;
import com.sena.barberspa.service.ISucursalesService;
import com.sena.barberspa.service.IUsuarioService;
import com.sena.barberspa.service.UploadFileService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/servicios")
public class ServicioController {

	private final Logger LOGGER = LoggerFactory.getLogger(ServicioController.class);

	@Autowired
	private ISucursalesService sucursalService;

	@Autowired
	private IServiciosService servicioService;

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private UploadFileService upload;

	@GetMapping("")
	public String show(Model model) {
		model.addAttribute("servicios", servicioService.findAll());
		return "servicios/show";
	}

	// Método para agregar el usuario a todos los modelos
	@ModelAttribute
	public void addUsuarioToModel(Model model, HttpSession session) {
		Integer idUsuario = (Integer) session.getAttribute("idUsuario");
		if (idUsuario != null) {
			Usuario usuario = usuarioService.findById(idUsuario).orElse(null);
			if (usuario != null) {
				model.addAttribute("sesion", usuario);
				model.addAttribute("usuario", usuario);

			}
		}
	}

	@GetMapping("/create")
	public String create(Model model) {
		model.addAttribute("servicio", new Servicio()); // Añade esta línea clave
		return "servicios/create";
	}

	@PostMapping("/save")
	public String save(@Valid Servicio servicio, BindingResult result, @RequestParam("img") MultipartFile file,
					   HttpSession session, RedirectAttributes redirectAttributes) throws IOException {

		if (result.hasErrors()) {
			return "servicios/create";
		}

		LOGGER.info("Guardando servicio: {}", servicio);
		Usuario u = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString())).get();
		servicio.setUsuario(u);

		// Manejo de la imagen
		if (!file.isEmpty()) {
			String nombreImagen = upload.saveImages(file, servicio.getNombre());
			servicio.setImagen(nombreImagen);
		} else {
			servicio.setImagen("default.jpg");
		}

		servicioService.save(servicio);
		redirectAttributes.addFlashAttribute("success", "Servicio creado exitosamente");
		return "redirect:/servicios";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Integer id, Model model) {
		Optional<Servicio> optionalServicio = servicioService.get(id);

		if (optionalServicio.isEmpty()) {
			return "redirect:/servicios";
		}

		model.addAttribute("servicio", optionalServicio.get());
		return "servicios/edit";
	}

	@PostMapping("/update")
	public String update(@Valid Servicio servicio, BindingResult result, @RequestParam("img") MultipartFile file,
						 RedirectAttributes redirectAttributes) throws IOException {

		if (result.hasErrors()) {
			return "servicios/edit";
		}

		LOGGER.info("Actualizando servicio: {}", servicio);
		Servicio existingServicio = servicioService.get(servicio.getId()).orElse(null);

		if (existingServicio == null) {
			return "redirect:/servicios";
		}

		// Manejo de la imagen
		if (file.isEmpty()) {
			servicio.setImagen(existingServicio.getImagen());
		} else {
			if (!existingServicio.getImagen().equals("default.jpg")) {
				upload.deleteImage(existingServicio.getImagen());
			}
			String nombreImagen = upload.saveImages(file, servicio.getNombre());
			servicio.setImagen(nombreImagen);
		}

		servicio.setUsuario(existingServicio.getUsuario());
		servicioService.update(servicio);
		redirectAttributes.addFlashAttribute("success", "Servicio actualizado exitosamente");
		return "redirect:/servicios";
	}

	@GetMapping("/delete/{id}")
	public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		Optional<Servicio> optionalServicio = servicioService.get(id);

		if (optionalServicio.isPresent()) {
			Servicio servicio = optionalServicio.get();

			if (!servicio.getImagen().equals("default.jpg")) {
				upload.deleteImage(servicio.getImagen());
			}

			servicioService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Servicio eliminado exitosamente");
		} else {
			redirectAttributes.addFlashAttribute("error", "No se encontró el servicio");
		}

		return "redirect:/servicios";
	}

	@PostMapping("/search")
	public String searchServicio(@RequestParam String nombre, Model model) {
		LOGGER.info("Buscando servicio: {}", nombre);
		List<Servicio> servicios = servicioService.findAll().stream()
				.filter(p -> p.getNombre().toUpperCase().contains(nombre.toUpperCase())).collect(Collectors.toList());
		model.addAttribute("servicios", servicios);
		return "servicios/show";
	}

	@GetMapping("/servicioHome/{id}")
	public String servicioHome(@PathVariable Integer id, Model model) {
		// Obtener el servicio
		Servicio servicio = servicioService.get(id).orElseThrow();

		// Obtener sucursales desde la base de datos
		List<Sucursal> sucursales = sucursalService.findAll();

		// Verificación en consola (eliminar después)
		System.out.println("Número de sucursales encontradas: " + sucursales.size());
		sucursales.forEach(s -> System.out.println(s.getNombre()));

		// Pasar datos al modelo
		model.addAttribute("servicio", servicio);
		model.addAttribute("sucursales", sucursales); // Esto es crucial
		model.addAttribute("servicios", servicioService.findAll());

		return "usuario/servicioHome";
	}
//
//	@GetMapping("/sucursales/json")
//	@ResponseBody
//	public List<Sucursal> getSucursalesJson() {
//	    return sucursalService.findAll();
//	}
}