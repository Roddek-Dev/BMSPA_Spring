package com.sena.barberspa.controller;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.sena.barberspa.model.PaymentRequest;
import com.sena.barberspa.service.IOrdenService;
import com.sena.barberspa.service.MercadoPagoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mercadopago")
public class MercadoPagoApiController {

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private IOrdenService ordenService;

    @PostMapping("/process_payment")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody PaymentRequest paymentRequest,
                                                              HttpSession session) {
        try {
            Integer ordenId = (Integer) session.getAttribute("ordenId");
            if (ordenId == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "No se encontró una orden activa")
                );
            }

            // Procesar el pago con Mercado Pago
            Payment payment = mercadoPagoService.createPayment(paymentRequest);

            // Actualizar el estado de la orden
            ordenService.findById(ordenId).ifPresent(orden -> {
                orden.setEstado(payment.getStatus());
                ordenService.update(orden);
            });

            // Devolver toda la información necesaria para las redirecciones
            return ResponseEntity.ok(
                    Map.of(
                            "status", payment.getStatus(),
                            "status_detail", payment.getStatusDetail(),
                            "id", payment.getId()
                    )
            );

        } catch (MPException | MPApiException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar el pago: " + e.getMessage()));
        }
    }
}