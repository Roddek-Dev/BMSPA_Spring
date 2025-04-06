package com.sena.barberspa.controller;

import com.sena.barberspa.model.Orden;
import com.sena.barberspa.service.IOrdenService;
import com.sena.barberspa.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.paypal.api.payments.*;
import com.paypal.base.rest.*;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paypal")
public class PayPalApiController {

    @Autowired
    private IOrdenService ordenService;

    @Autowired
    private IUsuarioService usuarioService;

    private final String CLIENT_ID = "Af2aprPDlFQMpXUr6Ak8e2bzlWjL_QctjCzKvuiyDkQYY3QTLwYXOgusOv63mxqr1vPQ8kh6cE0XTYOP";
    private final String CLIENT_SECRET = "EG5icBw6Rw1eIpWsxjQOc17FwvqpXIt9Wqgr2nwHRAWG_KjxqweKxKt6Lr0cFpDPTUH9zyaOMerLwp7z";
    private final String MODE = "sandbox";

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload, HttpSession session) {
        try {
            // Agregar logs para depuración
            System.out.println("Recibida solicitud para crear orden PayPal: " + payload);

            // Preparar orden para PayPal
            Double total = Double.parseDouble(payload.get("total").toString());
            System.out.println("Total de la orden: " + total);

            // Crear contexto API de PayPal
            APIContext apiContext = new APIContext(CLIENT_ID, CLIENT_SECRET, MODE);

            // Verificar configuración
            System.out.println("Modo PayPal: " + MODE);
            System.out.println("Client ID: " + CLIENT_ID.substring(0, 10) + "...");

            // Crear objeto de pago PayPal
            Payment payment = new Payment();
            payment.setIntent("sale");

            // Información del pagador
            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");
            payment.setPayer(payer);

            // Configurar redirección después del pago
            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setCancelUrl("http://localhost:63106/paypal/cancel");
            redirectUrls.setReturnUrl("http://localhost:63106/paypal/success");
            payment.setRedirectUrls(redirectUrls);

            // Configurar transacción
            Amount amount = new Amount();
            amount.setCurrency("MXN");
            amount.setTotal(String.format("%.2f", total));

            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setDescription("Compra en BarberMusic&Spa");

            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);
            payment.setTransactions(transactions);

            // Crear pago en PayPal
            Payment createdPayment = payment.create(apiContext);
            System.out.println("Pago creado en PayPal con ID: " + createdPayment.getId());

            // Guardar la orden en la base de datos - esto debería ser un método separado
            Integer idUsuario = (Integer) session.getAttribute("idUsuario");
            if (idUsuario != null) {
                System.out.println("Usuario autenticado con ID: " + idUsuario);
                Integer ordenId = (Integer) session.getAttribute("ordenId");

                if (ordenId != null) {
                    System.out.println("Orden ID desde sesión: " + ordenId);
                    ordenService.findById(ordenId).ifPresent(orden -> {
                        orden.setEstado("PROCESANDO");
                        ordenService.update(orden);
                        System.out.println("Orden actualizada a estado PROCESANDO");
                    });
                } else {
                    System.out.println("No se encontró orden en la sesión");
                }
            } else {
                System.out.println("No se encontró usuario en sesión");
            }

            // Buscar el enlace de aprobación
            String approvalUrl = null;
            for (Links link : createdPayment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    approvalUrl = link.getHref();
                    break;
                }
            }

            if (approvalUrl == null) {
                System.err.println("No se encontró URL de aprobación en la respuesta de PayPal.");
                throw new Exception("No se pudo obtener el enlace de aprobación de PayPal.");
            }

            Map<String, String> result = new HashMap<>();
            result.put("id", createdPayment.getId());
            result.put("approval_url", approvalUrl);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("Error al crear orden en PayPal: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @PostMapping("/capture-order")
    public ResponseEntity<?> captureOrder(@RequestBody Map<String, String> payload, HttpSession session) {
        try {
            String paymentId = payload.get("paymentId");
            String payerId = payload.get("PayerID");

            if (paymentId == null || payerId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Se requiere paymentId y PayerID");
                return ResponseEntity.badRequest().body(error);
            }

            // Crear contexto API de PayPal
            APIContext apiContext = new APIContext(CLIENT_ID, CLIENT_SECRET, MODE);

            // Obtener detalles del pago
            Payment payment = Payment.get(apiContext, paymentId);

            // Ejecutar el pago
            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);

            Payment executedPayment = payment.execute(apiContext, paymentExecution);

            // Verificar estado del pago
            if (executedPayment.getState().equals("approved")) {
                // Actualizar el estado de la orden
                Integer idOrden = (Integer) session.getAttribute("ordenId");
                if (idOrden != null) {
                    ordenService.findById(idOrden).ifPresent(orden -> {
                        orden.setEstado("PAGADO");
                        ordenService.update(orden);
                    });
                }

                Map<String, String> result = new HashMap<>();
                result.put("id", executedPayment.getId());
                result.put("status", executedPayment.getState());

                return ResponseEntity.ok(result);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El pago no fue aprobado");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}