package com.sena.barberspa.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sena.barberspa.model.Recordatorio;
import com.sena.barberspa.service.*;
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

import com.sena.barberspa.model.Producto;
import com.sena.barberspa.model.Usuario;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/productos")
public class ProductoController {

	private final Logger LOGGER = LoggerFactory.getLogger(ProductoController.class);

	@Autowired
	private IProductoService productoService;

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private UploadFileService upload;
	@Autowired
	private IRecordatorioService recordatorioService;
	@Autowired
	private IOrdenService ordenService;

	@GetMapping("")
	public String show(Model model) {
		model.addAttribute("productos", productoService.findAll());
		return "productos/show";
	}

	@ModelAttribute
	public void addUsuarioToModel(Model model, HttpSession session) {
		Integer idUsuario = (Integer) session.getAttribute("idUsuario");
		if (idUsuario != null) {
			Usuario usuario = usuarioService.findById(idUsuario).orElse(null);
			if (usuario != null) {
				model.addAttribute("usuario", usuario);
				// Procesar agendamientos próximos y convertirlos en recordatorios
				recordatorioService.procesarAgendamientosProximos(usuario, 3);

				// Obtener recordatorios para mostrar en la barra lateral
				List<Recordatorio> recordatorios = recordatorioService.findByUsuario(usuario);
				model.addAttribute("recordatorios", recordatorios);
			}
		}
	}

	@GetMapping("/create")
	public String create(Model model) {
		model.addAttribute("producto", new Producto());
		return "productos/create";
	}

	@PostMapping("/save")
	public String save(@Valid Producto producto, BindingResult result, @RequestParam("img") MultipartFile file,
			HttpSession session, RedirectAttributes redirectAttributes) throws IOException {

		if (result.hasErrors()) {
			return "productos/create";
		}

		LOGGER.info("Guardando producto: {}", producto);
		Usuario u = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString())).get();
		producto.setUsuario(u);

		// Manejo de la imagen
		if (!file.isEmpty()) {
			String nombreImagen = upload.saveImages(file, producto.getNombreproducto());
			producto.setImagen(nombreImagen);
		} else {
			producto.setImagen("default.jpg");
		}

		productoService.save(producto);
		redirectAttributes.addFlashAttribute("success", "Producto creado exitosamente");
		return "redirect:/productos";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Integer id, Model model) {
		Optional<Producto> optionalProducto = productoService.get(id);

		if (optionalProducto.isEmpty()) {
			return "redirect:/productos";
		}

		model.addAttribute("producto", optionalProducto.get());
		return "productos/edit";
	}

	@PostMapping("/update")
	public String update(@Valid Producto producto, BindingResult result, @RequestParam("img") MultipartFile file,
			RedirectAttributes redirectAttributes) throws IOException {

		if (result.hasErrors()) {
			return "productos/edit";
		}

		LOGGER.info("Actualizando producto: {}", producto);
		Producto existingProducto = productoService.get(producto.getId()).orElse(null);

		if (existingProducto == null) {
			return "redirect:/productos";
		}

		// Manejo de la imagen
		if (file.isEmpty()) {
			producto.setImagen(existingProducto.getImagen());
		} else {
			if (!existingProducto.getImagen().equals("default.jpg")) {
				upload.deleteImage(existingProducto.getImagen());
			}
			String nombreImagen = upload.saveImages(file, producto.getNombreproducto());
			producto.setImagen(nombreImagen);
		}

		producto.setUsuario(existingProducto.getUsuario());
		productoService.update(producto);
		redirectAttributes.addFlashAttribute("success", "Producto actualizado exitosamente");
		return "redirect:/productos";
	}

	@GetMapping("/delete/{id}")
	public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		Optional<Producto> optionalProducto = productoService.get(id);

		if (optionalProducto.isPresent()) {
			Producto producto = optionalProducto.get();

			if (!producto.getImagen().equals("default.jpg")) {
				try {
					upload.deleteImage(producto.getImagen());
				} catch (IOException e) {
					LOGGER.error("Error al eliminar la imagen: " + producto.getImagen(), e);
				}
			}

			productoService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Producto eliminado exitosamente");
		} else {
			redirectAttributes.addFlashAttribute("error", "No se encontró el producto");
		}

		return "redirect:/productos";
	}

	@PostMapping("/search")
	public String searchProducto(@RequestParam String nombre, Model model) {
		LOGGER.info("Buscando producto: {}", nombre);
		List<Producto> productos = productoService.findAll().stream()
				.filter(p -> p.getNombreproducto().toUpperCase().contains(nombre.toUpperCase()))
				.collect(Collectors.toList());
		model.addAttribute("productos", productos);
		return "productos/show";
	}
}