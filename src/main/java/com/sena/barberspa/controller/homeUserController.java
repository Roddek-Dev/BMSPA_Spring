package com.sena.barberspa.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.sena.barberspa.model.*;
import com.sena.barberspa.service.*;

import jakarta.servlet.http.HttpSession;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/")
public class homeUserController {

	private final Logger LOGGER = LoggerFactory.getLogger(homeUserController.class);

	@Autowired
	private MercadoPagoService mercadoPagoService;
	@Autowired
	private IProductoService productoService;
	@Autowired
	private IServiciosService servicioService;
	@Autowired
	private ISucursalesService sucursalService;
	@Autowired
	private IUsuarioService usuarioService;
	@Autowired
	private IOrdenService ordenService;
	@Autowired
	private IDetalleOrdenService detalleOrdenService;
	@Autowired
	private IAgendamientosService agendamientosService;
	@Autowired
	private PayPalService paypalService;

	// Variables temporales para el carrito
	private List<DetalleOrden> detalles = new ArrayList<>();
	private Orden orden = new Orden();

	@GetMapping("")
	public String home(Model model, HttpSession session) {
		model.addAttribute("productos", productoService.findAll());
		model.addAttribute("servicios", servicioService.findAll());
		model.addAttribute("sucursales", sucursalService.findAll());
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/home";
	}

	@GetMapping("/mantenimiento")
	public String mantenimiento(Model model, HttpSession session) {
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/mantenimiento";
	}

	@GetMapping("/productosVista")
	public String productosVista(Model model, HttpSession session) {
		model.addAttribute("productos", productoService.findAll());
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/productosVista";
	}

	@GetMapping("/serviciosVista")
	public String serviciosVista(Model model, HttpSession session) {
		model.addAttribute("servicios", servicioService.findAll());
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/serviciosVista";
	}

