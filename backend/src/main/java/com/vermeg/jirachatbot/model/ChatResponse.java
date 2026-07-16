package com.vermeg.jirachatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String type;
    private String message;
    private String jqlQuery;
    private List<JiraTicket> tickets;
    private List<Map<String, Object>> projects;
    private int totalTickets;
    private boolean success;
    private String error;

    public static ChatResponse success(String message, String jqlQuery, List<JiraTicket> tickets) {
        return ChatResponse.builder()
                .type(tickets != null && !tickets.isEmpty() ? "JIRA_ISSUES" : "GENERAL")
                .message(message)
                .jqlQuery(jqlQuery)
                .tickets(tickets)
                .projects(new ArrayList<>())
                .totalTickets(tickets != null ? tickets.size() : 0)
                .success(true)
                .build();
    }

    public static ChatResponse successWithProjects(String message, List<Map<String, Object>> projects) {
        return ChatResponse.builder()
                .type(projects != null && !projects.isEmpty() ? "JIRA_PROJECTS" : "GENERAL")
                .message(message)
                .jqlQuery(null)
                .tickets(new ArrayList<>())
                .projects(projects)
                .totalTickets(0)
                .success(true)
                .build();
    }

    public static ChatResponse general(String message) {
        return ChatResponse.builder()
                .type("GENERAL")
                .message(message)
                .jqlQuery(null)
                .tickets(new ArrayList<>())
                .projects(new ArrayList<>())
                .totalTickets(0)
                .success(true)
                .build();
    }

    public static ChatResponse error(String error) {
        return ChatResponse.builder()
                .type("ERROR")
                .message("An error occurred")
                .error(error)
                .success(false)
                .build();
    }
}
