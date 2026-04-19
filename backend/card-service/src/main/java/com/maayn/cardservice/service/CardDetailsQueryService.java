package com.maayn.cardservice.service;

import com.maayn.cardservice.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.GetCardDetailsException;
import maayn.veld.generated.models.card.CardDetailsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardDetailsQueryService {

    private final CardAccessService cardAccessService;

    @Transactional(readOnly = true)
    public CardDetailsResponse getCardDetails(String cardId) throws GetCardDetailsException {
        return CardMapper.toCardDetailsResponse(cardAccessService.getCardDetailsCard(cardId));
    }
}
