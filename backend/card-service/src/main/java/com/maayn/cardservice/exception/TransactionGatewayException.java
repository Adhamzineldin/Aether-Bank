package com.maayn.cardservice.exception;

public class TransactionGatewayException extends RuntimeException {

    private final Reason reason;

    public TransactionGatewayException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public TransactionGatewayException(String message, Throwable cause) {
        super(message, cause);
        this.reason = Reason.UNKNOWN;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        INVALID_AMOUNT,
        INSUFFICIENT_FUNDS,
        UNKNOWN
    }
}
