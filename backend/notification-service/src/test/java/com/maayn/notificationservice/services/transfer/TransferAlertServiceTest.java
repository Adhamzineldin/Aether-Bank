package com.maayn.notificationservice.services.transfer;

import com.maayn.notificationservice.dto.TransferFailedPayload;
import com.maayn.notificationservice.dto.TransferSuccessPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferAlertServiceTest {

    @Mock
    private AccountContactResolver contactResolver;

    @Mock
    private TransferEmailNotifier emailNotifier;

    private TransferAlertService service;

    @BeforeEach
    void setUp() {
        service = new TransferAlertService(contactResolver, emailNotifier);
    }

    @Test
    void handleSuccess_sendsEmailWhenContactExists() {
        UUID accountId = UUID.randomUUID();
        TransferSuccessPayload event = new TransferSuccessPayload(
                "REF-1",
                accountId,
                UUID.randomUUID(),
                new BigDecimal("125.50"),
                "USD",
                LocalDateTime.now()
        );

        when(contactResolver.resolveByAccountId(accountId)).thenReturn(Optional.of(new AccountContactDetails(UUID.randomUUID(), "user@example.com")));

        service.handleSuccess(event);

        verify(emailNotifier).sendTransferSuccess("user@example.com", event);
    }

    @Test
    void handleFailureDoesNothingWhenContactMissing() {
        UUID accountId = UUID.randomUUID();
        TransferFailedPayload event = new TransferFailedPayload(
                "REF-2",
                accountId,
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                "USD",
                LocalDateTime.now(),
                "Insufficient funds"
        );

        when(contactResolver.resolveByAccountId(accountId)).thenReturn(Optional.empty());

        service.handleFailure(event);

        verify(emailNotifier, never()).sendTransferFailed(any(), any());
    }
}