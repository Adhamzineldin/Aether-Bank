package com.maayn.transactionservice.mappers;

import com.maayn.transactionservice.entity.LedgerBalance;
import maayn.veld.generated.models.ledger.BalanceResponse;

public class LedgerMapper {

    public static BalanceResponse toBalanceResponse(LedgerBalance balance) {
        BalanceResponse response = new BalanceResponse();
        response.setAccountId(balance.getId().getAccountId());
        response.setCurrency(balance.getId().getCurrency());
        response.setAvailableBalance(balance.getAvailableBalance());
        response.setPendingHolds(balance.getPendingHolds());
        return response;
    }
}