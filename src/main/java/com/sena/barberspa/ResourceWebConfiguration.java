package com.sena.barberspa;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceWebConfiguration implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Opción 1: Directorio en el home del usuario (recomendado)
		String externalPath = "file:" + System.getProperty("user.home") + "/barberspa/images/";

		// Opción 2: Directorio en /tmp
		// String externalPath = "file:/tmp/barberspa/images/";

		// Opción 3: Directorio relativo al proyecto
		// String externalPath = "file:./uploads/images/";

		// Opción 4: Usando variable de entorno
		// String basePath = System.getenv("BARBERSPA_IMAGES_PATH") != null ?
		//     System.getenv("BARBERSPA_IMAGES_PATH") : System.getProperty("user.home") + "/barberspa/images/";
		// String externalPath = "file:" + basePath;

		registry.addResourceHandler("/images/**").addResourceLocations(externalPath);

		// También puedes agregar recursos estáticos adicionales
		registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
	}
}