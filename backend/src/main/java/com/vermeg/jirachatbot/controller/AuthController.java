package com.vermeg.jirachatbot.controller;

import com.vermeg.jirachatbot.dto.CheckEmailRequest;
import com.vermeg.jirachatbot.dto.CheckEmailResponse;
import com.vermeg.jirachatbot.dto.JwtResponse;
import com.vermeg.jirachatbot.dto.LoginRequest;
import com.vermeg.jirachatbot.dto.MessageResponse;
import com.vermeg.jirachatbot.dto.PasswordResetRequest;
import com.vermeg.jirachatbot.dto.SignupRequest;
import com.vermeg.jirachatbot.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try {
            JwtResponse response = authService.signup(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Signup error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            JwtResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid email or password"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }
    
    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@Valid @RequestBody CheckEmailRequest request) {
        try {
            CheckEmailResponse response = authService.checkEmailExists(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Check email error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Error checking email"));
        }
    }
    
    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@Valid @RequestBody CheckEmailRequest request) {
        try {
            // D'abord vérifier si l'email existe
            CheckEmailResponse checkResponse = authService.checkEmailExists(request);
            if (!checkResponse.isExists()) {
                return ResponseEntity.badRequest().body(new MessageResponse("No account found with this email"));
            }
            
            // Générer et envoyer le code
            String code = authService.generateAndSendVerificationCode(request.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Verification code sent successfully");
            response.put("code", code); // Pour la démo, on retourne le code
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Send verification code error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Error sending verification code"));
        }
    }
    
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");
            
            boolean isValid = authService.verifyCode(email, code);
            
            if (isValid) {
                return ResponseEntity.ok(new MessageResponse("Code verified successfully"));
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid verification code"));
            }
        } catch (Exception e) {
            log.error("Verify code error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Error verifying code"));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
        } catch (RuntimeException e) {
            log.error("Reset password error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
