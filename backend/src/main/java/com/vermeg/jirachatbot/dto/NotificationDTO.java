package com.vermeg.jirachatbot.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String message;
    private Boolean read;
    private Long chatSessionId;
    private LocalDateTime createdAt;
}
