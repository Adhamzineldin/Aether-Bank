package com.maayn.transactionservice.jobs;

import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.repository.TransactionRepository;
import com.maayn.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.transaction.TransactionStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionReconciliationSweeper {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private static final String TIMEOUT_REASON = "SAGA Timeout: System failed to respond within 10 minutes.";

    @Scheduled(fixedRate = 300000)
    public void sweepStuckTransactions() {
        log.info("Starting Reconciliation Sweep for stuck transactions...");

        List<Transaction> stuckTransactions = fetchStuckTransactions();

        if (stuckTransactions.isEmpty()) {
            log.info("Sweep complete. No stuck transactions found.");
            return;
        }

        log.warn("Found {} stuck PENDING transactions. Forcing rollbacks.", stuckTransactions.size());
        processBatch(stuckTransactions);
    }

 
    private List<Transaction> fetchStuckTransactions() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        return transactionRepository.findByStatusAndCreatedAtBefore(TransactionStatus.PENDING, tenMinutesAgo);
    }
    
    private void processBatch(List<Transaction> transactions) {
        for (Transaction tx : transactions) {
            reconcileSingleTransaction(tx);
        }
    }
    private void reconcileSingleTransaction(Transaction tx) {
        try {
            transactionService.finalizeTransaction(
                    tx.getReferenceNumber(),
                    TransactionStatus.FAILED,
                    TIMEOUT_REASON
            );
        } catch (Exception e) {
            log.error("Failed to reconcile stuck transaction {}: {}", tx.getReferenceNumber(), e.getMessage());
        }
    }
}