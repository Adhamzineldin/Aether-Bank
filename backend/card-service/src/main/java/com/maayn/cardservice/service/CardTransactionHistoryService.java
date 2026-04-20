package com.maayn.cardservice.service;

import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.repository.CardTransactionRepository;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.GetCardTransactionsException;
import maayn.veld.generated.models.card.GetCardTransactionsRequest;
import maayn.veld.generated.models.card.PaginatedCardTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardTransactionHistoryService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CardAccessService cardAccessService;
    private final CardTransactionRepository cardTransactionRepository;

    @Transactional(readOnly = true)
    public PaginatedCardTransactionResponse getTransactions(String cardId, GetCardTransactionsRequest input) throws GetCardTransactionsException {
        UUID parsedCardId = cardAccessService.getExistingCardId(cardId);
        GetCardTransactionsRequest request = input != null ? input : new GetCardTransactionsRequest();
        applyDefaultPagination(request);
        return CardMapper.toPaginatedResponse(queryPage(parsedCardId, request));
    }

    private Page<CardTransaction> queryPage(UUID cardId, GetCardTransactionsRequest request) {
        var pageable = PageRequest.of(request.getPage(), request.getPageSize());
        boolean hasStatus = request.getStatus() != null;
        boolean hasType = request.getType() != null;
        if (hasStatus && hasType) return cardTransactionRepository.findByCardIdAndStatusAndType(cardId, request.getStatus(), request.getType(), pageable);
        if (hasStatus) return cardTransactionRepository.findByCardIdAndStatus(cardId, request.getStatus(), pageable);
        if (hasType) return cardTransactionRepository.findByCardIdAndType(cardId, request.getType(), pageable);
        return cardTransactionRepository.findByCardId(cardId, pageable);
    }

    private void applyDefaultPagination(GetCardTransactionsRequest request) {
        if (request.getPage() == null) request.setPage(DEFAULT_PAGE);
        if (request.getPageSize() == null) request.setPageSize(DEFAULT_PAGE_SIZE);
    }
}
