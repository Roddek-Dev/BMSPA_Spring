package com.sena.barberspa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SpringBootSecurity {

	@Autowired
	private UserDetailServiceImplement userDetailService;

	@Autowired
	private CustomOAuth2UserService customOAuth2UserService;

	@Bean
	public AuthenticationSuccessHandler successHandler() {
		SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
		handler.setDefaultTargetUrl("/usuario/acceder");
		handler.setUseReferer(true);
		return handler;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.authorizeRequests(authorize -> authorize
						.requestMatchers("/administrador/**").hasRole("ADMIN")
						.requestMatchers("/productos/**").hasRole("ADMIN")
						.requestMatchers("/recordatorios/**").hasRole("ADMIN")
						// Permitir acceso público a la ruta de agendamientos
						.requestMatchers("/agendamientos/save", "/agendamientos/**").permitAll()
						// Permitir acceso a API de servicios para modal de citas
						.requestMatchers("/servicios/sucursales/json").permitAll()
						.requestMatchers("/usuario/resetPassword", "/usuario/cambiarPassword",
								"/usuario/saveNewPassword", "/usuario/token-invalido").permitAll()
						.requestMatchers("/assets/**", "/assetsADMINS/**", "/css/**", "/js/**", "/images/**",
								"/", "/serviciosVista", "/servicioHome/**", "/productosVista", "/productoHome/**",
								"/usuario/registro", "/usuario/login", "/usuario/save", "/error/**").permitAll()
						.anyRequest().authenticated()
				)
				.formLogin(formLogin -> formLogin
						.loginPage("/usuario/login")
						.permitAll()
						.defaultSuccessUrl("/usuario/acceder")
				)
				.oauth2Login(oauth2 -> oauth2
						.loginPage("/usuario/login")
						.userInfoEndpoint(userInfo -> userInfo
								.userService(customOAuth2UserService)
						)
						.successHandler(successHandler())
						.failureUrl("/error/oauth2_error")
				)
				.logout(logout -> logout
						.logoutSuccessUrl("/")
						.permitAll()
				)
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.accessDeniedPage("/error/403")
				)
				.csrf(csrf -> csrf.disable());

		return http.build();
	}
}