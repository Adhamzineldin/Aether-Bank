package com.maayn.cardservice.service.usecase;

import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.repository.CardTransactionRepository;
import com.maayn.cardservice.service.support.CardAccessService;
import maayn.veld.generated.errors.GetCardTransactionsException;
import maayn.veld.generated.models.card.GetCardTransactionsRequest;
import maayn.veld.generated.models.card.PaginatedCardTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CardTransactionHistoryService {

    private final CardAccessService cardAccessService;
    private final CardTransactionRepository cardTransactionRepository;

    public CardTransactionHistoryService(CardAccessService cardAccessService, CardTransactionRepository cardTransactionRepository) {
        this.cardAccessService = cardAccessService;
        this.cardTransactionRepository = cardTransactionRepository;
    }

    @Transactional(readOnly = true)
    public PaginatedCardTransactionResponse getTransactions(String cardId, GetCardTransactionsRequest input) throws GetCardTransactionsException {
        UUID parsedCardId = cardAccessService.getExistingCardId(cardId);
        GetCardTransactionsRequest request = input != null ? input : new GetCardTransactionsRequest();
        if (request.getPage() == null) {
            request.setPage(0);
        }
        if (request.getPageSize() == null) {
            request.setPageSize(20);
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getPageSize());
        Page<CardTransaction> page;

        if (request.getStatus() != null && request.getType() != null) {
            page = cardTransactionRepository.findByCardIdAndStatusAndType(parsedCardId, request.getStatus(), request.getType(), pageable);
        } else if (request.getStatus() != null) {
            page = cardTransactionRepository.findByCardIdAndStatus(parsedCardId, request.getStatus(), pageable);
        } else if (request.getType() != null) {
            page = cardTransactionRepository.findByCardIdAndType(parsedCardId, request.getType(), pageable);
        } else {
            page = cardTransactionRepository.findByCardId(parsedCardId, pageable);
        }

        return CardMapper.toPaginatedResponse(page);
    }
}
