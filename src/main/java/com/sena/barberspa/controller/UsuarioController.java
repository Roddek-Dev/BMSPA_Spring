package com.sena.barberspa.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sena.barberspa.model.Agendamiento;
import com.sena.barberspa.model.Orden;
import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.service.EmailService;
import com.sena.barberspa.service.IAgendamientosService;
import com.sena.barberspa.service.IOrdenService;
import com.sena.barberspa.service.IUsuarioService;
import com.sena.barberspa.service.UploadFileService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

	private final Logger LOGGER = LoggerFactory.getLogger(UsuarioController.class);

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IOrdenService ordenService;

	@Autowired
	private IAgendamientosService agendamientosService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private UploadFileService upload;

	public static void storeResetToken(String token, String email) {
		resetTokens.put(token, email);
	}

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	// Almacén temporal de tokens de restablecimiento (en producción, usar base de datos)
	private static final ConcurrentHashMap<String, String> resetTokens = new ConcurrentHashMap<>();

	@GetMapping("/registro")
	public String showRegistrationForm(Model model) {
		model.addAttribute("usuario", new Usuario());
		return "usuario/registro";
	}

	@PostMapping("/save")
	public String registerUser(Usuario usuario, RedirectAttributes redirectAttributes) {
		try {
			usuario.setTipo("USER");
			usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
			usuarioService.save(usuario);
			redirectAttributes.addFlashAttribute("success", "Registro exitoso! Por favor inicie sesión.");
			return "redirect:/usuario/login";
		} catch (Exception e) {
			LOGGER.error("Error al registrar usuario: {}", e.getMessage());
			redirectAttributes.addFlashAttribute("error", "Error al registrar. Intente nuevamente.");
			return "redirect:/usuario/registro";
		}
	}

	@GetMapping("/login")
	public String showLoginForm(Model model) {
		model.addAttribute("usuario", new Usuario());
		return "usuario/login";
	}

	@GetMapping("/acceder")
	public String acceder(Usuario usuario, HttpSession session) {
		LOGGER.info("Accesos: {}", usuario);
		Optional<Usuario> user = usuarioService
				.findById(Integer.parseInt(session.getAttribute("idUsuario").toString()));
		LOGGER.info("Usuario db obtenido: {}", user.get());
		if (user.isPresent()) {
			session.setAttribute("idUsuario", user.get().getId());
			if (user.get().getTipo().equals("ADMIN")) {
				return "redirect:/administrador";
			} else {
				return "redirect:/";
			}
		} else {
			LOGGER.warn("Usuario no existe en DB");
		}
		return "redirect:/";
	}

	@GetMapping("/cerrar")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}

	/**
	 * Método para procesar la solicitud de restablecimiento de contraseña
	 */
	@PostMapping("/resetPassword")
	@ResponseBody
	public ResponseEntity<String> resetPassword(@RequestParam("resetEmail") String email) {
		try {
			// Verificar si el usuario existe
			Optional<Usuario> usuario = usuarioService.findByEmail(email);
			if (usuario.isPresent()) {
				// Generar token único
				String token = UUID.randomUUID().toString();

				// Guardar token en el mapa (en producción, usar base de datos)
				resetTokens.put(token, email);

				// Enviar correo de restablecimiento
				emailService.sendPasswordResetEmail(email);

				return ResponseEntity.ok("Correo enviado correctamente");
			} else {
				// Por seguridad, no informamos si el correo existe o no
				return ResponseEntity.ok("Correo enviado correctamente");
			}
		} catch (Exception e) {
			LOGGER.error("Error al procesar solicitud de restablecimiento: {}", e.getMessage());
			return ResponseEntity.badRequest().body("Error al procesar la solicitud");
		}
	}

	/**
	 * Método para mostrar el formulario de cambio de contraseña
	 */
	@GetMapping("/cambiarPassword")
	public String showChangePasswordForm(@RequestParam("token") String token, Model model) {
		// Verificar si el token es válido
		if (resetTokens.containsKey(token)) {
			model.addAttribute("token", token);
			return "usuario/cambiar-password";
		} else {
			return "redirect:/usuario/token-invalido";
		}
	}

	/**
	 * Método para procesar el cambio de contraseña
	 */
	@PostMapping("/saveNewPassword")
	public String saveNewPassword(@RequestParam("token") String token,
								  @RequestParam("password") String password,
								  RedirectAttributes redirectAttributes) {
		// Verificar si el token es válido
		String email = resetTokens.get(token);
		if (email != null) {
			try {
				// Buscar usuario
				Optional<Usuario> optUser = usuarioService.findByEmail(email);
				if (optUser.isPresent()) {
					Usuario usuario = optUser.get();

					// Actualizar contraseña
					usuario.setPassword(passwordEncoder.encode(password));
					usuarioService.update(usuario);

					// Eliminar token usado
					resetTokens.remove(token);

					redirectAttributes.addFlashAttribute("success",
							"¡Tu contraseña ha sido actualizada correctamente! Ahora puedes iniciar sesión con tu nueva contraseña.");
					return "redirect:/usuario/login";
				}
			} catch (Exception e) {
				LOGGER.error("Error al cambiar contraseña: {}", e.getMessage());
			}
		}

		redirectAttributes.addFlashAttribute("error",
				"No se pudo cambiar la contraseña. El enlace ha expirado o es inválido.");
		return "redirect:/usuario/login";
	}

	@GetMapping("/token-invalido")
	public String tokenInvalido() {
		return "usuario/token-invalido";
	}

	@GetMapping("/compras")
	public String showUserOrders(HttpSession session, Model model) {
		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		Usuario usuario = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString()))
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		List<Orden> ordenes = ordenService.findByUsuario(usuario);
		model.addAttribute("ordenes", ordenes);
		model.addAttribute("sesion", session.getAttribute("idUsuario"));

		return "usuario/compras";
	}

	@GetMapping("/compras/{id}")
	public String showOrderDetails(@PathVariable Integer id, HttpSession session, Model model) {
		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		Orden orden = ordenService.findById(id).orElseThrow(() -> new RuntimeException("Orden no encontrada"));

		// Verificar que la orden pertenece al usuario
		if (!orden.getUsuario().getId().equals(Integer.parseInt(session.getAttribute("idUsuario").toString()))) {
			return "redirect:/usuario/compras";
		}

		model.addAttribute("detalles", orden.getDetalle());
		model.addAttribute("orden", orden);
		model.addAttribute("sesion", session.getAttribute("idUsuario"));

		return "usuario/detallecompra";
	}

	@GetMapping("/perfil")
	public String showProfile(HttpSession session, Model model) {
		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		Integer idUsuario = (Integer) session.getAttribute("idUsuario");
		Usuario usuario = usuarioService.findById(idUsuario)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		// Obtener agendamientos del usuario
		List<Agendamiento> agendamientos = agendamientosService.findByUsuario(usuario);

		// Obtener órdenes del usuario
		List<Orden> ordenes = ordenService.findByUsuario(usuario);

		// Agregar los datos al modelo
		model.addAttribute("usuario", usuario);
		model.addAttribute("agendamientos", agendamientos);
		model.addAttribute("ordenes", ordenes);
		model.addAttribute("ordenesCount", ordenes.size());
		model.addAttribute("citasCount", agendamientos.size());
		model.addAttribute("sesion", session.getAttribute("idUsuario"));

		return "usuario/perfil";
	}

	@GetMapping("/editar")
	public String showEditForm(HttpSession session, Model model) {
		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		Usuario usuario = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString()))
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		model.addAttribute("usuario", usuario);
		return "usuario/editar";
	}

	@PostMapping("/actualizar")
	public String updateProfile(Usuario usuario,
								@RequestParam(value = "img", required = false) MultipartFile file,
								HttpSession session,
								RedirectAttributes redirectAttributes) throws IOException {
		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		try {
			Usuario existingUser = usuarioService
					.findById(Integer.parseInt(session.getAttribute("idUsuario").toString()))
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

			// Actualizar solo los campos permitidos
			existingUser.setNombre(usuario.getNombre());
			existingUser.setEmail(usuario.getEmail());
			existingUser.setDireccion(usuario.getDireccion());
			existingUser.setTelefono(usuario.getTelefono());

			if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
				existingUser.setPassword(passwordEncoder.encode(usuario.getPassword()));
			}

			// Manejar la imagen de perfil si se subió una nueva
			if (file != null && !file.isEmpty()) {
				String nombreImagen = upload.saveImages(file, existingUser.getNombre());
				existingUser.setImagen(nombreImagen);
			}

			usuarioService.save(existingUser);
			redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
			return "redirect:/usuario/perfil";

		} catch (Exception e) {
			LOGGER.error("Error al actualizar perfil: {}", e.getMessage());
			redirectAttributes.addFlashAttribute("error", "Error al actualizar perfil");
			return "redirect:/usuario/editar";
		}
	}

}