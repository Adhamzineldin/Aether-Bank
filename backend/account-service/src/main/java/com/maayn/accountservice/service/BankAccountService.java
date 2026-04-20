package com.maayn.accountservice.service;

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

    @Transactional
    public AccountResponse openAccount(OpenAccountRequest request) {
        log.info("Opening new account for customer: {}", request.getCustomerId());

        // Verify customer exists
        if (!customerRepository.existsById(request.getCustomerId())) {
            throw new AccountNotFoundException("Customer not found with ID: " + request.getCustomerId());
        }

        // Create bank account
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

        // TODO: If initialDeposit is provided, call Transaction Service to deposit
        // For now, just return account with zero balance
        return mapToResponse(savedAccount, BigDecimal.ZERO);
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
            // TODO: Trigger transfer to transferToAccountId via Transaction Service
            log.warn("TODO: Transfer remaining balance {} to account {}", 
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

        return mapToResponse(closedAccount, BigDecimal.ZERO);
    }

    @Transactional
    public AccountResponse updateAccountStatus(UUID accountId, UpdateAccountStatusRequest request) {
        log.info("Updating status for account: {} to {}", accountId, request.getStatus());

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        // Validate status transition
        validateStatusTransition(account.getStatus(), request.getStatus());

        account.setStatus(request.getStatus());
        BankAccount updatedAccount = bankAccountRepository.save(account);

        BigDecimal balance = getBalanceFromTransactionService(accountId, account.getCurrency());

        log.info("Account status updated: {} -> {}", accountId, request.getStatus());

        return mapToResponse(updatedAccount, balance);
    }

    @Transactional(readOnly = true)
    public boolean doesAccountExist(UUID accountId) {
        return bankAccountRepository.existsById(accountId);
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
}

