package com.sena.barberspa.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mercadopago.exceptions.*;
import com.paypal.api.payments.*;
import com.paypal.base.rest.PayPalRESTException;
import com.sena.barberspa.model.*;
import com.sena.barberspa.service.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
public class homeUserController {

	private static final Logger LOGGER = LoggerFactory.getLogger(homeUserController.class);
	private static final String SESSION_ORDER_ID = "ordenId";
	private static final String SESSION_USER_ID = "idUsuario";
	private static final String REDIRECT_LOGIN = "redirect:/usuario/login";
	private static final String PAYMENT_SUCCESS = "pagos/pago_exitoso";
	private static final String PAYMENT_FAILED = "pagos/pago_fallido";
	private static final String PAYMENT_PENDING = "pagos/pago_pendiente";

	@Autowired private MercadoPagoService mercadoPagoService;
	@Autowired private IProductoService productoService;
	@Autowired private IServiciosService servicioService;
	@Autowired private ISucursalesService sucursalService;
	@Autowired private IUsuarioService usuarioService;
	@Autowired private IOrdenService ordenService;
	@Autowired private IDetalleOrdenService detalleOrdenService;
	@Autowired private IAgendamientosService agendamientosService;
	@Autowired private PayPalService paypalService;

	private List<DetalleOrden> detalles = new ArrayList<>();
	private Orden orden = new Orden();

	@ModelAttribute
	public void addCommonAttributes(Model model, HttpSession session) {
		Integer userId = (Integer) session.getAttribute(SESSION_USER_ID);
		if (userId != null) {
			usuarioService.findById(userId).ifPresent(usuario -> {
				model.addAttribute("usuario", usuario);
				model.addAttribute("sesion", userId);
			});
		}
	}

	// Métodos de vista básicos
	@GetMapping({"", "/home"})
	public String home(Model model) {
		model.addAttribute("productos", productoService.findAll());
		model.addAttribute("servicios", servicioService.findAll());
		model.addAttribute("sucursales", sucursalService.findAll());
		return "usuario/home";
	}

	@GetMapping("/mantenimiento")
	public String mantenimiento() {
		return "usuario/mantenimiento";
	}

	@GetMapping("/productosVista")
	public String productosVista(Model model) {
		model.addAttribute("productos", productoService.findAll());
		return "usuario/productosVista";
	}

	@GetMapping("/serviciosVista")
	public String serviciosVista(Model model) {
		model.addAttribute("servicios", servicioService.findAll());
		return "usuario/serviciosVista";
	}

	// Métodos de agendamiento
	@PostMapping("/save")
	public String saveAgendamiento(@RequestParam Integer servicio,
								   @RequestParam Integer sucursal,
								   @RequestParam String fechaHora,
								   @RequestParam String mensaje,
								   HttpSession session) throws IOException {

		Usuario usuario = getUsuarioFromSession(session)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		Agendamiento agendamiento = new Agendamiento();
		agendamiento.setFechaHora(parseDateTime(fechaHora));
		agendamiento.setMensaje(mensaje);
		agendamiento.setServicio(getServicio(servicio));
		agendamiento.setSucursal(getSucursal(sucursal));
		agendamiento.setEstado("SOLICITADA");
		agendamiento.setUsuario(usuario);

		agendamientosService.save(agendamiento);
		return "redirect:/";
	}

	// Métodos de carrito
	@GetMapping("productoHome/{id}")
	public String productoHome(@PathVariable Integer id, Model model) {
		model.addAttribute("producto", getProducto(id));
		return "usuario/productoHome";
	}

	@PostMapping("/cart")
	public String addCart(@RequestParam Integer id,
						  @RequestParam Double cantidad,
						  HttpSession session, Model model) {

		if (session.getAttribute(SESSION_USER_ID) == null) {
			return REDIRECT_LOGIN;
		}

		Producto producto = getProducto(id);
		addToCartIfNotExists(producto, cantidad);

		updateCartModel(model);
		return "usuario/carrito";
	}

	@GetMapping("/delete/cart/{id}")
	public String deleteProductoCart(@PathVariable Integer id, Model model) {
		detalles.removeIf(d -> d.getProducto().getId().equals(id));
		updateCartModel(model);
		return "usuario/carrito";
	}

