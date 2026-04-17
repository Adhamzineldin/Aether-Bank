package com.maayn.cardservice.service.usecase;

import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.service.support.CardAccessService;
import maayn.veld.generated.errors.GetCardDetailsException;
import maayn.veld.generated.models.card.CardDetailsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardDetailsQueryService {

    private final CardAccessService cardAccessService;

    public CardDetailsQueryService(CardAccessService cardAccessService) {
        this.cardAccessService = cardAccessService;
    }

    @Transactional(readOnly = true)
    public CardDetailsResponse getCardDetails(String cardId) throws GetCardDetailsException {
        return CardMapper.toCardDetailsResponse(cardAccessService.getCardDetailsCard(cardId));
    }
}
