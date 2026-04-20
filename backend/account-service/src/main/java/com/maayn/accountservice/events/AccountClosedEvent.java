package com.maayn.accountservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountClosedEvent {
    private UUID accountId;
    private UUID customerId;
    private LocalDateTime timestamp;
}

