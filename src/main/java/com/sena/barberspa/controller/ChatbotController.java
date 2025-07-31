package com.sena.barberspa.controller;

import org.springframework.beans.factory.annotation.Autowired; // <-- Importante
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    // La URL donde está corriendo tu microservicio de Python
    private final String chatbotServiceUrl = "http://localhost:8000/ask";

    // Inyectamos el RestTemplate gestionado por Spring
    @Autowired
    private final RestTemplate restTemplate;

    // Constructor para la inyección de dependencias
    public ChatbotController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public record ChatbotRequest(String question) {}

    @PostMapping("/ask")
    public ResponseEntity<Map> askChatbot(@RequestBody ChatbotRequest request) {
        // ¡Ya no creamos un nuevo RestTemplate aquí!
        // Lo reutilizamos.

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of("question", request.question());
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(chatbotServiceUrl, entity, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("answer", "Lo siento, el asistente no está disponible en este momento."));
        }
    }
}