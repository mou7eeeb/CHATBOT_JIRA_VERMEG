package com.vermeg.jirachatbot.controller;

import com.vermeg.jirachatbot.dto.JiraConnectionDTO;
import com.vermeg.jirachatbot.dto.MessageResponse;
import com.vermeg.jirachatbot.service.JiraConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/jira-connections")
@RequiredArgsConstructor
public class JiraConnectionController {
    
    private final JiraConnectionService jiraConnectionService;
    
    @PostMapping
    public ResponseEntity<?> createConnection(@Valid @RequestBody JiraConnectionDTO dto) {
        try {
            JiraConnectionDTO created = jiraConnectionService.createConnection(dto);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating connection: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<JiraConnectionDTO>> getAllConnections() {
        return ResponseEntity.ok(jiraConnectionService.getAllConnections());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getConnection(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(jiraConnectionService.getConnection(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateConnection(@PathVariable Long id, @Valid @RequestBody JiraConnectionDTO dto) {
        try {
            return ResponseEntity.ok(jiraConnectionService.updateConnection(id, dto));
        } catch (Exception e) {
            log.error("Error updating connection: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConnection(@PathVariable Long id) {
        try {
            jiraConnectionService.deleteConnection(id);
            return ResponseEntity.ok(new MessageResponse("Connection deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/test")
    public ResponseEntity<?> testConnection(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(jiraConnectionService.testConnection(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
