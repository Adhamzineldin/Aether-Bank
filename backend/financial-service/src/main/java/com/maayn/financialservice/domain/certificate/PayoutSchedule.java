package com.maayn.financialservice.domain.certificate;

import java.util.List;
import java.util.UUID;

public record PayoutSchedule(
        UUID certificateId,
        List<PayoutLine> lines
) {
}
