package com.vermeg.jirachatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiraSearchCriteria {
    private String projectKey;
    private String status;
    private String statusCategory;
    private String priority;
    private String assignee;
    private Boolean overdue;
    private String issueKey;
    private Integer maxResults;
    private String issueType;
}
