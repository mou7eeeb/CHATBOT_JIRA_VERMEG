package com.vermeg.jirachatbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {
    
    private final GroqService groqService;
    
    public String convertToJQL(String naturalLanguage) {
        log.info("Converting natural language to JQL: {}", naturalLanguage);
        
        if (groqService.isConfigured()) {
            return convertWithGroq(naturalLanguage);
        } else {
            log.info("Groq not configured, using rule-based fallback");
            return convertWithRules(naturalLanguage);
        }
    }
    
    private String convertWithGroq(String naturalLanguage) {
        try {
            String jql = groqService.generateJQL(naturalLanguage);
            if (jql == null || jql.isBlank()) {
                log.warn("Groq returned an empty JQL, falling back to rule-based system");
                return convertWithRules(naturalLanguage);
            }
            log.info("Groq generated JQL: {}", jql);
            return jql;
        } catch (Exception e) {
            log.error("Error calling Groq API: {}", e.getMessage());
            return convertWithRules(naturalLanguage);
        }
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
