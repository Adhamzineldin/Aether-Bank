package com.maayn.notificationservice.services.transfer;

import com.maayn.notificationservice.dto.TransferFailedPayload;
import com.maayn.notificationservice.dto.transfer.TransferSuccessPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferAlertService {

    private final AccountContactResolver contactResolver;
    private final TransferEmailNotifier emailNotifier;

    public void handleSuccess(TransferSuccessPayload event) {
        contactResolver.resolveByAccountId(event.sourceAccountId()).ifPresentOrElse(
                contact -> emailNotifier.sendTransferSuccess(contact.email(), event),
                () -> log.warn(
                        "No contact for sourceAccountId {}; referenceNumber={}",
                        event.sourceAccountId(),
                        event.referenceNumber()
                )
        );
    }

    public void handleFailure(TransferFailedPayload event) {
        contactResolver.resolveByAccountId(event.sourceAccountId()).ifPresentOrElse(
                contact -> emailNotifier.sendTransferFailed(contact.email(), event),
                () -> log.warn(
                        "No contact for sourceAccountId {}; referenceNumber={}",
                        event.sourceAccountId(),
                        event.referenceNumber()
                )
        );
    }
}
