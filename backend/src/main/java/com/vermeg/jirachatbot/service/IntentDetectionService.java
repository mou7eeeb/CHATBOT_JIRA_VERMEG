package com.vermeg.jirachatbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class IntentDetectionService {

    private static final List<String> JIRA_RELATED_KEYWORDS = Arrays.asList(
            "profile", "my profile", "my information", "my jira", "jira profile",
            "projects", "my projects", "list projects", "show projects",
            "tickets", "issues", "my tickets", "my issues", "open tickets", "open issues",
            "assigned", "assigned to me", "my tasks", "my work",
            "bug", "bugs", "critical", "priority", "high priority",
            "sprint", "sprints", "board", "boards",
            "status", "done", "in progress", "todo", "backlog",
            "search", "find", "show", "list", "display",
            "jql", "query"
    );

    private static final List<String> GENERAL_KNOWLEDGE_KEYWORDS = Arrays.asList(
            "what is", "explain", "define", "meaning of",
            "how to", "how do i", "tutorial",
            "scrum", "agile", "kanban", "methodology",
            "best practices", "tips", "advice"
    );

    public enum Intent {
        JIRA_DATA,
        GENERAL_KNOWLEDGE,
        UNKNOWN
    }

    public Intent detectIntent(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return Intent.UNKNOWN;
        }

        String lowerMessage = userMessage.toLowerCase().trim();
        log.info("Detecting intent for message: {}", lowerMessage);

        // Check for Jira-related patterns
        if (isJiraRelated(lowerMessage)) {
            log.info("Detected JIRA_DATA intent");
            return Intent.JIRA_DATA;
        }

        // Check for general knowledge patterns
        if (isGeneralKnowledge(lowerMessage)) {
            log.info("Detected GENERAL_KNOWLEDGE intent");
            return Intent.GENERAL_KNOWLEDGE;
        }

        // Default to JIRA_DATA if unsure, as it's safer to try Jira first
        log.info("Defaulting to JIRA_DATA intent");
        return Intent.JIRA_DATA;
    }

    private boolean isJiraRelated(String message) {
        // Check for Jira-related keywords
        for (String keyword : JIRA_RELATED_KEYWORDS) {
            if (message.contains(keyword)) {
                return true;
            }
        }

        // Check for issue key patterns (e.g., PROJ-123)
        if (Pattern.compile("[A-Z]+-\\d+").matcher(message).find()) {
            return true;
        }

        // Check for personal pronouns with work-related terms
        if ((message.contains("my ") || message.contains("show ") || message.contains("list ")) &&
                (message.contains("ticket") || message.contains("issue") || message.contains("task") ||
                 message.contains("project") || message.contains("sprint") || message.contains("board"))) {
            return true;
        }

        return false;
    }

    private boolean isGeneralKnowledge(String message) {
        // Check for general knowledge keywords
        for (String keyword : GENERAL_KNOWLEDGE_KEYWORDS) {
            if (message.contains(keyword)) {
                // But exclude if it's also Jira-related
                if (!message.contains("my ") && !message.contains("in jira") && !message.contains("jira")) {
                    return true;
                }
            }
        }

        // Check for "what is X" patterns where X is a methodology
        if (message.startsWith("what is ") || message.startsWith("what's ")) {
            String topic = message.replace("what is ", "").replace("what's ", "").trim();
            if (topic.equals("scrum") || topic.equals("agile") || topic.equals("kanban") ||
                topic.equals("jira") || topic.equals("confluence")) {
                return true;
            }
        }

        return false;
    }

    public String generateJQLFromIntent(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        log.info("Generating JQL from intent for message: {}", lowerMessage);

        // Profile information
        if (lowerMessage.contains("profile") || lowerMessage.contains("my information")) {
            return null; // Use getCurrentUser() instead
        }

        // Projects
        if (lowerMessage.contains("projects") || lowerMessage.contains("my projects")) {
            return null; // Use getProjects() instead
        }

        // My tickets / assigned to me
        if (lowerMessage.contains("my tickets") || lowerMessage.contains("my issues") ||
            lowerMessage.contains("assigned to me") || lowerMessage.contains("my tasks")) {
            return "assignee = currentUser()";
        }

        // Open tickets
        if (lowerMessage.contains("open tickets") || lowerMessage.contains("open issues")) {
            return "assignee = currentUser() AND status != Done";
        }

        // Critical bugs
        if (lowerMessage.contains("critical") && (lowerMessage.contains("bug") || lowerMessage.contains("bugs"))) {
            return "issuetype = Bug AND priority = Critical";
        }

        // High priority
        if (lowerMessage.contains("high priority") || lowerMessage.contains("priority high")) {
            return "priority in (High, Highest)";
        }

        // Done tickets
        if (lowerMessage.contains("done") || lowerMessage.contains("completed") || lowerMessage.contains("closed")) {
            return "assignee = currentUser() AND status = Done";
        }

        // In progress
        if (lowerMessage.contains("in progress") || lowerMessage.contains("active")) {
            return "assignee = currentUser() AND status = 'In Progress'";
        }

        // Backlog
        if (lowerMessage.contains("backlog") || lowerMessage.contains("todo")) {
            return "assignee = currentUser() AND status = Backlog";
        }

        // Search by project
        if (lowerMessage.contains("project")) {
            // Extract project name or key (simplified)
            return "project is not EMPTY";
        }

        // Default: search for all issues assigned to current user
        return "assignee = currentUser()";
    }
}
