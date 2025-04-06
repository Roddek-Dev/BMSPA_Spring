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

		detalles.forEach(dt -> {
			dt.setOrden(ordenGuardada);
			detalleOrdenService.save(dt);
		});

		// Limpiar carrito
		orden = new Orden();
		detalles.clear();

		return "redirect:/usuario/compras/" + ordenGuardada.getId();
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
}