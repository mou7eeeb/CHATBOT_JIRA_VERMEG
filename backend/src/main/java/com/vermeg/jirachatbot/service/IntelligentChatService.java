package com.vermeg.jirachatbot.service;

import com.vermeg.jirachatbot.config.GoogleAIConfig;
import com.vermeg.jirachatbot.model.ChatRequest;
import com.vermeg.jirachatbot.model.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntelligentChatService {
    
    private final GoogleAIConfig googleAIConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    
    public ChatResponse chat(ChatRequest request) {
        log.info("Processing intelligent chat message: {}", request.getMessage());
        log.info("Google AI Config - Key: {}, URL: {}, Model: {}",
            googleAIConfig.getKey() != null ? googleAIConfig.getKey().substring(0, Math.min(10, googleAIConfig.getKey().length())) + "..." : "null",
            googleAIConfig.getUrl(),
            googleAIConfig.getModel());
        log.info("Google AI is configured: {}", googleAIConfig.isConfigured());

        try {
            String response;

            // Use Google AI API if configured, otherwise fall back to demo mode
            if (googleAIConfig.isConfigured()) {
                log.info("Google AI is configured, using real AI response");
                response = getAIResponse(request.getMessage());
            } else {
                log.info("Google AI not configured, using demo mode");
                response = generateSmartResponse(request.getMessage());
            }
            
            return ChatResponse.builder()
                    .success(true)
                    .message(response)
                    .jqlQuery(null)
                    .tickets(new ArrayList<>())
                    .build();
            
        } catch (Exception e) {
            log.error("Error processing intelligent chat: {}", e.getMessage(), e);
            return ChatResponse.error("Sorry, I encountered an error: " + e.getMessage());
        }
    }
    
    private String generateSmartResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        
        // Réponses contextuelles intelligentes
        if (lowerMessage.contains("bonjour") || lowerMessage.contains("hello") || lowerMessage.contains("salut")) {
            return "Bonjour ! Je suis votre assistant IA intelligent. Je peux vous aider avec diverses questions. Comment puis-je vous aider aujourd'hui ?";
        }
        
        if (lowerMessage.contains("comment") && lowerMessage.contains("vas")) {
            return "Je vais très bien, merci ! Je suis prêt à répondre à vos questions. Que souhaitez-vous savoir ?";
        }
        
        if (lowerMessage.contains("qui es-tu") || lowerMessage.contains("qui es tu") || lowerMessage.contains("c'est quoi")) {
            return "Je suis un assistant IA intelligent intégré à votre plateforme Jira Chatbot. Je suis conçu pour vous aider à répondre à vos questions et vous assister dans vos tâches quotidiennes.";
        }
        
        if (lowerMessage.contains("merci")) {
            return "De rien ! N'hésitez pas si vous avez d'autres questions. Je suis là pour vous aider !";
        }
        
        if (lowerMessage.contains("aide") || lowerMessage.contains("help")) {
            return "Je peux vous aider de plusieurs façons :\n" +
                   "- Répondre à vos questions générales\n" +
                   "- Vous fournir des informations\n" +
                   "- Vous assister dans vos recherches\n" +
                   "N'hésitez pas à me poser vos questions !";
        }
        
        // Réponse par défaut intelligente
        return "Vous avez dit : \"" + userMessage + "\". " +
               "Je suis votre assistant IA en mode démo. Pour activer l'IA complète avec OpenAI GPT, " +
               "veuillez configurer une clé API valide dans les paramètres. " +
               "En attendant, je peux répondre à des questions simples. Comment puis-je vous aider ?";
    }
    
    private String getAIResponse(String userMessage) {
        try {
            log.info("Calling Google AI API with message: {}", userMessage);
            String urlWithKey = googleAIConfig.getUrl() + "?key=" + googleAIConfig.getKey();
            log.info("Google AI API URL: {}", urlWithKey);
            log.info("Google AI Model: {}", googleAIConfig.getModel());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", userMessage)
                ))
            ));
            requestBody.put("generationConfig", Map.of(
                "temperature", 0.7,
                "maxOutputTokens", 500
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                urlWithKey,
                entity,
                Map.class
            );

            if (response != null && response.containsKey("candidates")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    if (candidate.containsKey("content")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                        if (content.containsKey("parts")) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                            if (!parts.isEmpty()) {
                                Map<String, Object> part = parts.get(0);
                                String aiResponse = ((String) part.get("text")).trim();
                                log.info("AI Response: {}", aiResponse);
                                return aiResponse;
                            }
                        }
                    }
                }
            }

            return "I'm sorry, I couldn't generate a response. Please try again.";

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Google AI API HTTP Error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 429) {
                log.warn("Google AI quota exceeded, falling back to demo mode");
                return generateSmartResponse(userMessage);
            }
            throw new RuntimeException("Google AI API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("Google AI API Server Error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Google AI API server error: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Error calling Google AI API: {}", e.getMessage(), e);
            log.error("Error class: {}", e.getClass().getName());
            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("Failed to get AI response: " + e.getMessage());
        }
    }
}
