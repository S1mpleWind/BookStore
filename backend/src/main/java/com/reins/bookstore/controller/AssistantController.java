package com.reins.bookstore.controller;

import com.reins.bookstore.dto.request.AssistantChatRequest;
import com.reins.bookstore.dto.response.AssistantChatResponse;
import com.reins.bookstore.service.AssistantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/assistant")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AssistantController {
    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody AssistantChatRequest request) {
        try {
            return ResponseEntity.ok(assistantService.chat(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "AI 服务暂时不可用，请稍后重试"));
        }
    }
}
