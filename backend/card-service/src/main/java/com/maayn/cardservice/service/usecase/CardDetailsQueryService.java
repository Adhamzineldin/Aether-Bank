package com.maayn.cardservice.service.usecase;

import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.service.support.CardAccessService;
import maayn.veld.generated.errors.GetCardDetailsException;
import maayn.veld.generated.models.card.CardDetailsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/**
 * Handles card detail reads.
 * Kept separate from mutation flows so lookup logic stays simple and read-only.
 */
public class CardDetailsQueryService {

    private final CardAccessService cardAccessService;

    public CardDetailsQueryService(CardAccessService cardAccessService) {
        this.cardAccessService = cardAccessService;
    }

    @Transactional(readOnly = true)
    public CardDetailsResponse getCardDetails(String cardId) throws GetCardDetailsException {
        // Validate the identifier and load the card before mapping it to the API response contract.
        return CardMapper.toCardDetailsResponse(cardAccessService.getCardDetailsCard(cardId));
    }
}
