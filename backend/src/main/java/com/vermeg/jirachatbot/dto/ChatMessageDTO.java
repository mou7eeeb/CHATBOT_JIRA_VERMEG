package com.vermeg.jirachatbot.dto;

import com.vermeg.jirachatbot.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {
    private Long id;

    @NotBlank(message = "Content is required")
    private String content;

    private ChatMessage.MessageType messageType;
    private String generatedJql;
    private String jiraResults;
    private Integer ticketCount;
    private LocalDateTime createdAt;
}