	@GetMapping("/getCart")
	public String getCart(Model model) {
		updateCartModel(model);
		return "usuario/carrito";
	}

	@GetMapping("/order")
	public String order(Model model) {
		model.addAttribute("cart", detalles);
		model.addAttribute("orden", orden);
		return "usuario/resumenorden";
	}

	// Métodos de orden y pago
	@GetMapping("/saveOrder")
	public String saveOrder(HttpSession session) {
		if (session.getAttribute(SESSION_USER_ID) == null) {
			return REDIRECT_LOGIN;
		}

		Usuario usuario = getUsuarioFromSession(session)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		Orden ordenGuardada = createAndSaveOrder(usuario);
		session.setAttribute(SESSION_ORDER_ID, ordenGuardada.getId());

		return "redirect:/pagar/" + ordenGuardada.getId();
	}

	@PostMapping("/searchU")
	public String searchProducto(@RequestParam String nombreproducto, Model model) {
		model.addAttribute("productos", searchProducts(nombreproducto));
		return "usuario/productosVista";
	}

	// Métodos de MercadoPago
	@GetMapping("/pagar/{id}")
	public String procesarPago(@PathVariable Integer id,
							   HttpSession session,
							   Model model) {
		try {
			Orden orden = getOrden(id);
			session.setAttribute(SESSION_ORDER_ID, orden.getId());
			return "redirect:" + mercadoPagoService.createPreference(orden);
		} catch (MPException | MPApiException e) {
			model.addAttribute("error", "Error al procesar el pago: " + e.getMessage());
			return "usuario/error";
		}
	}

	@GetMapping("/success")
	public String pagoExitoso(@RequestParam String payment_id,
							  @RequestParam String status,
							  @RequestParam String merchant_order_id,
							  HttpSession session, Model model) {
		updateOrderStatus(session, model, "PAGADO");
		return PAYMENT_SUCCESS;
	}

	@GetMapping("/failure")
	public String pagoFallido(HttpSession session, Model model) {
		updateOrderStatus(session, model, "RECHAZADO");
		return PAYMENT_FAILED;
	}

	@GetMapping("/pending")
	public String pagoPendiente(HttpSession session, Model model) {
		updateOrderStatus(session, model, "PENDIENTE");
		return PAYMENT_PENDING;
	}

	// Métodos de PayPal
	@PostMapping("/paypal/create")
	public String createPaypalPayment(HttpSession session, RedirectAttributes redirectAttributes) {
		if (session.getAttribute(SESSION_USER_ID) == null) {
			return REDIRECT_LOGIN;
		}

		try {
			Integer ordenId = getOrderIdFromSession(session);
			Payment payment = paypalService.createPayment(
					getOrden(ordenId),
					"http://localhost:63106/paypal/cancel",
					"http://localhost:63106/paypal/success"
			);

			return getApprovalUrl(payment)
					.map(url -> "redirect:" + url)
					.orElseGet(() -> {
						redirectAttributes.addFlashAttribute("error", "No se pudo procesar el pago");
						return "redirect:/usuario/compras";
					});

		} catch (PayPalRESTException e) {
			LOGGER.error("Error al crear pago con PayPal: {}", e.getMessage());
			redirectAttributes.addFlashAttribute("error", "Error al procesar el pago: " + e.getMessage());
			return "redirect:/usuario/compras";
		}
	}

	@GetMapping("/paypal/success")
	public String paypalSuccessPayment(@RequestParam String paymentId,
									   @RequestParam String payerId,
									   HttpSession session, Model model) {
		try {
			Payment payment = paypalService.executePayment(paymentId, payerId);
			if (payment.getState().equals("approved")) {
				updateOrderStatus(session, model, "PAGADO");
				return PAYMENT_SUCCESS;
			}
		} catch (PayPalRESTException e) {
			LOGGER.error("Error al ejecutar pago con PayPal: {}", e.getMessage());
		}
		return "redirect:/paypal/cancel";
	}

	@GetMapping("/paypal/cancel")
	public String paypalCancelPayment(HttpSession session, Model model) {
		updateOrderStatus(session, model, "RECHAZADO");
		return PAYMENT_FAILED;
	}

