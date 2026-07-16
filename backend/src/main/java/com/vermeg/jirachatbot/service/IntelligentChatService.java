package com.vermeg.jirachatbot.service;

import com.vermeg.jirachatbot.model.ChatRequest;
import com.vermeg.jirachatbot.model.ChatResponse;
import com.vermeg.jirachatbot.model.JiraTicket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntelligentChatService {

    private final GroqService groqService;
    private final JiraToolService jiraToolService;
    private final IntentDetectionService intentDetectionService;

    public ChatResponse chat(ChatRequest request) {
        log.info("=== Starting Intelligent Chat Request ===");
        log.info("User Message: {}", request.getMessage());
        log.info("Groq is configured: {}", groqService.isConfigured());
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Detect user intent
            log.info("Step 1: Detecting user intent");
            IntentDetectionService.Intent intent = intentDetectionService.detectIntent(request.getMessage());
            log.info("Detected intent: {}", intent);

            // Step 2: Route based on intent
            log.info("Step 2: Routing based on intent");
            ChatResponse response;
            if (intent == IntentDetectionService.Intent.JIRA_DATA) {
                log.info("Routing to Jira data handler");
                response = handleJiraDataRequest(request);
            } else {
                log.info("Default: Routing to general knowledge handler");
                response = handleGeneralKnowledgeRequest(request);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("=== Chat Request Completed Successfully ===");
            log.info("Total Execution Time: {}ms", executionTime);
            log.info("Response Success: {}", response.isSuccess());

            return response;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("=== Chat Request Failed ===");
            log.error("Error: {}", e.getMessage(), e);
            log.error("Execution Time: {}ms", executionTime);

            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("No active Jira connection")) {
                log.info("No Jira connection error, returning friendly message");
                return ChatResponse.error(errorMessage);
            }
            return ChatResponse.error("Sorry, I encountered an error: " + e.getMessage());
        }
    }

    private ChatResponse handleJiraDataRequest(ChatRequest request) {
        log.info("=== Handling Jira Data Request ===");
        String userMessage = request.getMessage().toLowerCase();
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Call appropriate Jira API based on user intent
            log.info("Step 1: Retrieving Jira data");
            Object jiraData = retrieveJiraData(userMessage);
            log.info("Jira data retrieved successfully");

            // Step 2: Use AI to summarize/explain the data
            log.info("Step 2: Processing Jira data with AI");
            String response;
            if (groqService.isConfigured()) {
                log.info("Using Groq for summarization");
                response = summarizeJiraDataWithAI(jiraData, request.getMessage());
            } else {
                log.info("Groq not configured, using default formatting");
                response = formatJiraData(jiraData);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("=== Jira Data Request Completed ===");
            log.info("Execution Time: {}ms", executionTime);

            // Return structured response with actual Jira data
            if (jiraData instanceof List) {
                List<?> dataList = (List<?>) jiraData;
                if (!dataList.isEmpty() && dataList.get(0) instanceof com.vermeg.jirachatbot.model.JiraTicket) {
                    // It's a list of tickets
                    @SuppressWarnings("unchecked")
                    List<com.vermeg.jirachatbot.model.JiraTicket> tickets = (List<com.vermeg.jirachatbot.model.JiraTicket>) jiraData;

                    // Apply deterministic filtering based on user request
                    com.vermeg.jirachatbot.model.JiraSearchCriteria criteria = intentDetectionService.extractSearchCriteria(request.getMessage());
                    if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
                        String targetStatus = criteria.getStatus();
                        log.info("Applying status filter: {}", targetStatus);
                        log.info("Tickets before filter: {}", tickets.size());

                        tickets = tickets.stream()
                                .filter(ticket -> {
                                    String ticketStatus = ticket.getFields().getStatusName();
                                    boolean matches = ticketStatus.equalsIgnoreCase(targetStatus);
                                    if (!matches) {
                                        log.debug("Filtering out ticket {} with status {}", ticket.getKey(), ticketStatus);
                                    }
                                    return matches;
                                })
                                .collect(java.util.stream.Collectors.toList());

                        log.info("Tickets after filter: {}", tickets.size());
                    }

                    return ChatResponse.success(response, null, tickets);
                } else if (!dataList.isEmpty() && dataList.get(0) instanceof Map) {
                    // It's a list of projects or other data
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> projects = (List<Map<String, Object>>) jiraData;
                    return ChatResponse.successWithProjects(response, projects);
                }
            }

            return ChatResponse.general(response);

        } catch (RuntimeException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("=== Jira Data Request Failed ===");
            log.error("Error: {}", e.getMessage(), e);
            log.error("Execution Time: {}ms", executionTime);

            String errorMessage = e.getMessage();
            // If the error is about no Jira connection, fall back to general knowledge
            if (errorMessage != null && errorMessage.contains("No active Jira connection")) {
                log.info("No Jira connection, falling back to general knowledge mode");
                return handleGeneralKnowledgeRequest(request);
            }
            // For other Jira errors, return a friendly message without crashing
            log.info("Jira error occurred, returning friendly error message");
            return ChatResponse.error(errorMessage);
        }
    }

    private Object retrieveJiraData(String userMessage) {
        log.info("=== Retrieving Jira Data ===");
        log.info("User message: {}", userMessage);

        // Profile information
        if (userMessage.contains("profile") || userMessage.contains("my information") || userMessage.contains("my jira")) {
            log.info("Retrieving user profile");
            return jiraToolService.getCurrentUser();
        }

        // Projects
        if (userMessage.contains("projects") || userMessage.contains("my projects")) {
            log.info("Retrieving projects");
            return jiraToolService.getProjects();
        }

        // Boards
        if (userMessage.contains("board") || userMessage.contains("boards")) {
            log.info("Retrieving boards");
            return jiraToolService.getBoards();
        }

        // Sprints (requires board ID, simplified for now)
        if (userMessage.contains("sprint") || userMessage.contains("sprints")) {
            log.info("Retrieving sprints");
            List<Map<String, Object>> boards = jiraToolService.getBoards();
            if (!boards.isEmpty()) {
                Long firstBoardId = (Long) boards.get(0).get("id");
                return jiraToolService.getSprints(firstBoardId);
            }
            return List.of();
        }

        // Issue key pattern (e.g., PROJ-123)
        if (userMessage.matches(".*[A-Z]+-\\d+.*")) {
            String issueKey = extractIssueKey(userMessage);
            if (issueKey != null) {
                log.info("Retrieving specific issue: {}", issueKey);
                return jiraToolService.getIssue(issueKey);
            }
        }

        // Default: Use JQL search with proper filtering
        String jql = intentDetectionService.generateJQLFromIntent(userMessage);
        log.info("Generated JQL: {}", jql);
        if (jql != null) {
            List<com.vermeg.jirachatbot.model.JiraTicket> results = jiraToolService.executeJQL(jql);
            log.info("JQL returned {} tickets", results.size());
            if (!results.isEmpty()) {
                log.info("Ticket statuses in results: {}",
                    results.stream()
                        .map(t -> t.getFields().getStatusName())
                        .distinct()
                        .toList());
            }
            return results;
        }

        // Fallback to current user's issues
        log.info("Using fallback JQL: assignee = currentUser()");
        return jiraToolService.executeJQL("assignee = currentUser()");
    }

    private String extractIssueKey(String message) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[A-Z]+-\\d+");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String summarizeJiraDataWithAI(Object jiraData, String originalQuestion) {
        String dataContext = convertJiraDataToString(jiraData);
        String prompt = String.format(
                "User asked: %s\n\nHere is the relevant Jira data:\n%s\n\n" +
                "IMPORTANT: Return only a short introductory summary (1-2 sentences max).\n" +
                "Do NOT list each Jira issue or project individually.\n" +
                "Do NOT repeat issue keys, summaries, statuses, or priorities.\n" +
                "Do NOT format as a bulleted list.\n" +
                "The frontend will display the structured Jira items separately in a table.\n" +
                "Just provide a brief summary like 'I found X items matching your request.'",
                originalQuestion,
                dataContext
        );
        return groqService.chatFreeform(prompt);
    }

    private String convertJiraDataToString(Object jiraData) {
        if (jiraData instanceof Map) {
            return formatMap((Map<?, ?>) jiraData);
        } else if (jiraData instanceof List) {
            List<?> list = (List<?>) jiraData;
            if (list.isEmpty()) {
                return "No data found.";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(list.size(), 10); i++) {
                sb.append(formatItem(list.get(i))).append("\n");
            }
            if (list.size() > 10) {
                sb.append("... and ").append(list.size() - 10).append(" more items.");
            }
            return sb.toString();
        }
        return jiraData.toString();
    }

    private String formatMap(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    private String formatItem(Object item) {
        if (item instanceof com.vermeg.jirachatbot.model.JiraTicket) {
            com.vermeg.jirachatbot.model.JiraTicket ticket = (com.vermeg.jirachatbot.model.JiraTicket) item;
            return String.format("[%s] %s - Status: %s, Priority: %s",
                    ticket.getKey(),
                    ticket.getFields().getSummary(),
                    ticket.getFields().getStatusName(),
                    ticket.getFields().getPriorityName());
        }
        if (item instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) item;
            Object key = map.get("key");
            Object name = map.get("name");
            Object summary = map.get("summary");
            if (key != null) {
                return String.format("[%s] %s", key, summary != null ? summary : (name != null ? name : ""));
            }
            return map.toString();
        }
        return item.toString();
    }

    private String formatJiraData(Object jiraData) {
        String dataString = convertJiraDataToString(jiraData);
        return "Here's what I found in Jira:\n\n" + dataString + "\n\n(Note: Enable Groq API for AI-powered summaries)";
    }

    private ChatResponse handleGeneralKnowledgeRequest(ChatRequest request) {
        log.info("Handling general knowledge request");

        try {
            String response;
            if (groqService.isConfigured()) {
                response = groqService.chatFreeform(request.getMessage());
            } else {
                response = generateSmartResponse(request.getMessage());
            }

            return ChatResponse.general(response);

        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("429")) {
                log.warn("Groq rate limit exceeded, falling back to demo mode");
                return ChatResponse.general(generateSmartResponse(request.getMessage()));
            }
            log.error("Error calling Groq API: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String generateSmartResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        // Réponses contextuelles intelligentes
        if (lowerMessage.contains("bonjour") || lowerMessage.contains("hello") || lowerMessage.contains("salut")) {
            return "Bonjour ! Je suis votre assistant IA intelligent connecté à Jira. Je peux vous aider avec vos tickets, projets, et questions sur Jira. Comment puis-je vous aider aujourd'hui ?";
        }

        if (lowerMessage.contains("comment") && lowerMessage.contains("vas")) {
            return "Je vais très bien, merci ! Je suis prêt à vous aider avec vos données Jira. Que souhaitez-vous savoir ?";
        }

        if (lowerMessage.contains("qui es-tu") || lowerMessage.contains("qui es tu") || lowerMessage.contains("c'est quoi")) {
            return "Je suis un assistant IA intelligent connecté à votre Jira. Je peux récupérer vos tickets, projets, et informations de profil en temps réel depuis Jira.";
        }

        if (lowerMessage.contains("merci")) {
            return "De rien ! N'hésitez pas si vous avez d'autres questions sur vos données Jira.";
        }

        if (lowerMessage.contains("aide") || lowerMessage.contains("help")) {
            return "Je peux vous aider avec :\n" +
                   "- Vos tickets et issues Jira\n" +
                   "- Vos projets\n" +
                   "- Votre profil Jira\n" +
                   "- Vos boards et sprints\n" +
                   "- Questions générales sur Jira, Scrum, Agile\n" +
                   "Essayez de demander : \"Montre mes tickets\" ou \"Quels sont mes projets ?\"";
        }

        // Réponse par défaut intelligente
        return "Vous avez dit : \"" + userMessage + "\". " +
               "Je suis votre assistant IA connecté à Jira. Pour activer l'IA complète avec Groq, " +
               "veuillez configurer une clé API valide. En attendant, essayez de me poser des questions sur vos données Jira.";
    }
}
