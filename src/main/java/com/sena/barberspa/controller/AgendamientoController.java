package com.sena.barberspa.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

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

import com.sena.barberspa.model.Agendamiento;

import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.service.IAgendamientosService;
import com.sena.barberspa.service.IUsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/agendamientos")
public class AgendamientoController {

	// Instancia de LOGGER para ver datos en consola
	private final Logger LOGGER = (Logger) LoggerFactory.getLogger(AgendamientoController.class);

	@Autowired
	private IAgendamientosService agendamientosService;

	@Autowired
	private IUsuarioService usuarioService;

	@GetMapping("")
	public String show(Model model) {
		model.addAttribute("agendamientos", agendamientosService.findAll());
		return "agendamientos/show";
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

	// metodo para llenar los imputs de la vista edit
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Integer id, Model model) {
		Agendamiento s = new Agendamiento();
		Optional<Agendamiento> os = agendamientosService.get(id);
		s = os.get();
		LOGGER.info("Busqueda de sucursal por id {}", s);
		model.addAttribute("estadosAgendamiento", Arrays.asList("Pendiente", "Confirmado", "Cancelado", "Completado")); // reales
		model.addAttribute("agendamiento", s);
		return "agendamientos/edit";
	}

	// metodo para actualizar los datos de un producto
	@PostMapping("/update")
	public String update(Agendamiento agendamiento) throws IOException {
		LOGGER.info("Este es el objeto de la sucursal a actualizar en la DB {}", agendamiento);
		Agendamiento s = new Agendamiento();
		s = agendamientosService.get(agendamiento.getId()).get();
		agendamiento.setUsuario(s.getUsuario());
		agendamientosService.update(agendamiento);
		return "redirect:/agendamientos";
	}

	@GetMapping("/delete/{id}")
	public String delete(@PathVariable Integer id) {
		agendamientosService.delete(id);
		return "redirect:/agendamientos";
	}

}
