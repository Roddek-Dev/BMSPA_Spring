package com.sena.barberspa.controller;

import com.sena.barberspa.model.Agendamiento;
import com.sena.barberspa.model.Recordatorio;
import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.service.IAgendamientosService;
import com.sena.barberspa.service.IOrdenService;
import com.sena.barberspa.service.IRecordatorioService;
import com.sena.barberspa.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/recordatorios")
public class RecordatorioController {

    @Autowired
    private IRecordatorioService recordatorioService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IAgendamientosService agendamientosService;

    @Autowired
    private IOrdenService ordenService;

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

    @GetMapping("")
    public String listarRecordatorios(Model model, HttpSession session) {
        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        if (idUsuario != null) {
            Usuario usuario = usuarioService.findById(idUsuario).orElse(null);
            if (usuario != null) {
                List<Recordatorio> recordatorios = recordatorioService.findByUsuario(usuario);
                model.addAttribute("recordatorios", recordatorios);
            }
        }
        return "recordatorios/lista";
    }

    @GetMapping("/create")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("recordatorio", new Recordatorio());
        return "recordatorios/create";
    }

    @PostMapping("/save")
    public String guardarRecordatorio(@ModelAttribute Recordatorio recordatorio,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        if (idUsuario != null) {
            Usuario usuario = usuarioService.findById(idUsuario).orElse(null);
            if (usuario != null) {
                recordatorio.setUsuario(usuario);
                recordatorio.setActivo(true);
                recordatorioService.save(recordatorio);
                redirectAttributes.addFlashAttribute("success", "Recordatorio creado correctamente");
                return "redirect:/recordatorios";
            }
        }
        redirectAttributes.addFlashAttribute("error", "Error al crear el recordatorio");
        return "redirect:/recordatorios";
    }

    @PostMapping("/desactivar/{id}")
    @ResponseBody
    public ResponseEntity<?> desactivarRecordatorio(@PathVariable Integer id) {
        try {
            recordatorioService.desactivar(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al desactivar el recordatorio");
        }
    }

    @PostMapping("/fijar/{id}")
    @ResponseBody
    public ResponseEntity<?> fijarRecordatorio(@PathVariable Integer id) {
        try {
            recordatorioService.cambiarFijado(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al fijar el recordatorio");
        }
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarRecordatorio(@PathVariable Integer id) {
        try {
            Optional<Recordatorio> optRecordatorio = recordatorioService.get(id);
            if (optRecordatorio.isPresent() && optRecordatorio.get().getAgendamiento() == null) {
                // Solo eliminar completamente si no está asociado a un agendamiento
                recordatorioService.delete(id);
            } else {
                // Si está asociado a un agendamiento, solo desactivarlo
                recordatorioService.desactivar(id);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar el recordatorio");
        }
    }
}