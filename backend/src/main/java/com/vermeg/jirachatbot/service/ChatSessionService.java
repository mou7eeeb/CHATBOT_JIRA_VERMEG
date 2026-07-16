package com.vermeg.jirachatbot.service;

import com.vermeg.jirachatbot.dto.ChatMessageDTO;
import com.vermeg.jirachatbot.dto.ChatSessionDTO;
import com.vermeg.jirachatbot.dto.CreateChatSessionDTO;
import com.vermeg.jirachatbot.dto.UpdateChatSessionDTO;
import com.vermeg.jirachatbot.entity.ChatMessage;
import com.vermeg.jirachatbot.entity.ChatSession;
import com.vermeg.jirachatbot.entity.JiraConnection;
import com.vermeg.jirachatbot.entity.User;
import com.vermeg.jirachatbot.repository.ChatMessageRepository;
import com.vermeg.jirachatbot.repository.ChatSessionRepository;
import com.vermeg.jirachatbot.repository.JiraConnectionRepository;
import com.vermeg.jirachatbot.repository.UserRepository;
import com.vermeg.jirachatbot.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final JiraConnectionRepository jiraConnectionRepository;

    private Long getCurrentUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    @Transactional
    public ChatSessionDTO createSession(CreateChatSessionDTO dto) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JiraConnection jiraConnection = null;
        if (dto.getJiraConnectionId() != null) {
            jiraConnection = jiraConnectionRepository.findByIdAndUserId(dto.getJiraConnectionId(), userId)
                    .orElseThrow(() -> new RuntimeException("Jira connection not found"));
        }

        String title = dto.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = "New Conversation";
        }

        ChatSession session = ChatSession.builder()
                .user(user)
                .jiraConnection(jiraConnection)
                .title(title)
                .isActive(true)
                .build();

        session = chatSessionRepository.save(session);
        log.info("Created chat session: {} for user: {}", session.getId(), userId);

        return mapToDTO(session);
    }

    @Transactional
    public ChatSessionDTO createSessionWithAutoTitle(String firstMessage, Long jiraConnectionId) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JiraConnection jiraConnection = null;
        if (jiraConnectionId != null) {
            jiraConnection = jiraConnectionRepository.findByIdAndUserId(jiraConnectionId, userId)
                    .orElse(null);
        }

        String autoTitle = generateAutoTitle(firstMessage);

        ChatSession session = ChatSession.builder()
                .user(user)
                .jiraConnection(jiraConnection)
                .title(autoTitle)
                .isActive(true)
                .build();

        session = chatSessionRepository.save(session);
        log.info("Created chat session with auto title: {} for user: {}", session.getId(), userId);

        return mapToDTO(session);
    }

    private String generateAutoTitle(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "New Conversation";
        }

        // Simple title generation: take first 3-7 words
        String[] words = message.trim().split("\\s+");
        int wordCount = Math.min(words.length, 7);
        int minWords = Math.min(wordCount, 3);

        StringBuilder title = new StringBuilder();
        for (int i = 0; i < minWords; i++) {
            if (i > 0) title.append(" ");
            title.append(words[i]);
        }

        // Capitalize first letter
        String result = title.toString();
        if (result.length() > 0) {
            result = result.substring(0, 1).toUpperCase() + result.substring(1);
        }

        // Add ellipsis if truncated
        if (words.length > 7) {
            result += "...";
        }

        return result;
    }

    public List<ChatSessionDTO> getAllSessions() {
        Long userId = getCurrentUserId();
        return chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ChatSessionDTO> getActiveSessions() {
        Long userId = getCurrentUserId();
        return chatSessionRepository.findByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ChatSessionDTO getSession(Long id) {
        Long userId = getCurrentUserId();
        ChatSession session = chatSessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return mapToDTO(session);
    }

    @Transactional
    public ChatSessionDTO updateSession(Long id, UpdateChatSessionDTO dto) {
        Long userId = getCurrentUserId();
        ChatSession session = chatSessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setTitle(dto.getTitle());
        session = chatSessionRepository.save(session);
        log.info("Updated chat session: {}", id);

        return mapToDTO(session);
    }

    @Transactional
    public void deleteSession(Long id) {
        Long userId = getCurrentUserId();
        ChatSession session = chatSessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        chatSessionRepository.delete(session);
        log.info("Deleted chat session: {}", id);
    }

    @Transactional
    public ChatMessageDTO addMessage(Long sessionId, ChatMessageDTO messageDTO) {
        Long userId = getCurrentUserId();
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        ChatMessage message = ChatMessage.builder()
                .chatSession(session)
                .messageType(messageDTO.getMessageType())
                .content(messageDTO.getContent())
                .generatedJql(messageDTO.getGeneratedJql())
                .jiraResults(messageDTO.getJiraResults())
                .ticketCount(messageDTO.getTicketCount())
                .build();

        message = chatMessageRepository.save(message);
        log.info("Added message to session: {}", sessionId);

        return mapMessageToDTO(message);
    }

    public List<ChatMessageDTO> getMessages(Long sessionId) {
        Long userId = getCurrentUserId();
        chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        return chatMessageRepository.findByChatSessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(this::mapMessageToDTO)
                .collect(Collectors.toList());
    }

    private ChatSessionDTO mapToDTO(ChatSession session) {
        ChatSessionDTO dto = new ChatSessionDTO();
        dto.setId(session.getId());
        dto.setTitle(session.getTitle());
        dto.setIsActive(session.getIsActive());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());

        if (session.getJiraConnection() != null) {
            dto.setJiraConnectionId(session.getJiraConnection().getId());
            dto.setJiraConnectionName(session.getJiraConnection().getConnectionName());
        }

        if (session.getMessages() != null) {
            dto.setMessages(session.getMessages().stream()
                    .map(this::mapMessageToDTO)
                    .collect(Collectors.toList()));

            // Set message count
            dto.setMessageCount(session.getMessages().size());

            // Set last message preview
            if (!session.getMessages().isEmpty()) {
                ChatMessage lastMessage = session.getMessages().get(session.getMessages().size() - 1);
                String content = lastMessage.getContent();
                if (content != null && content.length() > 50) {
                    dto.setLastMessagePreview(content.substring(0, 50) + "...");
                } else {
                    dto.setLastMessagePreview(content);
                }
            }
        } else {
            dto.setMessageCount(0);
        }

        return dto;
    }

    private ChatMessageDTO mapMessageToDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setGeneratedJql(message.getGeneratedJql());
        dto.setJiraResults(message.getJiraResults());
        dto.setTicketCount(message.getTicketCount());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}
