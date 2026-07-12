package com.vermeg.jirachatbot.service;

import com.vermeg.jirachatbot.config.GroqConfig;
import com.vermeg.jirachatbot.model.JiraTicket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dedicated service responsible for all communication with the Groq
 * Chat Completions API (OpenAI-compatible endpoint).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroqService {

    private final GroqConfig groqConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public boolean isConfigured() {
        return groqConfig.isConfigured();
    }

    /**
     * Low-level call to the Groq Chat Completions API.
     */
    public String chat(List<Map<String, String>> messages, double temperature, int maxTokens) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqConfig.getKey());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", groqConfig.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(groqConfig.getUrl(), entity, Map.class);

            if (response == null) {
                throw new RuntimeException("Groq API returned an empty response");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("Invalid response from Groq API: missing 'choices'");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null || message.get("content") == null) {
                throw new RuntimeException("Invalid response from Groq API: missing message content");
            }

            return ((String) message.get("content")).trim();

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Groq API 401 Unauthorized: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Groq API authentication failed (401): invalid or missing API key", e);
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("Groq API 403 Forbidden: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Groq API access forbidden (403)", e);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.warn("Groq API 429 Rate limit exceeded: {}", e.getResponseBodyAsString());
                throw new RuntimeException("Groq API rate limit exceeded (429)", e);
            }
            log.error("Groq API HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Groq API error: " + e.getStatusCode(), e);
        } catch (HttpServerErrorException e) {
            log.error("Groq API server error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Groq API server error: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.error("Groq API network error: {}", e.getMessage());
            throw new RuntimeException("Network error while calling Groq API", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error calling Groq API: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error calling Groq API: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a natural language question into a JQL query using Groq.
     */
    public String generateJQL(String naturalLanguage) {
        List<Map<String, String>> messages = List.of(
            Map.of("role", "system", "content",
                "You are a JQL query generator. Convert natural language questions about Jira tickets into JQL (Jira Query Language). Return ONLY the JQL query, no explanations."),
            Map.of("role", "user", "content", buildJqlPrompt(naturalLanguage))
        );
        String jql = chat(messages, 0.3, 150);
        return cleanJql(jql);
    }

    private String buildJqlPrompt(String naturalLanguage) {
        return "Convert this question to JQL: \"" + naturalLanguage + "\"\n" +
               "Examples:\n" +
               "- \"show me open bugs in CRM\" -> \"project = CRM AND issuetype = Bug AND status != Done\"\n" +
               "- \"all high priority issues\" -> \"priority = High\"\n" +
               "- \"issues assigned to john\" -> \"assignee = john\"\n" +
               "- \"my open tasks\" -> \"assignee = currentUser() AND status != Done\"\n" +
               "Return ONLY the JQL query.";
    }

    private String cleanJql(String jql) {
        if (jql == null) {
            return "";
        }
        String cleaned = jql.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("```(json|jql)?", "").trim();
        }
        return cleaned;
    }

    /**
     * Asks Groq to summarize a list of Jira tickets in natural language.
     */
    public String summarizeTickets(List<JiraTicket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return "No tickets found matching your query.";
        }

        StringBuilder ticketSummary = new StringBuilder();
        for (JiraTicket ticket : tickets) {
            String summary = ticket.getFields() != null ? ticket.getFields().getSummary() : "N/A";
            String status = ticket.getFields() != null ? ticket.getFields().getStatusName() : "Unknown";
            ticketSummary.append("- ").append(ticket.getKey()).append(": ")
                    .append(summary != null ? summary : "N/A")
                    .append(" [").append(status).append("]\n");
        }

        List<Map<String, String>> messages = List.of(
            Map.of("role", "system", "content",
                "You are a helpful assistant that summarizes Jira ticket search results concisely."),
            Map.of("role", "user", "content",
                "Summarize these " + tickets.size() + " Jira tickets in 2-3 short sentences:\n" + ticketSummary)
        );

        return chat(messages, 0.5, 300);
    }

    /**
     * Generic conversational reply, used as a fallback / free-form chat mode.
     */
    public String chatFreeform(String userMessage) {
        List<Map<String, String>> messages = List.of(
            Map.of("role", "system", "content",
                "You are a helpful AI assistant integrated into a Jira Chatbot platform. Respond concisely and helpfully."),
            Map.of("role", "user", "content", userMessage)
        );
        return chat(messages, 0.7, 500);
    }
}
