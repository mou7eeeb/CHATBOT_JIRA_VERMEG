package com.vermeg.jirachatbot.controller;

import com.vermeg.jirachatbot.dto.ChatMessageDTO;
import com.vermeg.jirachatbot.dto.ChatSessionDTO;
import com.vermeg.jirachatbot.dto.CreateChatSessionDTO;
import com.vermeg.jirachatbot.dto.MessageResponse;
import com.vermeg.jirachatbot.dto.UpdateChatSessionDTO;
import com.vermeg.jirachatbot.service.ChatSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @PostMapping
    public ResponseEntity<?> createSession(@Valid @RequestBody CreateChatSessionDTO dto) {
        try {
            return ResponseEntity.ok(chatSessionService.createSession(dto));
        } catch (Exception e) {
            log.error("Error creating session: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/auto-title")
    public ResponseEntity<?> createSessionWithAutoTitle(@RequestBody CreateChatSessionDTO dto) {
        try {
            return ResponseEntity.ok(chatSessionService.createSessionWithAutoTitle(dto.getTitle(), dto.getJiraConnectionId()));
        } catch (Exception e) {
            log.error("Error creating session with auto title: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ChatSessionDTO>> getAllSessions() {
        return ResponseEntity.ok(chatSessionService.getAllSessions());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ChatSessionDTO>> getActiveSessions() {
        return ResponseEntity.ok(chatSessionService.getActiveSessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSession(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(chatSessionService.getSession(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateSession(@PathVariable Long id, @Valid @RequestBody UpdateChatSessionDTO dto) {
        try {
            return ResponseEntity.ok(chatSessionService.updateSession(id, dto));
        } catch (Exception e) {
            log.error("Error updating session: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        try {
            chatSessionService.deleteSession(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting session: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<?> addMessage(@PathVariable Long id, @Valid @RequestBody ChatMessageDTO messageDTO) {
        try {
            return ResponseEntity.ok(chatSessionService.addMessage(id, messageDTO));
        } catch (Exception e) {
            log.error("Error adding message: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(chatSessionService.getMessages(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