	@PostMapping("/save")
	public String saveAgendamiento(@RequestParam("servicio") Integer idServicio,
			@RequestParam("sucursal") Integer idSucursal, @RequestParam String fechaHora, @RequestParam String mensaje,
			HttpSession session) throws IOException {

		Usuario usuario = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString()))
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		Agendamiento agendamiento = new Agendamiento();
		agendamiento.setFechaHora(LocalDateTime.parse(fechaHora, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
		agendamiento.setMensaje(mensaje);
		agendamiento.setServicio(servicioService.get(idServicio).orElseThrow());
		agendamiento.setSucursal(sucursalService.get(idSucursal).orElseThrow());
		agendamiento.setEstado("SOLICITADA");
		agendamiento.setUsuario(usuario);

		agendamientosService.save(agendamiento);
		return "redirect:/";
	}

	@GetMapping("productoHome/{id}")
	public String productoHome(@PathVariable Integer id, Model model, HttpSession session) {
		Producto producto = productoService.get(id).orElseThrow(() -> new RuntimeException("Producto no encontrado"));

		model.addAttribute("producto", producto);
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/productoHome";
	}

	@GetMapping("servicioHome/{id}")
	public String servicioHome(@PathVariable Integer id, Model model, HttpSession session) {
		Servicio servicio = servicioService.get(id).orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

		model.addAttribute("servicio", servicio);
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/servicioHome";
	}

	@PostMapping("/cart")
	public String addCart(@RequestParam Integer id, @RequestParam Double cantidad, Model model, HttpSession session) {

		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		Producto producto = productoService.get(id).orElseThrow(() -> new RuntimeException("Producto no encontrado"));

		// Evitar duplicados
		boolean productoExistente = detalles.stream().anyMatch(d -> d.getProducto().getId().equals(id));

		if (!productoExistente) {
			DetalleOrden detalle = new DetalleOrden();
			detalle.setCantidad(cantidad);
			detalle.setPrecio(producto.getPrecio());
			detalle.setNombre(producto.getNombreproducto());
			detalle.setTotal(producto.getPrecio() * cantidad);
			detalle.setProducto(producto);
			detalles.add(detalle);
		}

		double sumaTotal = detalles.stream().mapToDouble(DetalleOrden::getTotal).sum();

		orden.setTotal(sumaTotal);
		model.addAttribute("cart", detalles);
		model.addAttribute("orden", orden);
		model.addAttribute("sesion", session.getAttribute("idUsuario"));

		return "usuario/carrito";
	}

	@GetMapping("/delete/cart/{id}")
	public String deleteProductoCart(@PathVariable Integer id, Model model, HttpSession session) {
		detalles.removeIf(d -> d.getProducto().getId().equals(id));

		double sumaTotal = detalles.stream().mapToDouble(DetalleOrden::getTotal).sum();

		orden.setTotal(sumaTotal);
		model.addAttribute("cart", detalles);
		model.addAttribute("orden", orden);
		model.addAttribute("sesion", session.getAttribute("idUsuario"));

		return "usuario/carrito";
	}

	@GetMapping("/getCart")
	public String getCart(Model model, HttpSession session) {
		model.addAttribute("cart", detalles);
		model.addAttribute("orden", orden);
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/carrito";
	}

	@GetMapping("/order")
	public String order(Model model, HttpSession session) {
		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		Usuario usuario = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString()))
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		model.addAttribute("cart", detalles);
		model.addAttribute("orden", orden);
		model.addAttribute("usuario", usuario);
		model.addAttribute("sesion", session.getAttribute("idUsuario"));

		return "usuario/resumenorden";
	}

	@GetMapping("/saveOrder")
	public String saveOrder(HttpSession session) {
		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		Usuario usuario = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString()))
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		orden.setFechacreacion(new Date());
		orden.setNumero(ordenService.generarNumeroOrden());
		orden.setUsuario(usuario);
		orden.setEstado("PENDIENTE");

		Orden ordenGuardada = ordenService.save(orden);

		// Guardar el ID de la orden en la sesión para usarlo con pagos
		session.setAttribute("ordenId", ordenGuardada.getId());

		detalles.forEach(dt -> {
			dt.setOrden(ordenGuardada);
			detalleOrdenService.save(dt);
		});

		// Limpiar carrito
		orden = new Orden();
		detalles.clear();

		// Redireccionar a la página de MercadoPago
		return "redirect:/pagar/" + ordenGuardada.getId();
	}

	@PostMapping("/searchU")
	public String searchProducto(@RequestParam String nombreproducto, Model model) {
		List<Producto> productos = productoService.findAll().stream()
				.filter(p -> p.getNombreproducto().toUpperCase().contains(nombreproducto.toUpperCase()))
				.collect(Collectors.toList());

		model.addAttribute("productos", productos);
		return "usuario/productosVista";
	}

	@GetMapping("/pagar/{id}")
	public String procesarPago(@PathVariable Integer id, Model model, HttpSession session) {
		try {
			Orden orden = ordenService.findById(id).orElseThrow(() -> new RuntimeException("Orden no encontrada"));

			session.setAttribute("ordenId", orden.getId());
			String paymentUrl = mercadoPagoService.createPreference(orden);
			return "redirect:" + paymentUrl;

		} catch (MPException | MPApiException e) {
			model.addAttribute("error", "Error al procesar el pago: " + e.getMessage());
			return "usuario/error";
		}
	}

	@GetMapping("/success")
	public String pagoExitoso(@RequestParam String payment_id, @RequestParam String status,
			@RequestParam String merchant_order_id, HttpSession session, Model model) {

		Integer idOrden = (Integer) session.getAttribute("ordenId");
		if (idOrden != null) {
			ordenService.findById(idOrden).ifPresent(orden -> {
				orden.setEstado("PAGADO");
				ordenService.update(orden);
				model.addAttribute("orden", orden);
			});
		}

		return "pagos/pago_exitoso";
	}

	@GetMapping("/failure")
	public String pagoFallido(HttpSession session, Model model) {
		Integer idOrden = (Integer) session.getAttribute("ordenId");
		if (idOrden != null) {
			ordenService.findById(idOrden).ifPresent(orden -> {
				orden.setEstado("RECHAZADO");
				ordenService.update(orden);
				model.addAttribute("orden", orden);
			});
		}

		return "pagos/pago_fallido";
	}

	@GetMapping("/pending")
	public String pagoPendiente(HttpSession session, Model model) {
		Integer idOrden = (Integer) session.getAttribute("ordenId");
		if (idOrden != null) {
			ordenService.findById(idOrden).ifPresent(orden -> {
				orden.setEstado("PENDIENTE");
				ordenService.update(orden);
				model.addAttribute("orden", orden);
			});
		}

		return "pagos/pago_pendiente";
	}
	@PostMapping("/paypal/create")
	public String createPaypalPayment(HttpSession session, RedirectAttributes redirectAttributes) {
		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		try {
			Integer ordenId = (Integer) session.getAttribute("ordenId");
			if (ordenId == null) {
				redirectAttributes.addFlashAttribute("error", "Orden no encontrada");
				return "redirect:/usuario/compras";
			}

			Orden orden = ordenService.findById(ordenId)
					.orElseThrow(() -> new RuntimeException("Orden no encontrada"));

			Payment payment = paypalService.createPayment(
					orden,
					"http://localhost:63106/paypal/cancel",
					"http://localhost:63106/paypal/success"
			);

			for(Links link : payment.getLinks()) {
				if(link.getRel().equals("approval_url")) {
					return "redirect:" + link.getHref();
				}
			}

		} catch (PayPalRESTException e) {
			LOGGER.error("Error al crear pago con PayPal: {}", e.getMessage());
			redirectAttributes.addFlashAttribute("error", "Error al procesar el pago: " + e.getMessage());
		}

		redirectAttributes.addFlashAttribute("error", "No se pudo procesar el pago");
		return "redirect:/usuario/compras";
	}

	@GetMapping("/paypal/success")
	public String paypalSuccessPayment(@RequestParam("paymentId") String paymentId,
									   @RequestParam("PayerID") String payerId,
									   HttpSession session, Model model) {

		try {
			Payment payment = paypalService.executePayment(paymentId, payerId);

			if(payment.getState().equals("approved")) {
				Integer idOrden = (Integer) session.getAttribute("ordenId");
				if (idOrden != null) {
					ordenService.findById(idOrden).ifPresent(orden -> {
						orden.setEstado("PAGADO");
						ordenService.update(orden);
						model.addAttribute("orden", orden);
					});
				}

				return "pagos/pago_exitoso";
			}
		} catch (PayPalRESTException e) {
			LOGGER.error("Error al ejecutar pago con PayPal: {}", e.getMessage());
		}

		return "redirect:/paypal/cancel";
	}

	@GetMapping("/paypal/cancel")
	public String paypalCancelPayment(HttpSession session, Model model) {
		Integer idOrden = (Integer) session.getAttribute("ordenId");
		if (idOrden != null) {
			ordenService.findById(idOrden).ifPresent(orden -> {
				orden.setEstado("RECHAZADO");
				ordenService.update(orden);
				model.addAttribute("orden", orden);
			});
		}

		return "pagos/pago_fallido";
	}
	@PostMapping("/paypalOrder")
	public String processPaypalOrder(HttpSession session, RedirectAttributes redirectAttributes) {
		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		try {
			LOGGER.info("Iniciando proceso de pago con PayPal");

			// Obtener usuario
			Integer idUsuario = (Integer) session.getAttribute("idUsuario");
			Usuario usuario = usuarioService.findById(idUsuario)
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
			LOGGER.info("Usuario identificado: ID={}, Nombre={}", idUsuario, usuario.getNombre());

			// Validar carrito
			if (detalles.isEmpty()) {
				LOGGER.error("Carrito vacío al intentar procesar un pago");
				redirectAttributes.addFlashAttribute("error", "El carrito está vacío");
				return "redirect:/getCart";
			}

			// Crear nueva orden
			orden.setFechacreacion(new Date());
			orden.setNumero(ordenService.generarNumeroOrden());
			orden.setUsuario(usuario);
			orden.setEstado("PENDIENTE");
			LOGGER.info("Orden creada: Número={}, Total={}", orden.getNumero(), orden.getTotal());

			// Guardar orden
			Orden ordenGuardada = ordenService.save(orden);
			LOGGER.info("Orden guardada en DB con ID: {}", ordenGuardada.getId());

			// Guardar ID de orden en sesión
			session.setAttribute("ordenId", ordenGuardada.getId());

			// Guardar detalles
			for (DetalleOrden detalle : detalles) {
				detalle.setOrden(ordenGuardada);
				detalleOrdenService.save(detalle);
				LOGGER.info("Detalle guardado: Producto={}, Cantidad={}, Total={}",
						detalle.getNombre(), detalle.getCantidad(), detalle.getTotal());
			}

			// Limpiar el carrito
			List<DetalleOrden> tempDetalles = new ArrayList<>(detalles);
			orden = new Orden();
			detalles.clear();
			LOGGER.info("Carrito limpiado");

			// Crear pago en PayPal
			LOGGER.info("Generando pago en PayPal para orden ID: {}", ordenGuardada.getId());
			Payment payment = paypalService.createPayment(
					ordenGuardada,
					"http://localhost:63106/paypal/cancel",
					"http://localhost:63106/paypal/success"
			);
			LOGGER.info("Pago de PayPal creado con ID: {}", payment.getId());

			// Buscar el enlace de aprobación
			String approvalUrl = null;
			for (Links link : payment.getLinks()) {
				if (link.getRel().equals("approval_url")) {
					approvalUrl = link.getHref();
					LOGGER.info("URL de aprobación de PayPal: {}", approvalUrl);
					break;
				}
			}

			if (approvalUrl != null) {
				return "redirect:" + approvalUrl;
			} else {
				LOGGER.error("No se encontró URL de aprobación en la respuesta de PayPal");
				redirectAttributes.addFlashAttribute("error", "Error al procesar el pago con PayPal");
				return "redirect:/user/cart";
			}

		} catch (Exception e) {
			LOGGER.error("Error al procesar el pago con PayPal: {}", e.getMessage(), e);
			redirectAttributes.addFlashAttribute("error", "Error al procesar el pago: " + e.getMessage());
			return "redirect:/getCart";
		}
	}

	@GetMapping("/prepareOrder")
	@ResponseBody
	public ResponseEntity<?> prepareOrder(HttpSession session) {
		if (session.getAttribute("idUsuario") == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
		}

		try {
			Usuario usuario = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString()))
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

			orden.setFechacreacion(new Date());
			orden.setNumero(ordenService.generarNumeroOrden());
			orden.setUsuario(usuario);
			orden.setEstado("PENDIENTE");

			Orden ordenGuardada = ordenService.save(orden);

			// Guardar el ID de la orden en la sesión para usarlo con PayPal
			session.setAttribute("ordenId", ordenGuardada.getId());

			for (DetalleOrden detalle : detalles) {
				detalle.setOrden(ordenGuardada);
				detalleOrdenService.save(detalle);
			}

			// Limpiar carrito
			List<DetalleOrden> detallesGuardados = new ArrayList<>(detalles);
			orden = new Orden();
			detalles.clear();

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("ordenId", ordenGuardada.getId());
			response.put("total", ordenGuardada.getTotal());
			response.put("detalles", detallesGuardados);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error al preparar la orden: " + e.getMessage());
		}
	}

}