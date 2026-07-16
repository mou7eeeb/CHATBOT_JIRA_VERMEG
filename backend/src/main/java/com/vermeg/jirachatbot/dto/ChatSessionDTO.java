package com.vermeg.jirachatbot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatSessionDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private Long jiraConnectionId;
    private String jiraConnectionName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String lastMessagePreview;
    private Integer messageCount;

    private List<ChatMessageDTO> messages;
}
