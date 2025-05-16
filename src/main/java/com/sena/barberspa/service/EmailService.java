package com.sena.barberspa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.sena.barberspa.controller.UsuarioController;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.UUID;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private UsuarioController usuarioController;

    /**
     * Envía un correo simple
     */
    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    /**
     * Envía un correo HTML utilizando una plantilla Thymeleaf
     */
    public void sendHtmlEmail(String to, String subject, String templateName, Context context) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);

        String htmlContent = templateEngine.process(templateName, context);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * Envía un correo para restablecer la contraseña
     */
    public void sendPasswordResetEmail(String email) {
        try {
            // Generar token único para restablecimiento
            String token = UUID.randomUUID().toString();

            // Almacenar el token (en este caso, en el mapa de tokens del controlador)
            // En una implementación real, esto debería ir a la base de datos
            storeToken(email, token);

            // Crear contexto para la plantilla
            Context context = new Context();
            context.setVariable("resetLink", "http://localhost:63106/usuario/cambiarPassword?token=" + token);
            context.setVariable("email", email);

            // Enviar correo usando plantilla
            sendHtmlEmail(
                    email,
                    "Restablecimiento de Contraseña - BarberMusic&Spa",
                    "emails/reset-password",
                    context
            );
        } catch (Exception e) {
            // Manejar excepción
            e.printStackTrace();
        }
    }

    // Método para almacenar el token en el mapa
    private void storeToken(String email, String token) {
        // Llamar al método estático de UsuarioController para almacenar el token
        UsuarioController.storeResetToken(token, email);
    }
}