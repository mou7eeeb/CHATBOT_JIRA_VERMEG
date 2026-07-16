package com.vermeg.jirachatbot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            log.debug("JWT Filter - Request URI: {}", request.getRequestURI());
            log.debug("JWT Filter - Token present: {}", jwt != null);
            
            if (StringUtils.hasText(jwt)) {
                log.debug("JWT Filter - Validating token...");
                boolean isValid = tokenProvider.validateToken(jwt);
                log.debug("JWT Filter - Token valid: {}", isValid);
                
                if (isValid) {
                    Long userId = tokenProvider.getUserIdFromToken(jwt);
                    log.debug("JWT Filter - User ID from token: {}", userId);
                    
                    UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                    log.debug("JWT Filter - User loaded: {}", userDetails.getUsername());
                    log.debug("JWT Filter - User authorities: {}", userDetails.getAuthorities());
                    
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT Filter - Authentication set successfully");
                } else {
                    log.warn("JWT Filter - Token validation failed for URI: {}", request.getRequestURI());
                }
            } else {
                log.debug("JWT Filter - No token found in request");
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
