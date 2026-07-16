package com.vermeg.jirachatbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraTicket {
    
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("fields")
    private TicketFields fields;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TicketFields {
        
        @JsonProperty("summary")
        private String summary;

        @JsonProperty("description")
        private Map<String, Object> description;

        @JsonProperty("status")
        private Map<String, Object> status;

        @JsonProperty("statuscategory")
        private Map<String, Object> statusCategory;
        
        @JsonProperty("issuetype")
        private Map<String, Object> issueType;
        
        @JsonProperty("priority")
        private Map<String, Object> priority;
        
        @JsonProperty("assignee")
        private Map<String, Object> assignee;
        
        @JsonProperty("reporter")
        private Map<String, Object> reporter;
        
        @JsonProperty("created")
        private String created;
        
        @JsonProperty("updated")
        private String updated;
        
        public String getStatusName() {
            if (status != null && status.containsKey("name")) {
                return (String) status.get("name");
            }
            return "Unknown";
        }
        
        public String getIssueTypeName() {
            if (issueType != null && issueType.containsKey("name")) {
                return (String) issueType.get("name");
            }
            return "Unknown";
        }
        
        public String getPriorityName() {
            if (priority != null && priority.containsKey("name")) {
                return (String) priority.get("name");
            }
            return "Unknown";
        }
        
        public String getAssigneeName() {
            if (assignee != null && assignee.containsKey("displayName")) {
                return (String) assignee.get("displayName");
            }
            return "Unassigned";
        }

        public String getStatusCategoryName() {
            if (statusCategory != null && statusCategory.containsKey("name")) {
                return (String) statusCategory.get("name");
            }
            return "Unknown";
        }
    }
}
