package com.vermeg.jirachatbot.service;

import com.vermeg.jirachatbot.model.JiraSearchCriteria;
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
            "mon profil", "mes informations", "mon compte", "mon compte jira",
            "projects", "my projects", "list projects", "show projects",
            "projets", "mes projets", "affiche mes projets", "liste des projets",
            "tickets", "issues", "my tickets", "my issues", "open tickets", "open issues",
            "ticket", "mes tickets", "affiche mes tickets", "tickets ouverts",
            "assigned", "assigned to me", "my tasks", "my work",
            "assigné", "assigné à moi", "mes tâches", "mon travail",
            "bug", "bugs", "critical", "priority", "high priority",
            "priorité", "tickets priorité", " haute priorité",
            "sprint", "sprints", "board", "boards",
            "status", "done", "in progress", "todo", "backlog",
            "statut", "en cours", "terminé", "à faire",
            "search", "find", "show", "list", "display",
            "recherche", "trouve", "affiche", "liste", "montre",
            "jql", "query",
            "jira", "jira cloud", "atlassian"
    );

    private static final List<String> GENERAL_KNOWLEDGE_KEYWORDS = Arrays.asList(
            "what is", "explain", "define", "meaning of",
            "how to", "how do i", "tutorial",
            "scrum", "agile", "kanban", "methodology",
            "best practices", "tips", "advice",
            "bonjour", "hello", "salut", "hi",
            "comment ça va", "how are you", "how do you do",
            "qui es-tu", "who are you", "what are you",
            "tu comprends", "do you understand", "understand",
            "merci", "thanks", "thank you",
            "aide", "help", "assistant",
            "spring boot", "java", "programming", "code",
            "écris", "write", "rédige", "compose",
            "email", "cv", "resume", "letter"
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

        // Default to GENERAL_KNOWLEDGE if unsure - better to answer general questions than force Jira
        log.info("Defaulting to GENERAL_KNOWLEDGE intent");
        return Intent.GENERAL_KNOWLEDGE;
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

        // Check for personal pronouns with work-related terms (English)
        if ((message.contains("my ") || message.contains("show ") || message.contains("list ") || message.contains("display ")) &&
                (message.contains("ticket") || message.contains("issue") || message.contains("task") ||
                 message.contains("project") || message.contains("sprint") || message.contains("board"))) {
            return true;
        }

        // Check for personal pronouns with work-related terms (French)
        if ((message.contains("mes ") || message.contains("mon ") || message.contains("affiche ") || message.contains("montre ") || message.contains("liste ")) &&
                (message.contains("ticket") || message.contains("tâche") || message.contains("projet") ||
                 message.contains("sprint") || message.contains("board"))) {
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

    public JiraSearchCriteria extractSearchCriteria(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        log.info("Extracting search criteria from message: {}", lowerMessage);

        JiraSearchCriteria criteria = JiraSearchCriteria.builder()
                .assignee("currentUser()")
                .build();

        // Extract status filter
        if (lowerMessage.contains("à faire") || lowerMessage.contains("todo") || lowerMessage.contains("backlog")) {
            criteria.setStatus("To Do");
            log.info("Detected status filter: To Do");
        } else if (lowerMessage.contains("en cours") || lowerMessage.contains("in progress") || lowerMessage.contains("active")) {
            criteria.setStatus("In Progress");
            log.info("Detected status filter: In Progress");
        } else if (lowerMessage.contains("terminé") || lowerMessage.contains("done") || lowerMessage.contains("completed") || lowerMessage.contains("closed")) {
            criteria.setStatus("Done");
            log.info("Detected status filter: Done");
        } else if (lowerMessage.contains("revue") || lowerMessage.contains("review")) {
            criteria.setStatus("In Review");
            log.info("Detected status filter: In Review");
        }

        // Extract priority filter
        if (lowerMessage.contains("critique") || lowerMessage.contains("critical")) {
            criteria.setPriority("Critical");
            log.info("Detected priority filter: Critical");
        } else if (lowerMessage.contains("haute") || lowerMessage.contains("high")) {
            criteria.setPriority("High");
            log.info("Detected priority filter: High");
        } else if (lowerMessage.contains("moyenne") || lowerMessage.contains("medium")) {
            criteria.setPriority("Medium");
            log.info("Detected priority filter: Medium");
        } else if (lowerMessage.contains("faible") || lowerMessage.contains("low")) {
            criteria.setPriority("Low");
            log.info("Detected priority filter: Low");
        }

        // Extract issue type
        if (lowerMessage.contains("bug") || lowerMessage.contains("bugs")) {
            criteria.setIssueType("Bug");
            log.info("Detected issue type: Bug");
        } else if (lowerMessage.contains("story") || lowerMessage.contains("user story")) {
            criteria.setIssueType("Story");
            log.info("Detected issue type: Story");
        } else if (lowerMessage.contains("task") || lowerMessage.contains("tâche")) {
            criteria.setIssueType("Task");
            log.info("Detected issue type: Task");
        }

        // Extract issue key
        Pattern issueKeyPattern = Pattern.compile("[A-Z]+-\\d+");
        java.util.regex.Matcher matcher = issueKeyPattern.matcher(userMessage);
        if (matcher.find()) {
            criteria.setIssueKey(matcher.group());
            log.info("Detected issue key: {}", criteria.getIssueKey());
        }

        return criteria;
    }

    public String generateJQLFromCriteria(JiraSearchCriteria criteria) {
        if (criteria == null) {
            return "assignee = currentUser()";
        }

        StringBuilder jql = new StringBuilder();
        boolean hasCondition = false;

        // Issue key - highest priority
        if (criteria.getIssueKey() != null && !criteria.getIssueKey().isEmpty()) {
            jql.append("key = '").append(criteria.getIssueKey()).append("'");
            hasCondition = true;
            log.info("Generated JQL with issue key: {}", jql.toString());
            return jql.toString();
        }

        // Assignee
        if (criteria.getAssignee() != null && !criteria.getAssignee().isEmpty()) {
            if (hasCondition) jql.append(" AND ");
            jql.append("assignee = ").append(criteria.getAssignee());
            hasCondition = true;
        }

        // Status
        if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
            if (hasCondition) jql.append(" AND ");
            jql.append("status = '").append(criteria.getStatus()).append("'");
            hasCondition = true;
        }

        // Issue type
        if (criteria.getIssueType() != null && !criteria.getIssueType().isEmpty()) {
            if (hasCondition) jql.append(" AND ");
            jql.append("issuetype = '").append(criteria.getIssueType()).append("'");
            hasCondition = true;
        }

        // Priority
        if (criteria.getPriority() != null && !criteria.getPriority().isEmpty()) {
            if (hasCondition) jql.append(" AND ");
            jql.append("priority = '").append(criteria.getPriority()).append("'");
            hasCondition = true;
        }

        // Project
        if (criteria.getProjectKey() != null && !criteria.getProjectKey().isEmpty()) {
            if (hasCondition) jql.append(" AND ");
            jql.append("project = '").append(criteria.getProjectKey()).append("'");
            hasCondition = true;
        }

        String finalJql = hasCondition ? jql.toString() : "assignee = currentUser()";
        log.info("Generated JQL from criteria: {}", finalJql);
        return finalJql;
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

        // Use the new criteria-based approach
        JiraSearchCriteria criteria = extractSearchCriteria(userMessage);
        return generateJQLFromCriteria(criteria);
    }
}
