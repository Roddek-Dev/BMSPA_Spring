package com.sena.barberspa.service;

import com.sena.barberspa.model.Orden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalService {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Value("${paypal.success.url}")
    private String successUrl;

    @Value("${paypal.cancel.url}")
    private String cancelUrl;

    // En PayPalService.java, verifica que el método createPayment esté así:

    public Payment createPayment(Orden orden, String cancelUrl, String successUrl) throws PayPalRESTException {
        try {
            APIContext apiContext = new APIContext(clientId, clientSecret, mode);

            // Configurar detalles del pago
            Payment payment = new Payment();
            payment.setIntent("sale");

            // Información del pagador
            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");
            payment.setPayer(payer);

            // Configurar URL de redirección
            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setCancelUrl(cancelUrl);
            redirectUrls.setReturnUrl(successUrl);
            payment.setRedirectUrls(redirectUrls);

            // Detalles del importe
            Amount amount = new Amount();
            amount.setCurrency("MXN");
            amount.setTotal(String.format("%.2f", orden.getTotal()));

            // Descripción de la transacción
            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setDescription("Compra en BarberMusic&Spa - Orden #" + orden.getNumero());

            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);
            payment.setTransactions(transactions);

            // Crear pago en PayPal
            return payment.create(apiContext);
        } catch (PayPalRESTException e) {
            // Registrar el error detalladamente
            System.err.println("Error al crear pago en PayPal: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        APIContext apiContext = new APIContext(clientId, clientSecret, mode);
        return payment.execute(apiContext, paymentExecution);
    }
}