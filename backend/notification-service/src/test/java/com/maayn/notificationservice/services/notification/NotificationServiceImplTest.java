package com.maayn.notificationservice.services.notification;

import com.maayn.notificationservice.documents.notification.NotificationItemDocument;
import com.maayn.notificationservice.documents.notification.NotificationTemplateDocument;
import com.maayn.notificationservice.mappers.NotificationMapper;
import com.maayn.notificationservice.repositories.NotificationItemRepository;
import com.maayn.notificationservice.repositories.NotificationTemplateRepository;
import maayn.veld.generated.models.notification.NotificationChannel;
import maayn.veld.generated.models.notification.NotificationEventType;
import maayn.veld.generated.models.notification.NotificationStatus;
import maayn.veld.generated.models.notification.NotificationType;
import maayn.veld.generated.models.notification.NotificationItem;
import maayn.veld.generated.models.notification.SendNotificationInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationItemRepository itemRepository;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private NotificationDispatchService dispatchService;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(
                itemRepository,
                templateRepository,
                new NotificationMapper(),
                dispatchService
        );
    }

    @Test
    void sendNotification_resolvesTemplateAndMarksAsSent() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        NotificationTemplateDocument template = NotificationTemplateDocument.builder()
                .id(templateId)
                .eventType(NotificationEventType.LOAN_APPROVED.getValue())
                .channel(NotificationChannel.EMAIL.getValue())
                .titleTemplate("Loan update for {userId}")
                .bodyTemplate("Event {eventType} via {channel} for {type}")
                .version(4)
                .isActive(Boolean.TRUE)
                .build();

        when(templateRepository.findByEventTypeAndChannelAndIsActiveTrue(
                eq(NotificationEventType.LOAN_APPROVED.getValue()),
                eq(NotificationChannel.EMAIL.getValue())
        )).thenReturn(Optional.of(template));

        when(itemRepository.save(any(NotificationItemDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        doAnswer(invocation -> {
            NotificationItemDocument document = invocation.getArgument(0);
            document.setStatus(NotificationStatus.SENT);
            document.setProcessedAt(now);
            document.setSentAt(now);
            document.setFailedReason(null);
            return null;
        }).when(dispatchService).dispatch(any(NotificationItemDocument.class));

        SendNotificationInput input = new SendNotificationInput(
                userId,
                NotificationEventType.LOAN_APPROVED,
                NotificationChannel.EMAIL,
                NotificationType.LOAN,
                UUID.randomUUID(),
                null,
                null
        );

        NotificationItem result = service.sendNotification(input);

        assertNotNull(result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(templateId, result.getTemplateId());
        assertEquals(Integer.valueOf(4), result.getTemplateVersion());
        assertEquals(NotificationStatus.SENT, result.getStatus());
        assertTrue(result.getTitle().contains(userId.toString()));
        assertTrue(result.getMessage().contains(NotificationType.LOAN.getValue()));
        verify(dispatchService).dispatch(any(NotificationItemDocument.class));
        verify(itemRepository, times(2)).save(any(NotificationItemDocument.class));
    }
}