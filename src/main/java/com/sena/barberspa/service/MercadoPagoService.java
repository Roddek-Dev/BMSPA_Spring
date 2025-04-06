package com.sena.barberspa.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.sena.barberspa.model.DetalleOrden;
import com.sena.barberspa.model.Orden;
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

	public String createPreference(Orden orden) throws MPException, MPApiException {
		// Configurar credenciales
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
		PreferenceRequest preferenceRequest = PreferenceRequest.builder().items(items).autoReturn("approved") // "approved"
																												// para
																												// redirección
																												// automática
				.backUrls(backUrls).build();

		PreferenceClient client = new PreferenceClient();
		Preference preference = client.create(preferenceRequest);

		return preference.getInitPoint(); // URL para redirigir al usuario
	}
}