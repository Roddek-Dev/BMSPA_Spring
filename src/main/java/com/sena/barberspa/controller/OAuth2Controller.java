package com.sena.barberspa.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @GetMapping("/success")
    public String handleOAuthSuccess(Authentication authentication) {
        // La sesión ya ha sido configurada en CustomOAuth2UserService
        // Aquí puedes hacer cualquier procesamiento adicional si es necesario

        // Redirigir al usuario a la página principal
        return "redirect:/";
    }
}