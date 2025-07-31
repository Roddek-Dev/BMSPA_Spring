package com.sena.barberspa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BarberSpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarberSpaApplication.class, args);
		
	}

	@Bean // <-- Con esta anotación, Spring gestionará el objeto
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