	@PostMapping("/paypalOrder")
	public String processPaypalOrder(HttpSession session, RedirectAttributes redirectAttributes) {
		if (session.getAttribute(SESSION_USER_ID) == null) {
			return REDIRECT_LOGIN;
		}

		try {
			Usuario usuario = getUsuarioFromSession(session)
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

			Orden ordenGuardada = createAndSaveOrder(usuario);
			session.setAttribute(SESSION_ORDER_ID, ordenGuardada.getId());

			Payment payment = paypalService.createPayment(
					ordenGuardada,
					"http://localhost:63106/paypal/cancel",
					"http://localhost:63106/paypal/success"
			);

			return getApprovalUrl(payment)
					.map(url -> "redirect:" + url)
					.orElseGet(() -> {
						redirectAttributes.addFlashAttribute("error", "Error al procesar el pago con PayPal");
						return "redirect:/user/cart";
					});

		} catch (Exception e) {
			LOGGER.error("Error al procesar el pago con PayPal: {}", e.getMessage(), e);
			redirectAttributes.addFlashAttribute("error", "Error al procesar el pago: " + e.getMessage());
			return "redirect:/getCart";
		}
	}

	// Métodos utilitarios privados
	private Optional<Usuario> getUsuarioFromSession(HttpSession session) {
		return Optional.ofNullable((Integer) session.getAttribute(SESSION_USER_ID))
				.flatMap(usuarioService::findById);
	}

	private Producto getProducto(Integer id) {
		return productoService.get(id)
				.orElseThrow(() -> new RuntimeException("Producto no encontrado"));
	}

	private Servicio getServicio(Integer id) {
		return servicioService.get(id)
				.orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
	}

	private Sucursal getSucursal(Integer id) {
		return sucursalService.get(id)
				.orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
	}

	private Orden getOrden(Integer id) {
		return ordenService.findById(id)
				.orElseThrow(() -> new RuntimeException("Orden no encontrada"));
	}

	private Integer getOrderIdFromSession(HttpSession session) {
		return Optional.ofNullable((Integer) session.getAttribute(SESSION_ORDER_ID))
				.orElseThrow(() -> new RuntimeException("Orden no encontrada"));
	}

	private LocalDateTime parseDateTime(String fechaHora) {
		return LocalDateTime.parse(fechaHora, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
	}

	private void addToCartIfNotExists(Producto producto, Double cantidad) {
		if (detalles.stream().noneMatch(d -> d.getProducto().getId().equals(producto.getId()))) {
			DetalleOrden detalle = new DetalleOrden();
			detalle.setCantidad(cantidad);
			detalle.setPrecio(producto.getPrecio());
			detalle.setNombre(producto.getNombreproducto());
			detalle.setTotal(producto.getPrecio() * cantidad);
			detalle.setProducto(producto);
			detalles.add(detalle);
		}
	}

	private void updateCartModel(Model model) {
		double sumaTotal = detalles.stream().mapToDouble(DetalleOrden::getTotal).sum();
		orden.setTotal(sumaTotal);
		model.addAttribute("cart", detalles);
		model.addAttribute("orden", orden);
	}

	private Orden createAndSaveOrder(Usuario usuario) {
		orden.setFechacreacion(new Date());
		orden.setNumero(ordenService.generarNumeroOrden());
		orden.setUsuario(usuario);
		orden.setEstado("PENDIENTE");

		Orden ordenGuardada = ordenService.save(orden);

		detalles.forEach(dt -> {
			dt.setOrden(ordenGuardada);
			detalleOrdenService.save(dt);
		});

		// Limpiar carrito
		orden = new Orden();
		detalles.clear();

		return ordenGuardada;
	}

	private List<Producto> searchProducts(String nombre) {
		return productoService.findAll().stream()
				.filter(p -> p.getNombreproducto().toUpperCase().contains(nombre.toUpperCase()))
				.collect(Collectors.toList());
	}

	private void updateOrderStatus(HttpSession session, Model model, String status) {
		Optional.ofNullable((Integer) session.getAttribute(SESSION_ORDER_ID))
				.flatMap(ordenService::findById)
				.ifPresent(orden -> {
					orden.setEstado(status);
					ordenService.update(orden);
					model.addAttribute("orden", orden);
				});
	}

	private Optional<String> getApprovalUrl(Payment payment) {
		return payment.getLinks().stream()
				.filter(link -> link.getRel().equals("approval_url"))
				.findFirst()
				.map(Links::getHref);
	}
}