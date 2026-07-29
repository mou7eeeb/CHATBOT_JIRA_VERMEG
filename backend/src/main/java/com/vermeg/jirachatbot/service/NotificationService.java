package com.vermeg.jirachatbot.service;

import com.vermeg.jirachatbot.dto.NotificationDTO;
import com.vermeg.jirachatbot.entity.Notification;
import com.vermeg.jirachatbot.entity.User;
import com.vermeg.jirachatbot.repository.NotificationRepository;
import com.vermeg.jirachatbot.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    private Long currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

    public List<NotificationDTO> getNotifications() {
        return notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(currentUserId())
                .stream().map(this::toDTO).toList();
    }

    public long getUnreadCount() {
        return notificationRepository.countByUserIdAndReadFalse(currentUserId());
    }

    @Transactional
    public void create(User user, String message, Long chatSessionId) {
        notificationRepository.save(Notification.builder()
                .user(user).message(message).chatSessionId(chatSessionId).build());
    }

    @Transactional
    public NotificationDTO markAsRead(Long id) {
        Notification notification = notificationRepository.findByIdAndUserId(id, currentUserId())
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return toDTO(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead() {
        List<Notification> notifications = notificationRepository.findByUserIdAndReadFalse(currentUserId());
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId()).message(notification.getMessage())
                .read(notification.getRead()).chatSessionId(notification.getChatSessionId())
                .createdAt(notification.getCreatedAt()).build();
    }
}
