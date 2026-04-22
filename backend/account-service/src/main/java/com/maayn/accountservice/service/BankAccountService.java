package com.maayn.accountservice.service;

import com.maayn.accountservice.audit.AuditPublisher;
import com.maayn.accountservice.client.TransactionServiceClient;
import com.maayn.accountservice.dto.*;
import com.maayn.accountservice.entity.BankAccount;
import com.maayn.accountservice.enums.AccountStatus;
import com.maayn.accountservice.events.AccountClosedEvent;
import com.maayn.accountservice.events.AccountCreatedEvent;
import com.maayn.accountservice.events.AccountEventPublisher;
import com.maayn.accountservice.exception.AccountHasBalanceException;
import com.maayn.accountservice.exception.AccountNotFoundException;
import com.maayn.accountservice.exception.InvalidAccountStatusException;
import com.maayn.accountservice.repository.BankAccountRepository;
import com.maayn.accountservice.repository.CustomerRepository;
import com.maayn.accountservice.util.AccountNumberGenerator;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountEventPublisher eventPublisher;
    private final TransactionServiceClient transactionServiceClient;
    private final CustomerRepository customerRepository;
    private final AuditPublisher auditPublisher;

    @Transactional
    public AccountResponse openAccount(OpenAccountRequest request) {
        log.info("Opening new account for customer: {}", request.getCustomerId());

        try {
            // Skipping local customer existence check — customer identity is owned by IAM service.
            BankAccount account = BankAccount.builder()
                    .accountNumber(accountNumberGenerator.generate())
                    .customerId(request.getCustomerId())
                    .accountType(request.getAccountType())
                    .status(AccountStatus.ACTIVE)
                    .currency(request.getCurrency())
                    .openedDate(LocalDate.now())
                    .build();

            BankAccount savedAccount = bankAccountRepository.save(account);

            // Publish event to initialize ledger in Transaction Service
            AccountCreatedEvent event = AccountCreatedEvent.builder()
                    .accountId(savedAccount.getId())
                    .currency(savedAccount.getCurrency())
                    .timestamp(LocalDateTime.now())
                    .build();
            eventPublisher.publishAccountCreated(event);

            log.info("Account created successfully: {}", savedAccount.getAccountNumber());

            auditPublisher.publishSuccess(
                    "OPEN_ACCOUNT",
                    savedAccount.getCustomerId(),
                    String.format("Opened %s account %s (%s) for customer %s in %s",
                            savedAccount.getAccountType(), savedAccount.getAccountNumber(),
                            savedAccount.getId(), savedAccount.getCustomerId(), savedAccount.getCurrency()));

            return mapToResponse(savedAccount, BigDecimal.ZERO);
        } catch (RuntimeException ex) {
            auditPublisher.publishFailure(
                    "OPEN_ACCOUNT",
                    request.getCustomerId(),
                    String.format("Failed to open %s account for customer %s: %s",
                            request.getAccountType(), request.getCustomerId(), ex.getMessage()));
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID accountId) {
        log.info("Fetching account: {}", accountId);

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        BigDecimal balance = getBalanceFromTransactionService(accountId, account.getCurrency());

        return mapToResponse(account, balance);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listCustomerAccounts(UUID customerId) {
        log.info("Listing accounts for customer: {}", customerId);

        List<BankAccount> accounts = bankAccountRepository.findByCustomerId(customerId);

        if (accounts.isEmpty()) {
            throw new AccountNotFoundException("No accounts found for customer: " + customerId);
        }

        return accounts.stream()
                .map(account -> {
                    BigDecimal balance = getBalanceFromTransactionService(account.getId(), account.getCurrency());
                    return mapToResponse(account, balance);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse closeAccount(UUID accountId, CloseAccountRequest request) {
        log.info("Closing account: {}", accountId);

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        try {
            if (account.getStatus() == AccountStatus.CLOSED) {
                throw new InvalidAccountStatusException("Account is already closed");
            }

            // Check balance
            BigDecimal balance = getBalanceFromTransactionService(accountId, account.getCurrency());
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                if (request.getTransferToAccountId() == null) {
                    throw new AccountHasBalanceException(
                            "Account has balance. Please transfer funds or provide transferToAccountId");
                }
                // Transfer remaining balance to specified account
                // This should be done via Transaction Service transfer endpoint
                log.info("Remaining balance {} should be transferred to account {} via Transaction Service",
                        balance, request.getTransferToAccountId());
            }

            account.setStatus(AccountStatus.CLOSED);
            account.setClosedDate(LocalDate.now());
            BankAccount closedAccount = bankAccountRepository.save(account);

            // Publish event
            AccountClosedEvent event = AccountClosedEvent.builder()
                    .accountId(closedAccount.getId())
                    .customerId(closedAccount.getCustomerId())
                    .timestamp(LocalDateTime.now())
                    .build();
            eventPublisher.publishAccountClosed(event);

            log.info("Account closed successfully: {}", accountId);

            auditPublisher.publishSuccess(
                    "CLOSE_ACCOUNT",
                    closedAccount.getCustomerId(),
                    String.format("Closed account %s (%s)", closedAccount.getAccountNumber(), accountId));

            return mapToResponse(closedAccount, BigDecimal.ZERO);
        } catch (RuntimeException ex) {
            auditPublisher.publishFailure(
                    "CLOSE_ACCOUNT",
                    account.getCustomerId(),
                    String.format("Failed to close account %s: %s", accountId, ex.getMessage()));
            throw ex;
        }
    }

    @Transactional
    public AccountResponse updateAccountStatus(UUID accountId, UpdateAccountStatusRequest request) {
        log.info("Updating status for account: {} to {}", accountId, request.getStatus());

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        try {
            // Validate status transition
            validateStatusTransition(account.getStatus(), request.getStatus());

            AccountStatus previous = account.getStatus();
            account.setStatus(request.getStatus());
            BankAccount updatedAccount = bankAccountRepository.save(account);

            BigDecimal balance = getBalanceFromTransactionService(accountId, account.getCurrency());

            log.info("Account status updated: {} -> {}", accountId, request.getStatus());

            auditPublisher.publishSuccess(
                    "UPDATE_ACCOUNT_STATUS",
                    updatedAccount.getCustomerId(),
                    String.format("Account %s status %s -> %s",
                            accountId, previous, request.getStatus()));

            return mapToResponse(updatedAccount, balance);
        } catch (RuntimeException ex) {
            auditPublisher.publishFailure(
                    "UPDATE_ACCOUNT_STATUS",
                    account.getCustomerId(),
                    String.format("Failed to update account %s status to %s: %s",
                            accountId, request.getStatus(), ex.getMessage()));
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public boolean doesAccountExist(UUID accountId) {
        return bankAccountRepository.existsById(accountId);
    }

    /**
     * Resolve a human-readable account number to the full account DTO. Used by
     * the transfer flow so customers can send to an account number / IBAN
     * rather than a raw UUID.
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        BigDecimal balance = getBalanceFromTransactionService(account.getId(), account.getCurrency());
        return mapToResponse(account, balance);
    }

    private BigDecimal getBalanceFromTransactionService(UUID accountId, String currency) {
        try {
            BalanceResponse balanceResponse = transactionServiceClient.getAccountBalance(accountId, currency);
            return balanceResponse.getAvailableBalance();
        } catch (FeignException.NotFound e) {
            log.warn("Ledger not initialized for account: {}, returning 0", accountId);
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Failed to fetch balance from Transaction Service for account: {}", accountId, e);
            return BigDecimal.ZERO; // Fallback
        }
    }

    private void validateStatusTransition(AccountStatus currentStatus, AccountStatus newStatus) {
        if (currentStatus == AccountStatus.CLOSED) {
            throw new InvalidAccountStatusException("Cannot change status of a closed account");
        }

        if (newStatus == AccountStatus.CLOSED) {
            throw new InvalidAccountStatusException("Use closeAccount endpoint to close an account");
        }

        // Allow ACTIVE <-> FROZEN
        // PENDING can go to ACTIVE only
        if (currentStatus == AccountStatus.PENDING && newStatus != AccountStatus.ACTIVE) {
            throw new InvalidAccountStatusException("Pending account can only be activated");
        }
    }

    private AccountResponse mapToResponse(BankAccount account, BigDecimal balance) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .iban(deriveIban(account.getAccountNumber()))
                .customerId(account.getCustomerId())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .currency(account.getCurrency())
                .openedDate(account.getOpenedDate())
                .closedDate(account.getClosedDate())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .balance(balance)
                .build();
    }

    /**
     * Synthesise a 24-char demo IBAN ({@code AE} country code + 2 check digits
     * + 4-char bank code + right-padded account number). Good enough for the
     * demo — <b>not</b> a real IBAN algorithm.
     */
    private String deriveIban(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) return null;
        String digitsOnly = accountNumber.replaceAll("[^A-Za-z0-9]", "");
        String padded = (digitsOnly + "0000000000000000").substring(0, 16);
        return "AE07AETH" + padded;
    }

    private void depositInitialAmount(UUID accountId, BigDecimal amount, String currency) {
        // This would call Transaction Service to deposit initial amount
        // For now, the ledger is initialized with 0 balance via AccountCreatedEvent
        // Initial deposit can be done via Transaction Service /deposit endpoint
        log.info("Initial deposit of {} {} requested for account {}", amount, currency, accountId);
    }

    private void transferRemainingBalance(UUID sourceAccountId, UUID destAccountId, 
                                         BigDecimal amount, String currency) {
        // This would call Transaction Service to transfer remaining balance
        // Implementation would use TransactionClient SDK if available
        log.info("Transferring {} {} from {} to {}", amount, currency, sourceAccountId, destAccountId);
    }
}
