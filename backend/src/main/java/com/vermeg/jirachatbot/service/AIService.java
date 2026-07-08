package com.vermeg.jirachatbot.service;

import com.vermeg.jirachatbot.config.OpenAIConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {
    
    private final OpenAIConfig openAIConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    
    public String convertToJQL(String naturalLanguage) {
        log.info("Converting natural language to JQL: {}", naturalLanguage);
        
        if (openAIConfig.isConfigured()) {
            return convertWithOpenAI(naturalLanguage);
        } else {
            log.info("OpenAI not configured, using rule-based fallback");
            return convertWithRules(naturalLanguage);
        }
    }
    
    private String convertWithOpenAI(String naturalLanguage) {
        try {
            String prompt = buildPrompt(naturalLanguage);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAIConfig.getKey());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openAIConfig.getModel());
            requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", "You are a JQL query generator. Convert natural language questions about Jira tickets into JQL (Jira Query Language). Return ONLY the JQL query, no explanations."),
                Map.of("role", "user", "content", prompt)
            });
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 150);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(openAIConfig.getUrl(), entity, Map.class);
            
            if (response != null && response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    String jql = message.get("content").trim();
                    log.info("OpenAI generated JQL: {}", jql);
                    return jql;
                }
            }
            
            log.warn("OpenAI API call failed, falling back to rule-based system");
            return convertWithRules(naturalLanguage);
            
        } catch (Exception e) {
            log.error("Error calling OpenAI API: {}", e.getMessage());
            return convertWithRules(naturalLanguage);
        }
    }
    
    private String buildPrompt(String naturalLanguage) {
        return "Convert this question to JQL: \"" + naturalLanguage + "\"\n" +
               "Examples:\n" +
               "- \"show me open bugs in CRM\" -> \"project = CRM AND issuetype = Bug AND status != Done\"\n" +
               "- \"all high priority issues\" -> \"priority = High\"\n" +
               "- \"issues assigned to john\" -> \"assignee = john\"\n" +
               "- \"my open tasks\" -> \"assignee = currentUser() AND status != Done\"\n" +
               "Return ONLY the JQL query.";
    }
    
    private String convertWithRules(String naturalLanguage) {
        String lowerInput = naturalLanguage.toLowerCase();
        StringBuilder jql = new StringBuilder();
        
        Pattern projectPattern = Pattern.compile("in\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher projectMatcher = projectPattern.matcher(lowerInput);
        if (projectMatcher.find()) {
            String project = projectMatcher.group(1);
            jql.append("project = \"").append(project).append("\"");
        }
        
        if (lowerInput.contains("bug")) {
            if (jql.length() > 0) jql.append(" AND ");
            jql.append("issuetype = Bug");
        } else if (lowerInput.contains("story")) {
            if (jql.length() > 0) jql.append(" AND ");
            jql.append("issuetype = Story");
        } else if (lowerInput.contains("task")) {
            if (jql.length() > 0) jql.append(" AND ");
            jql.append("issuetype = Task");
        }
        
        if (lowerInput.contains("open") || lowerInput.contains("not done") || lowerInput.contains("in progress")) {
            if (jql.length() > 0) jql.append(" AND ");
            jql.append("status != Done");
        } else if (lowerInput.contains("closed") || lowerInput.contains("done")) {
            if (jql.length() > 0) jql.append(" AND ");
            jql.append("status = Done");
        }
        
        if (lowerInput.contains("high priority")) {
            if (jql.length() > 0) jql.append(" AND ");
            jql.append("priority = High");
        } else if (lowerInput.contains("low priority")) {
            if (jql.length() > 0) jql.append(" AND ");
            jql.append("priority = Low");
        }
        
        Pattern assigneePattern = Pattern.compile("assigned to\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher assigneeMatcher = assigneePattern.matcher(lowerInput);
        if (assigneeMatcher.find()) {
            String assignee = assigneeMatcher.group(1);
            if (jql.length() > 0) jql.append(" AND ");
            jql.append("assignee = \"").append(assignee).append("\"");
        } else if (lowerInput.contains("my") || lowerInput.contains("assigned to me")) {
            if (jql.length() > 0) jql.append(" AND ");
            jql.append("assignee = currentUser()");
        }
        
        if (jql.length() == 0) {
            jql.append("order by created DESC");
        }
        
        String result = jql.toString();
        log.info("Rule-based JQL: {}", result);
        return result;
    }
}
