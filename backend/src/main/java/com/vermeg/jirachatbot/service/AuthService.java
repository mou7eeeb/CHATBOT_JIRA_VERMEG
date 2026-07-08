package com.vermeg.jirachatbot.service;

import com.vermeg.jirachatbot.dto.CheckEmailRequest;
import com.vermeg.jirachatbot.dto.CheckEmailResponse;
import com.vermeg.jirachatbot.dto.JwtResponse;
import com.vermeg.jirachatbot.dto.LoginRequest;
import com.vermeg.jirachatbot.dto.PasswordResetRequest;
import com.vermeg.jirachatbot.dto.SignupRequest;
import com.vermeg.jirachatbot.entity.User;
import com.vermeg.jirachatbot.repository.UserRepository;
import com.vermeg.jirachatbot.security.JwtTokenProvider;
import com.vermeg.jirachatbot.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;

    // Stockage en mémoire des codes de vérification (email -> code)
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    private final Random random = new Random();
    
    @Transactional
    public JwtResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(User.Role.USER)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        return new JwtResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }
    
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("User logged in: {}", user.getEmail());
        
        return new JwtResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }
    
    public CheckEmailResponse checkEmailExists(CheckEmailRequest request) {
        boolean exists = userRepository.existsByEmail(request.getEmail());
        if (exists) {
            log.info("Email check: {} exists", request.getEmail());
            return CheckEmailResponse.exists();
        } else {
            log.info("Email check: {} does not exist", request.getEmail());
            return CheckEmailResponse.notExists();
        }
    }
    
    public String generateAndSendVerificationCode(String email) {
        // Générer un code à 6 chiffres
        String code = String.format("%06d", random.nextInt(1000000));

        // Stocker le code en mémoire
        verificationCodes.put(email, code);

        // Envoyer le code par email via EmailService
        emailService.sendVerificationCode(email, code);

        log.info("Verification code generated and sent to {}", email);

        return code;
    }
    
    public boolean verifyCode(String email, String code) {
        String storedCode = verificationCodes.get(email);
        if (storedCode != null && storedCode.equals(code)) {
            log.info("Code verified successfully for {}", email);
            return true;
        }
        log.warn("Invalid code verification attempt for {}", email);
        return false;
    }
    
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        if (!verifyCode(request.getEmail(), request.getVerificationCode())) {
            throw new RuntimeException("Invalid verification code");
        }
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Supprimer le code après utilisation
        verificationCodes.remove(request.getEmail());
        
        log.info("Password reset successfully for {}", request.getEmail());
    }
}
