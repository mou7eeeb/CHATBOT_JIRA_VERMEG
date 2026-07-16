package com.vermeg.jirachatbot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroqStartupLogger {

    private final GroqConfig groqConfig;

    @EventListener(ApplicationReadyEvent.class)
    public void logGroqConfiguration() {
        if (groqConfig.isConfigured()) {
            log.info("✓ Groq API key configured: true");
            log.info("✓ Groq API model: {}", groqConfig.getModel());
            log.info("✓ Groq API URL: {}", groqConfig.getUrl());
        } else {
            log.warn("⚠ GROQ_API_KEY is missing or not configured");
            log.warn("⚠ To enable Groq AI features, set the GROQ_API_KEY environment variable");
            log.warn("⚠ Example: $env:GROQ_API_KEY='your-key-here'");
        }
    }
}
