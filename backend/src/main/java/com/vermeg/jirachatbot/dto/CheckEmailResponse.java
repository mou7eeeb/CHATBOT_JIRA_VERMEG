package com.vermeg.jirachatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckEmailResponse {
    
    private boolean exists;
    private String message;
    
    public static CheckEmailResponse exists() {
        return new CheckEmailResponse(true, "Email exists in the system");
    }
    
    public static CheckEmailResponse notExists() {
        return new CheckEmailResponse(false, "No account found with this email");
    }
}
