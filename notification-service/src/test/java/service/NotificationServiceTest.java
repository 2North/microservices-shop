package service;

import com.shop.notification.dto.NotificationDto;
import com.shop.notification.model.Notification;
import com.shop.notification.repository.NotificationRepository;
import com.shop.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Юнит-тест для NotificationService
 *
 * Тестирует:
 * - Отправка (создание) уведомления
 * - Получение уведомлений пользователя
 * - Подсчёт непрочитанных уведомлений
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationDto notificationDto;
    private Notification savedNotification;

    @BeforeEach
    void setUp() {
        notificationDto = NotificationDto.builder()
                .userId(1L)
                .type("ORDER_CREATED")
                .title("Order Created")
                .message("Your order #1 has been created successfully!")
                .build();

        savedNotification = Notification.builder()
                .id(1L)
                .userId(1L)
                .type("ORDER_CREATED")
                .title("Order Created")
                .message("Your order #1 has been created successfully!")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void sendNotification_SavesAndReturnsNotification() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // Act
        NotificationDto result = notificationService.sendNotification(notificationDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals("ORDER_CREATED", result.getType());
        assertEquals("Order Created", result.getTitle());
        assertFalse(result.getIsRead());  // Новое уведомление не прочитано

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getUserNotifications_ReturnsUserNotifications() {
        // Arrange
        Notification notification1 = Notification.builder()
                .id(1L)
                .userId(1L)
                .type("ORDER_CREATED")
                .title("Order Created")
                .message("Order #1 created")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification notification2 = Notification.builder()
                .id(2L)
                .userId(1L)
                .type("ORDER_STATUS_CHANGED")
                .title("Status Changed")
                .message("Order #1 confirmed")
                .isRead(true)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(notification1, notification2));

        // Act
        List<NotificationDto> results = notificationService.getUserNotifications(1L);

        // Assert
        assertEquals(2, results.size());
        assertEquals("Order Created", results.get(0).getTitle());
        assertEquals("Status Changed", results.get(1).getTitle());

        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getUnreadCount_ReturnsCorrectCount() {
        // Arrange
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5L);

        // Act
        long count = notificationService.getUnreadCount(1L);

        // Assert
        assertEquals(5L, count);

        verify(notificationRepository).countByUserIdAndIsReadFalse(1L);
    }
}