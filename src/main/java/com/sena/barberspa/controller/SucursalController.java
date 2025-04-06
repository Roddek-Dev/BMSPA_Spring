package com.sena.barberspa.controller;

import java.io.IOException;
import java.util.Optional;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sena.barberspa.model.Sucursal;
import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.service.ISucursalesService;
import com.sena.barberspa.service.IUsuarioService;
import com.sena.barberspa.service.UploadFileService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/sucursales")
public class SucursalController {

	private final Logger LOGGER = LoggerFactory.getLogger(SucursalController.class);

	@Autowired
	private ISucursalesService sucursalesService;

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private UploadFileService upload;

	@GetMapping("")
	public String show(Model model) {
		model.addAttribute("sucursales", sucursalesService.findAll());
		return "sucursales/show";
	}

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

	@GetMapping("/create")
	public String create(Model model) {
		model.addAttribute("sucursal", new Sucursal()); // Objeto para el formulario
		return "sucursales/create";
	}

	@PostMapping("/save")
	public String save(@Valid Sucursal sucursal, BindingResult result, @RequestParam("img") MultipartFile file,
			RedirectAttributes redirectAttributes) throws IOException {

		if (result.hasErrors()) {
			return "sucursales/create";
		}

		LOGGER.info("Guardando sucursal: {}", sucursal);

		// Manejo de la imagen
		if (!file.isEmpty()) {
			String nombreImagen = upload.saveImages(file, sucursal.getNombre());
			sucursal.setImagen(nombreImagen);
		} else {
			sucursal.setImagen("default.jpg");
		}

		sucursalesService.save(sucursal);
		redirectAttributes.addFlashAttribute("success", "Sucursal creada exitosamente");
		return "redirect:/sucursales";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Integer id, Model model) {
		Optional<Sucursal> optionalSucursal = sucursalesService.get(id);

		if (optionalSucursal.isEmpty()) {
			return "redirect:/sucursales";
		}

		model.addAttribute("sucursal", optionalSucursal.get());
		return "sucursales/edit";
	}

	@PostMapping("/update")
	public String update(@Valid Sucursal sucursal, BindingResult result, @RequestParam("img") MultipartFile file,
			RedirectAttributes redirectAttributes) throws IOException {

		if (result.hasErrors()) {
			return "sucursales/edit";
		}

		LOGGER.info("Actualizando sucursal: {}", sucursal);

		Sucursal existingSucursal = sucursalesService.get(sucursal.getId()).orElse(null);

		if (existingSucursal == null) {
			return "redirect:/sucursales";
		}

		// Manejo de la imagen
		if (file.isEmpty()) {
			sucursal.setImagen(existingSucursal.getImagen());
		} else {
			if (!existingSucursal.getImagen().equals("default.jpg")) {
				upload.deleteImage(existingSucursal.getImagen());
			}
			String nombreImagen = upload.saveImages(file, sucursal.getNombre());
			sucursal.setImagen(nombreImagen);
		}

		sucursalesService.update(sucursal);
		redirectAttributes.addFlashAttribute("success", "Sucursal actualizada exitosamente");
		return "redirect:/sucursales";
	}

	@GetMapping("/delete/{id}")
	public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		Optional<Sucursal> optionalSucursal = sucursalesService.get(id);

		if (optionalSucursal.isPresent()) {
			Sucursal sucursal = optionalSucursal.get();

			if (!sucursal.getImagen().equals("default.jpg")) {
				upload.deleteImage(sucursal.getImagen());
			}

			sucursalesService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Sucursal eliminada exitosamente");
		} else {
			redirectAttributes.addFlashAttribute("error", "No se encontró la sucursal");
		}

		return "redirect:/sucursales";
	}
}