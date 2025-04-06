package com.sena.barberspa.service;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.sena.barberspa.model.Usuario;

import jakarta.servlet.http.HttpSession;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private HttpSession session;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            logger.info("OAuth2 authentication successful. Provider: {}",
                    userRequest.getClientRegistration().getRegistrationId());

            // Obtener información del proveedor
            String provider = userRequest.getClientRegistration().getRegistrationId();
            Map<String, Object> attributes = oauth2User.getAttributes();

            logger.debug("User attributes from {}: {}", provider, attributes);

            // Extraer información del usuario
            String email = extractEmail(oauth2User, provider);
            String name = extractName(oauth2User, provider);

            logger.info("Extracted user info - Email: {}, Name: {}", email, name);

            // Procesar usuario en la base de datos
            Usuario user = processUser(email, name);

            // Establecer sesión
            session.setAttribute("idUsuario", user.getId());
            logger.info("User session established for ID: {}", user.getId());

            // El atributo de nombre puede cambiar dependiendo del proveedor
            String userNameAttributeName = getUserNameAttributeName(provider);

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getTipo())),
                    attributes,
                    userNameAttributeName
            );
        } catch (Exception e) {
            logger.error("Error during OAuth2 authentication: {}", e.getMessage(), e);
            throw e; // Re-lanzar para que sea manejada por el controlador de excepciones
        }
    }

    private String getUserNameAttributeName(String provider) {
        switch (provider) {
            case "google": return "email";
            case "facebook": return "id";
            case "github": return "id";
            default: return "email";
        }
    }

    private String extractEmail(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        try {
            switch (provider) {
                case "google":
                    return attributes.get("email").toString();
                case "facebook":
                    return attributes.containsKey("email") ?
                            attributes.get("email").toString() :
                            "fb_" + attributes.get("id") + "@facebook.com";
                case "github":
                    return attributes.containsKey("email") && attributes.get("email") != null ?
                            attributes.get("email").toString() :
                            "gh_" + attributes.get("login") + "@github.com";
                default:
                    return "user_" + System.currentTimeMillis() + "@unknown.com";
            }
        } catch (Exception e) {
            logger.error("Error extracting email from {} provider: {}", provider, e.getMessage());
            return "error_" + System.currentTimeMillis() + "@example.com";
        }
    }

    private String extractName(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        try {
            switch (provider) {
                case "google":
                    return attributes.get("name").toString();
                case "facebook":
                    return attributes.get("name").toString();
                case "github":
                    return attributes.containsKey("name") && attributes.get("name") != null ?
                            attributes.get("name").toString() :
                            attributes.get("login").toString();
                default:
                    return "Usuario";
            }
        } catch (Exception e) {
            logger.error("Error extracting name from {} provider: {}", provider, e.getMessage());
            return "Usuario " + System.currentTimeMillis();
        }
    }

    private Usuario processUser(String email, String name) {
        try {
            // Buscar si el usuario ya existe
            Usuario user = usuarioService.findByEmail(email).orElse(null);

            if (user == null) {
                // Crear nuevo usuario
                user = new Usuario();
                user.setEmail(email);
                user.setNombre(name);
                user.setDireccion("Por definir");
                user.setTipo("USER");
                user.setPassword(passwordEncoder.encode("oauth_default_password"));
                user.setEstado("ACTIVO");

                user = usuarioService.save(user);
                logger.info("New user created: {}", user.getId());
            } else {
                logger.info("Existing user found: {}", user.getId());
            }

            return user;
        } catch (Exception e) {
            logger.error("Error processing user: {}", e.getMessage(), e);
            throw e;
        }
    }
}