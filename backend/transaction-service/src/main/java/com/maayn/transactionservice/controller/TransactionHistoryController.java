package com.maayn.transactionservice.controller;

import com.maayn.transactionservice.dto.AccountStatementRow;
import com.maayn.transactionservice.dto.PaginatedAccountStatement;
import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Account statement endpoint. Exists alongside the Veld-generated
 * {@code getAccountTransactions} because (a) that action uses a GET with a
 * {@code @RequestBody}, which browsers can't reliably send, and (b) the
 * generated {@code TransactionResponse} strips currency and direction, which
 * renders FX transfers incorrectly from the recipient's perspective.
 *
 * <p>Rows returned here are shaped from the queried account's perspective:
 * {@code amount}/{@code currency} are the viewer's leg of the transfer, and
 * {@code counterpartyAmount}/{@code counterpartyCurrency} surface the other
 * leg so the UI can display FX context when the two differ.
 */
@RestController
@RequestMapping("/api/transaction_service/transactions/history")
@RequiredArgsConstructor
public class TransactionHistoryController {

    private final TransactionRepository transactionRepository;

    @GetMapping("/{accountId}")
    public ResponseEntity<PaginatedAccountStatement> getAccountStatement(
            @PathVariable("accountId") UUID accountId,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {

        Page<Transaction> pageResult = transactionRepository.findByAccountWallet(
                accountId, currency, PageRequest.of(page, pageSize));

        List<AccountStatementRow> rows = pageResult.getContent().stream()
                .map(tx -> toRow(tx, accountId))
                .toList();

        return ResponseEntity.ok(new PaginatedAccountStatement(
                rows,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()));
    }

    /**
     * Project a raw transaction into the viewer's perspective. For same-currency
     * transfers {@code amount} and {@code counterpartyAmount} are identical; for
     * FX transfers they differ and the exchange rate ties them together.
     */
    private static AccountStatementRow toRow(Transaction tx, UUID viewerAccountId) {
        boolean viewerIsSource = viewerAccountId.equals(tx.getSourceAccountId());

        BigDecimal sourceAmount = tx.getAmount();
        BigDecimal destAmount = tx.getDestinationAmount() != null ? tx.getDestinationAmount() : sourceAmount;
        String sourceCurrency = tx.getSourceCurrency() != null ? tx.getSourceCurrency() : tx.getCurrency();
        String destCurrency = tx.getDestinationCurrency() != null ? tx.getDestinationCurrency() : tx.getCurrency();

        BigDecimal viewerAmount;
        String viewerCurrency;
        BigDecimal counterpartyAmount;
        String counterpartyCurrency;
        UUID counterpartyAccountId;
        AccountStatementRow.Direction direction;

        if (viewerIsSource) {
            viewerAmount = sourceAmount;
            viewerCurrency = sourceCurrency;
            counterpartyAmount = destAmount;
            counterpartyCurrency = destCurrency;
            counterpartyAccountId = tx.getDestinationAccountId();
            direction = AccountStatementRow.Direction.DEBIT;
        } else {
            viewerAmount = destAmount;
            viewerCurrency = destCurrency;
            counterpartyAmount = sourceAmount;
            counterpartyCurrency = sourceCurrency;
            counterpartyAccountId = tx.getSourceAccountId();
            direction = AccountStatementRow.Direction.CREDIT;
        }

        return new AccountStatementRow(
                tx.getReferenceNumber(),
                tx.getCreatedAt(),
                tx.getStatus(),
                tx.getTransactionType(),
                direction,
                viewerAmount,
                viewerCurrency,
                counterpartyAccountId,
                counterpartyAmount,
                counterpartyCurrency,
                tx.getExchangeRate());
    }
}
