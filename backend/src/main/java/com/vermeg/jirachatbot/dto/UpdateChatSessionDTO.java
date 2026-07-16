package com.vermeg.jirachatbot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateChatSessionDTO {
    @NotBlank(message = "Title is required")
    private String title;
}
