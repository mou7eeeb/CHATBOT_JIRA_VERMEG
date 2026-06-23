package com.vermeg.jirachatbot.service;

import com.vermeg.jirachatbot.model.ChatRequest;
import com.vermeg.jirachatbot.model.ChatResponse;
import com.vermeg.jirachatbot.model.JiraTicket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final AIService aiService;
    private final JiraService jiraService;
    
    public ChatResponse processMessage(ChatRequest request) {
        log.info("Processing chat message: {}", request.getMessage());
        
        try {
            String jqlQuery = aiService.convertToJQL(request.getMessage());
            
            List<JiraTicket> tickets = jiraService.searchTickets(jqlQuery);
            
            String message = buildResponseMessage(tickets.size());
            
            return ChatResponse.success(message, jqlQuery, tickets);
            
        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage(), e);
            return ChatResponse.error(e.getMessage());
        }
    }
    
    private String buildResponseMessage(int ticketCount) {
        if (ticketCount == 0) {
            return "No tickets found matching your query.";
        } else if (ticketCount == 1) {
            return "Found 1 ticket matching your query.";
        } else {
            return String.format("Found %d tickets matching your query.", ticketCount);
        }
    }
}
