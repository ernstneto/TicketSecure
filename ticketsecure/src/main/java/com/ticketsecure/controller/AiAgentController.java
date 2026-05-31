package com.ticketsecure.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketsecure.dto.ChatResponseDTO;
import com.ticketsecure.service.AiAgentService;

import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AiAgentController {

    private final AiAgentService aiAgentService;

    public AiAgentController(AiAgentService aiAgentService) {
        this.aiAgentService = aiAgentService;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDTO> chatWithAgent(@RequestBody Map<String, Object> payload, HttpSession session) {
        Object rawMessage = payload.get("mensagem");
        String userMessage = rawMessage != null ? rawMessage.toString() : null;

        if (userMessage == null || userMessage.isBlank()) {
            return ResponseEntity.badRequest().body(new ChatResponseDTO("Mensagem não pode ser vazia."));
        }

        Double latitude = parseDouble(payload.get("latitude"));
        Double longitude = parseDouble(payload.get("longitude"));

        String sessionId = session.getId();
        ChatResponseDTO response = aiAgentService.processChat(sessionId, userMessage, latitude, longitude);
        return ResponseEntity.ok(response);
    }

    private Double parseDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
