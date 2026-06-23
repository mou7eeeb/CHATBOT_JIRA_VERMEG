package com.vermeg.jirachatbot.service;

import com.vermeg.jirachatbot.config.OpenAIConfig;
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
    
    private final OpenAIConfig openAIConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    
    public ChatResponse chat(ChatRequest request) {
        log.info("Processing intelligent chat message: {}", request.getMessage());
        
        try {
            // Réponse intelligente simulée - Mode démo
            String response = generateSmartResponse(request.getMessage());
            
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
            log.info("Calling OpenAI API with message: {}", userMessage);
            log.info("OpenAI API URL: {}", openAIConfig.getUrl());
            log.info("OpenAI Model: {}", openAIConfig.getModel());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAIConfig.getKey());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openAIConfig.getModel());
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                "role", "system",
                "content", "You are an intelligent AI assistant. You are helpful, friendly, and knowledgeable. " +
                          "Answer questions clearly and concisely. If you don't know something, say so honestly."
            ));
            messages.add(Map.of(
                "role", "user",
                "content", userMessage
            ));
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 500);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                openAIConfig.getUrl(), 
                entity, 
                Map.class
            );
            
            if (response != null && response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    String content = message.get("content").trim();
                    log.info("AI Response: {}", content);
                    return content;
                }
            }
            
            return "I'm sorry, I couldn't generate a response. Please try again.";
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("OpenAI API HTTP Error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("OpenAI API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("OpenAI API Server Error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("OpenAI API server error: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Error calling OpenAI API: {}", e.getMessage(), e);
            log.error("Error class: {}", e.getClass().getName());
            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("Failed to get AI response: " + e.getMessage());
        }
    }
}
