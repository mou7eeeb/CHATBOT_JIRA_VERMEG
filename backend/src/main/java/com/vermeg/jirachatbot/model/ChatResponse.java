package com.vermeg.jirachatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    private String message;
    
    private String jqlQuery;
    
    private List<JiraTicket> tickets;
    
    private int totalTickets;
    
    private boolean success;
    
    private String error;
    
    public static ChatResponse success(String message, String jqlQuery, List<JiraTicket> tickets) {
        return ChatResponse.builder()
                .message(message)
                .jqlQuery(jqlQuery)
                .tickets(tickets)
                .totalTickets(tickets != null ? tickets.size() : 0)
                .success(true)
                .build();
    }
    
    public static ChatResponse error(String error) {
        return ChatResponse.builder()
                .message("An error occurred")
                .error(error)
                .success(false)
                .build();
    }
}
