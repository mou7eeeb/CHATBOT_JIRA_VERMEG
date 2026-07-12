package com.vermeg.jirachatbot.controller;

import com.vermeg.jirachatbot.model.ChatRequest;
import com.vermeg.jirachatbot.model.ChatResponse;
import com.vermeg.jirachatbot.service.ChatService;
import com.vermeg.jirachatbot.service.IntelligentChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final IntelligentChatService intelligentChatService;
    
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("Received chat request: {}", request.getMessage());
        
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ChatResponse.error("Message cannot be empty"));
        }
        
        ChatResponse response = chatService.processMessage(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/chat/ai")
    public ResponseEntity<ChatResponse> intelligentChat(@RequestBody ChatRequest request) {
        log.info("Received intelligent chat request: {}", request.getMessage());
        log.info("Request object: {}", request);

        try {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                log.warn("Message is empty");
                return ResponseEntity.badRequest()
                    .body(ChatResponse.error("Message cannot be empty"));
            }

            log.info("Calling intelligentChatService.chat()");
            ChatResponse response = intelligentChatService.chat(request);
            log.info("Service returned response: {}", response);

            // Return 200 OK even for error responses - errors are handled in the response body
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in intelligentChat endpoint: {}", e.getMessage(), e);
            return ResponseEntity.ok()
                .body(ChatResponse.error("Server error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Jira Chatbot API is running");
    }
}
