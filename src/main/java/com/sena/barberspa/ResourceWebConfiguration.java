package com.sena.barberspa;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceWebConfiguration implements WebMvcConfigurer {
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// ruta absoluta para windows 
		String externalPath = "file:C:/images/";
		registry.addResourceHandler("/images/**").addResourceLocations(externalPath);
		// registry.addResourceHandler("/images/**").addResourceLocations("file:images/");
	}

}
 