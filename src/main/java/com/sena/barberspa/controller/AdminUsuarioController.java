package com.sena.barberspa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.service.IUsuarioService;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    @Autowired
    private IUsuarioService usuarioService;

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
    public String listarUsuarios(Model model, HttpSession session) {
        // Obtener el ID del usuario actual
        Integer idUsuarioActual = (Integer) session.getAttribute("idUsuario");

        // Obtener todos los usuarios
        List<Usuario> todosUsuarios = usuarioService.findAll();

        // Filtrar la lista para excluir al usuario actual
        List<Usuario> usuarios = todosUsuarios.stream()
                .filter(u -> !u.getId().equals(idUsuarioActual))
                .collect(Collectors.toList());

        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios";
    }

    @PostMapping("/cambiar-rol/{id}")
    public String cambiarRol(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.findById(id).orElse(null);

        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/admin/usuarios";
        }

        // Cambiar el rol del usuario (USER <-> ADMIN)
        if ("USER".equals(usuario.getTipo())) {
            usuario.setTipo("ADMIN");
            redirectAttributes.addFlashAttribute("success", "Usuario " + usuario.getNombre() + " ahora es administrador");
        } else if ("ADMIN".equals(usuario.getTipo())) {
            usuario.setTipo("USER");
            redirectAttributes.addFlashAttribute("success", "Usuario " + usuario.getNombre() + " ahora es usuario regular");
        }

        usuarioService.update(usuario);
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}