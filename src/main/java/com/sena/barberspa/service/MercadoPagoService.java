package com.sena.barberspa.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.payment.PaymentMethod;
import com.mercadopago.resources.payment.PaymentStatus;
import com.mercadopago.client.common.IdentificationRequest;
import com.sena.barberspa.model.DetalleOrden;
import com.sena.barberspa.model.Orden;
import com.sena.barberspa.model.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${mercadopago.success.url}")
    private String successUrl;

    @Value("${mercadopago.failure.url}")
    private String failureUrl;

    @Value("${mercadopago.pending.url}")
    private String pendingUrl;

    // Método existente para Checkout Pro (se mantiene)
    public String createPreference(Orden orden) throws MPException, MPApiException {
        // Tu código actual de createPreference...
        MercadoPagoConfig.setAccessToken(accessToken);

        // Crear items de preferencia
        List<PreferenceItemRequest> items = new ArrayList<>();

        for (DetalleOrden detalle : orden.getDetalle()) {
            PreferenceItemRequest item = PreferenceItemRequest.builder().title(detalle.getNombre())
                    .quantity(detalle.getCantidad().intValue()).unitPrice(new BigDecimal(detalle.getPrecio())).build();
            items.add(item);
        }

        // Configurar URLs de retorno
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder().success(successUrl).failure(failureUrl)
                .pending(pendingUrl).build();

        // Crear preferencia
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .autoReturn("approved")
                .backUrls(backUrls)
                .externalReference(orden.getId().toString())  // Añadir referencia externa
                .build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);

        return preference.getInitPoint(); // URL para redirigir al usuario
    }

    // Nuevo método para Checkout API
    public Payment createPayment(PaymentRequest paymentRequest) throws MPException, MPApiException {
        MercadoPagoConfig.setAccessToken(accessToken);

        PaymentClient client = new PaymentClient();

        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(paymentRequest.getTransactionAmount())
                .token(paymentRequest.getToken())
                .description(paymentRequest.getDescription())
                .installments(paymentRequest.getInstallments())
                .paymentMethodId(paymentRequest.getPaymentMethodId())
                .payer(
                        PaymentPayerRequest.builder()
                                .email(paymentRequest.getPayer().getEmail())
                                .identification(
                                        IdentificationRequest.builder()
                                                .type(paymentRequest.getPayer().getIdentification().getType())
                                                .number(paymentRequest.getPayer().getIdentification().getNumber())
                                                .build()
                                )
                                .build()
                )
                .build();

        return client.create(request);
    }
}