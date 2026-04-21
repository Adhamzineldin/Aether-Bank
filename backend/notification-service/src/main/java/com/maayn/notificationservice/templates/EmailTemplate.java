package com.maayn.notificationservice.templates;

import com.maayn.notificationservice.dto.TransferFailedPayload;
import com.maayn.notificationservice.dto.TransferSuccessPayload;

public final class EmailTemplate {

    private EmailTemplate() {
    }

    public static String transferSuccessSubject(String referenceNumber) {
        return "Transfer completed — " + referenceNumber;
    }

    public static String transferSuccessBody(TransferSuccessPayload e) {
        return """
                Hello,

                Your transfer has completed successfully.

                Reference: %s
                Amount: %s %s
                From account: %s
                To account: %s

                Thank you for banking with us.
                """.formatted(
                e.referenceNumber(),
                e.amount(),
                e.currency(),
                e.sourceAccountId(),
                e.destinationAccountId()
        );
    }

    public static String transferFailedSubject(String referenceNumber) {
        return "Transfer could not be completed — " + referenceNumber;
    }

    public static String transferFailedBody(TransferFailedPayload e) {
        String reason = e.failureReason() != null ? e.failureReason() : "Unknown reason";
        return """
                Hello,

                We could not complete your transfer.

                Reference: %s
                Amount: %s %s
                From account: %s
                Reason: %s

                If you need help, please contact support.
                """.formatted(
                e.referenceNumber(),
                e.amount(),
                e.currency(),
                e.sourceAccountId(),
                reason
        );
    }
}
