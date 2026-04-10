package com.maayn.transactionservice.integration;

import com.maayn.transactionservice.entity.LedgerBalance;
import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.events.TransactionEventPublisher;
import com.maayn.transactionservice.handlers.TransferIdempotencyHandler;
import com.maayn.transactionservice.listeners.AccountEventListener;
import com.maayn.transactionservice.listeners.LedgerCommandListener;
import com.maayn.transactionservice.repository.LedgerBalanceRepository;
import com.maayn.transactionservice.repository.OutboxRepository;
import com.maayn.transactionservice.repository.TransactionRepository;
import com.maayn.transactionservice.service.LedgerService;
import com.maayn.transactionservice.service.TransactionService;
import com.maayn.transactionservice.validators.TransactionValidator;
import maayn.veld.generated.errors.TransferException;
import maayn.veld.generated.models.transaction.TransactionResponse;
import maayn.veld.generated.models.transaction.TransactionStatus;
import maayn.veld.generated.models.transaction.TransactionType;
import maayn.veld.generated.models.transaction.TransferRequest;
import maayn.veld.generated.sdk.account.models.shared.AccountCreatedEvent;
import maayn.veld.generated.sdk.notification.models.shared.TransferFailedEvent;
import maayn.veld.generated.sdk.notification.models.shared.TransferSuccessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration-style tests simulating the FULL end-to-end RabbitMQ message flows:
 *
 * 1. Account Service → AccountCreatedEvent → Transaction Service → Ledger initialized
 * 2. Financial/Card Service → SAGA TransferRequest → Transaction Service → Transaction created + outbox event
 * 3. Card Service payment flow → Success/Failure
 *
 * Uses mocked repositories but wires real service classes together to verify
 * the complete flow through all layers (listener → service → validator → ledger → outbox).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("End-to-End RabbitMQ Integration Simulation Tests")
class RabbitMQIntegrationSimulationTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private LedgerBalanceRepository ledgerBalanceRepository;
    @Mock private OutboxRepository outboxRepository;

    // Real instances wired together
    private TransactionValidator validator;
    private TransferIdempotencyHandler idempotencyHandler;
    private TransactionEventPublisher eventPublisher;
    private LedgerService ledgerService;
    private TransactionService transactionService;
    private AccountEventListener accountEventListener;
    private LedgerCommandListener ledgerCommandListener;

    private UUID aliceAccountId;
    private UUID bobAccountId;
    private UUID merchantAccountId;

    @BeforeEach
    void setUp() {
        validator = new TransactionValidator();
        idempotencyHandler = new TransferIdempotencyHandler(transactionRepository);
        eventPublisher = new TransactionEventPublisher(outboxRepository, new com.fasterxml.jackson.databind.ObjectMapper());
        ledgerService = new LedgerService(ledgerBalanceRepository);
        transactionService = new TransactionService(
                transactionRepository, ledgerService, eventPublisher, validator, idempotencyHandler
        );
        accountEventListener = new AccountEventListener(ledgerBalanceRepository);
        ledgerCommandListener = new LedgerCommandListener(transactionService, eventPublisher);

        aliceAccountId = UUID.randomUUID();
        bobAccountId = UUID.randomUUID();
        merchantAccountId = UUID.randomUUID();
    }

    // ════════════════════════════════════════════════════════════════
    //  SCENARIO 1: Account Service sends AccountCreatedEvent
    //  Simulates: Account Service → RabbitMQ → ledger.account.events.queue
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Scenario 1: Account Service → AccountCreatedEvent → Ledger Initialization")
    class AccountServiceEventTests {

        @Test
        @DisplayName("Full flow: Account service creates account → Ledger balance initialized at $0.00")
        void accountCreated_initializesLedgerBalance() {
            AccountCreatedEvent event = new AccountCreatedEvent(aliceAccountId, "USD", LocalDateTime.now());

            when(ledgerBalanceRepository.existsById(aliceAccountId)).thenReturn(false);

            // Simulate the RabbitMQ message arriving at our listener
            accountEventListener.handleAccountCreated(event);

            // Verify ledger was created with $0.00 balance
            ArgumentCaptor<LedgerBalance> captor = ArgumentCaptor.forClass(LedgerBalance.class);
            verify(ledgerBalanceRepository).save(captor.capture());

            LedgerBalance created = captor.getValue();
            assertThat(created.getAccountId()).isEqualTo(aliceAccountId);
            assertThat(created.getAvailableBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(created.getPendingHolds()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Full flow: Multiple accounts created → Each gets its own ledger")
        void multipleAccountsCreated_eachGetsLedger() {
            when(ledgerBalanceRepository.existsById(any())).thenReturn(false);

            accountEventListener.handleAccountCreated(new AccountCreatedEvent(aliceAccountId, "USD", LocalDateTime.now()));
            accountEventListener.handleAccountCreated(new AccountCreatedEvent(bobAccountId, "USD", LocalDateTime.now()));
            accountEventListener.handleAccountCreated(new AccountCreatedEvent(merchantAccountId, "EUR", LocalDateTime.now()));

            verify(ledgerBalanceRepository, times(3)).save(any(LedgerBalance.class));
        }

        @Test
        @DisplayName("Full flow: Duplicate AccountCreatedEvent → No duplicate ledger")
        void duplicateAccountCreated_noDoubleLedger() {
            when(ledgerBalanceRepository.existsById(aliceAccountId))
                    .thenReturn(false)  // first call
                    .thenReturn(true);  // second call (already exists)

            accountEventListener.handleAccountCreated(new AccountCreatedEvent(aliceAccountId, "USD", LocalDateTime.now()));
            accountEventListener.handleAccountCreated(new AccountCreatedEvent(aliceAccountId, "USD", LocalDateTime.now()));

            verify(ledgerBalanceRepository, times(1)).save(any(LedgerBalance.class));
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  SCENARIO 2: Financial Service sends SAGA TransferRequest
    //  Simulates: Financial Service → RabbitMQ → ledger.commands.queue
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Scenario 2: Financial Service → SAGA TransferRequest → Transaction Created")
    class FinancialServiceSagaTests {

        @Test
        @DisplayName("Full flow: Financial service initiates transfer → Transaction persisted + outbox event created")
        void sagaTransfer_success_transactionCreatedAndEventPublished() throws Exception {
            TransferRequest command = buildTransferCommand(aliceAccountId, bobAccountId, "150.00", "saga-fin-001");

            // Alice has $500, Bob has $100
            setupLedgerBalance(aliceAccountId, "500.00");
            setupLedgerBalance(bobAccountId, "100.00");

            // No existing transaction for this idempotency key
            when(transactionRepository.findByIdempotencyKey("saga-fin-001")).thenReturn(Optional.empty());

            // Capture the saved transaction
            when(transactionRepository.saveAndFlush(any(Transaction.class))).thenAnswer(inv -> {
                Transaction txn = inv.getArgument(0);
                txn.setId(UUID.randomUUID());
                txn.setCreatedAt(LocalDateTime.now());
                return txn;
            });

            // Simulate the SAGA command arriving via RabbitMQ
            ledgerCommandListener.handleExecuteTransferCommand(command);

            // Verify: transaction was persisted
            ArgumentCaptor<Transaction> txnCaptor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).saveAndFlush(txnCaptor.capture());

            Transaction saved = txnCaptor.getValue();
            assertThat(saved.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
            assertThat(saved.getSourceAccountId()).isEqualTo(aliceAccountId);
            assertThat(saved.getDestinationAccountId()).isEqualTo(bobAccountId);
            assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(saved.getCurrency()).isEqualTo("USD");
            assertThat(saved.getIdempotencyKey()).isEqualTo("saga-fin-001");
            assertThat(saved.getReferenceNumber()).startsWith("TXN-");

            // Verify: outbox success event was created
            verify(outboxRepository).save(any());
        }

        @Test
        @DisplayName("Full flow: Financial service transfer with insufficient funds → Failure event published")
        void sagaTransfer_insufficientFunds_failureEventPublished() throws Exception {
            TransferRequest command = buildTransferCommand(aliceAccountId, bobAccountId, "1000.00", "saga-fin-002");

            // Alice only has $50
            setupLedgerBalance(aliceAccountId, "50.00");
            setupLedgerBalance(bobAccountId, "100.00");

            when(transactionRepository.findByIdempotencyKey("saga-fin-002")).thenReturn(Optional.empty());

            // Simulate the SAGA command arriving via RabbitMQ
            ledgerCommandListener.handleExecuteTransferCommand(command);

            // Verify: transaction was NOT persisted (failed before save)
            verify(transactionRepository, never()).saveAndFlush(any());

            // Verify: failure event was published to outbox
            verify(outboxRepository).save(argThat(msg ->
                    msg.getPayload().contains("Insufficient funds") &&
                    msg.getRoutingKey().equals("transaction.transfer.failed")
            ));
        }

        @Test
        @DisplayName("Full flow: Idempotent duplicate SAGA command → Returns cached response, no double processing")
        void sagaTransfer_idempotentDuplicate_noDoubleProcessing() throws Exception {
            TransferRequest command = buildTransferCommand(aliceAccountId, bobAccountId, "200.00", "saga-fin-003");

            // Simulate existing transaction
            Transaction existing = Transaction.builder()
                    .id(UUID.randomUUID())
                    .sourceAccountId(aliceAccountId)
                    .destinationAccountId(bobAccountId)
                    .amount(new BigDecimal("200.00"))
                    .status(TransactionStatus.SUCCESS)
                    .referenceNumber("TXN-EXISTING")
                    .idempotencyKey("saga-fin-003")
                    .currency("USD")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(transactionRepository.findByIdempotencyKey("saga-fin-003")).thenReturn(Optional.of(existing));

            // Simulate the SAGA command arriving again
            ledgerCommandListener.handleExecuteTransferCommand(command);

            // Verify: ledger was NOT touched
            verify(ledgerBalanceRepository, never()).saveAll(anyList());
            // Verify: no new transaction saved
            verify(transactionRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Full flow: Transfer validates different source and destination accounts")
        void sagaTransfer_sameAccount_failsValidation() {
            TransferRequest command = buildTransferCommand(aliceAccountId, aliceAccountId, "100.00", "saga-fin-004");

            when(transactionRepository.findByIdempotencyKey("saga-fin-004")).thenReturn(Optional.empty());

            ledgerCommandListener.handleExecuteTransferCommand(command);

            verify(transactionRepository, never()).saveAndFlush(any());
            verify(outboxRepository).save(argThat(msg ->
                    msg.getPayload().contains("cannot be the same") &&
                    msg.getRoutingKey().equals("transaction.transfer.failed")
            ));
        }

        @Test
        @DisplayName("Full flow: Transfer validates positive amount")
        void sagaTransfer_zeroAmount_failsValidation() {
            TransferRequest command = buildTransferCommand(aliceAccountId, bobAccountId, "0", "saga-fin-005");

            when(transactionRepository.findByIdempotencyKey("saga-fin-005")).thenReturn(Optional.empty());

            ledgerCommandListener.handleExecuteTransferCommand(command);

            verify(transactionRepository, never()).saveAndFlush(any());
            verify(outboxRepository).save(argThat(msg ->
                    msg.getPayload().contains("greater than 0") &&
                    msg.getRoutingKey().equals("transaction.transfer.failed")
            ));
        }

        @Test
        @DisplayName("Full flow: Ledger balances are correctly updated after successful transfer")
        void sagaTransfer_ledgerBalancesUpdated() throws Exception {
            TransferRequest command = buildTransferCommand(aliceAccountId, bobAccountId, "300.00", "saga-fin-006");

            LedgerBalance aliceBalance = setupLedgerBalance(aliceAccountId, "1000.00");
            LedgerBalance bobBalance = setupLedgerBalance(bobAccountId, "200.00");

            when(transactionRepository.findByIdempotencyKey("saga-fin-006")).thenReturn(Optional.empty());
            when(transactionRepository.saveAndFlush(any(Transaction.class))).thenAnswer(inv -> {
                Transaction txn = inv.getArgument(0);
                txn.setId(UUID.randomUUID());
                txn.setCreatedAt(LocalDateTime.now());
                return txn;
            });

            ledgerCommandListener.handleExecuteTransferCommand(command);

            // Verify ledger updates
            assertThat(aliceBalance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
            assertThat(bobBalance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("500.00"));

            verify(ledgerBalanceRepository).saveAll(anyList());
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  SCENARIO 3: Card Service sends SAGA Payment Command
    //  Simulates: Card Service → RabbitMQ → ledger.commands.queue
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Scenario 3: Card Service → SAGA Card Payment → Transaction Created")
    class CardServicePaymentTests {

        @Test
        @DisplayName("Full flow: Card payment → Funds debited from customer, credited to merchant")
        void cardPayment_success_fundsTransferred() throws Exception {
            TransferRequest cardCommand = new TransferRequest();
            cardCommand.setIdempotencyKey("card-pay-001");
            cardCommand.setSourceAccountId(aliceAccountId);    // Customer
            cardCommand.setDestinationAccountId(merchantAccountId); // Merchant
            cardCommand.setAmount(new BigDecimal("49.99"));
            cardCommand.setCurrency("USD");
            cardCommand.setType(TransactionType.CARD_PAYMENT);

            LedgerBalance customerBalance = setupLedgerBalance(aliceAccountId, "500.00");
            LedgerBalance merchantBalance = setupLedgerBalance(merchantAccountId, "10000.00");

            when(transactionRepository.findByIdempotencyKey("card-pay-001")).thenReturn(Optional.empty());
            when(transactionRepository.saveAndFlush(any(Transaction.class))).thenAnswer(inv -> {
                Transaction txn = inv.getArgument(0);
                txn.setId(UUID.randomUUID());
                txn.setCreatedAt(LocalDateTime.now());
                return txn;
            });

            ledgerCommandListener.handleExecuteTransferCommand(cardCommand);

            // Customer debited
            assertThat(customerBalance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("450.01"));
            // Merchant credited
            assertThat(merchantBalance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("10049.99"));

            // Transaction persisted
            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).saveAndFlush(captor.capture());
            assertThat(captor.getValue().getTransactionType()).isEqualTo(TransactionType.CARD_PAYMENT);
            assertThat(captor.getValue().getStatus()).isEqualTo(TransactionStatus.SUCCESS);

            // Success event in outbox
            verify(outboxRepository).save(argThat(msg ->
                    msg.getRoutingKey().equals("transaction.transfer.success")
            ));
        }

        @Test
        @DisplayName("Full flow: Card payment declined → Insufficient funds → Failure event published")
        void cardPayment_declined_insufficientFunds() {
            TransferRequest cardCommand = new TransferRequest();
            cardCommand.setIdempotencyKey("card-pay-002");
            cardCommand.setSourceAccountId(aliceAccountId);
            cardCommand.setDestinationAccountId(merchantAccountId);
            cardCommand.setAmount(new BigDecimal("999.99"));
            cardCommand.setCurrency("USD");
            cardCommand.setType(TransactionType.CARD_PAYMENT);

            setupLedgerBalance(aliceAccountId, "10.00"); // Not enough
            setupLedgerBalance(merchantAccountId, "5000.00");

            when(transactionRepository.findByIdempotencyKey("card-pay-002")).thenReturn(Optional.empty());

            ledgerCommandListener.handleExecuteTransferCommand(cardCommand);

            verify(transactionRepository, never()).saveAndFlush(any());
            verify(outboxRepository).save(argThat(msg ->
                    msg.getRoutingKey().equals("transaction.transfer.failed") &&
                    msg.getPayload().contains("Insufficient funds")
            ));
        }

        @Test
        @DisplayName("Full flow: Card payment of $0.01 (minimum) → Succeeds")
        void cardPayment_minimumAmount_succeeds() throws Exception {
            TransferRequest cardCommand = new TransferRequest();
            cardCommand.setIdempotencyKey("card-pay-003");
            cardCommand.setSourceAccountId(aliceAccountId);
            cardCommand.setDestinationAccountId(merchantAccountId);
            cardCommand.setAmount(new BigDecimal("0.01"));
            cardCommand.setCurrency("USD");
            cardCommand.setType(TransactionType.CARD_PAYMENT);

            setupLedgerBalance(aliceAccountId, "100.00");
            setupLedgerBalance(merchantAccountId, "5000.00");

            when(transactionRepository.findByIdempotencyKey("card-pay-003")).thenReturn(Optional.empty());
            when(transactionRepository.saveAndFlush(any(Transaction.class))).thenAnswer(inv -> {
                Transaction txn = inv.getArgument(0);
                txn.setId(UUID.randomUUID());
                txn.setCreatedAt(LocalDateTime.now());
                return txn;
            });

            ledgerCommandListener.handleExecuteTransferCommand(cardCommand);

            verify(transactionRepository).saveAndFlush(any());
        }

        @Test
        @DisplayName("Full flow: Large card payment drains account to zero")
        void cardPayment_drainsToZero() throws Exception {
            TransferRequest cardCommand = new TransferRequest();
            cardCommand.setIdempotencyKey("card-pay-004");
            cardCommand.setSourceAccountId(aliceAccountId);
            cardCommand.setDestinationAccountId(merchantAccountId);
            cardCommand.setAmount(new BigDecimal("500.00"));
            cardCommand.setCurrency("USD");
            cardCommand.setType(TransactionType.CARD_PAYMENT);

            LedgerBalance customerBalance = setupLedgerBalance(aliceAccountId, "500.00");
            setupLedgerBalance(merchantAccountId, "5000.00");

            when(transactionRepository.findByIdempotencyKey("card-pay-004")).thenReturn(Optional.empty());
            when(transactionRepository.saveAndFlush(any(Transaction.class))).thenAnswer(inv -> {
                Transaction txn = inv.getArgument(0);
                txn.setId(UUID.randomUUID());
                txn.setCreatedAt(LocalDateTime.now());
                return txn;
            });

            ledgerCommandListener.handleExecuteTransferCommand(cardCommand);

            assertThat(customerBalance.getAvailableBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  SCENARIO 4: Full Lifecycle — Account Created → Funded → Transfer
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Scenario 4: Full Account Lifecycle — Create → Fund → Transfer")
    class FullLifecycleTests {

        @Test
        @DisplayName("Full lifecycle: Account created → Receives funds → Sends transfer")
        void fullLifecycle_createFundTransfer() throws Exception {
            // Step 1: Account Service creates Alice's account
            when(ledgerBalanceRepository.existsById(aliceAccountId)).thenReturn(false);
            accountEventListener.handleAccountCreated(new AccountCreatedEvent(aliceAccountId, "USD", LocalDateTime.now()));
            verify(ledgerBalanceRepository).save(any(LedgerBalance.class));

            // Step 2: Account Service creates Bob's account
            when(ledgerBalanceRepository.existsById(bobAccountId)).thenReturn(false);
            accountEventListener.handleAccountCreated(new AccountCreatedEvent(bobAccountId, "USD", LocalDateTime.now()));

            // Step 3: Now simulate Alice having funds and transferring to Bob
            LedgerBalance aliceBalance = setupLedgerBalance(aliceAccountId, "1000.00");
            LedgerBalance bobBalance = setupLedgerBalance(bobAccountId, "0.00");

            TransferRequest command = buildTransferCommand(aliceAccountId, bobAccountId, "250.00", "lifecycle-001");
            when(transactionRepository.findByIdempotencyKey("lifecycle-001")).thenReturn(Optional.empty());
            when(transactionRepository.saveAndFlush(any(Transaction.class))).thenAnswer(inv -> {
                Transaction txn = inv.getArgument(0);
                txn.setId(UUID.randomUUID());
                txn.setCreatedAt(LocalDateTime.now());
                return txn;
            });

            ledgerCommandListener.handleExecuteTransferCommand(command);

            // Verify final balances
            assertThat(aliceBalance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("750.00"));
            assertThat(bobBalance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("250.00"));

            // Verify transaction was persisted
            verify(transactionRepository).saveAndFlush(any());
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  SCENARIO 5: Negative Amount / Edge Cases via SAGA
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Scenario 5: Edge Cases via SAGA RabbitMQ Commands")
    class EdgeCaseTests {

        @Test
        @DisplayName("Negative amount in SAGA command → Failure event published")
        void sagaCommand_negativeAmount_publishesFailure() {
            TransferRequest command = buildTransferCommand(aliceAccountId, bobAccountId, "-50.00", "edge-001");
            when(transactionRepository.findByIdempotencyKey("edge-001")).thenReturn(Optional.empty());

            ledgerCommandListener.handleExecuteTransferCommand(command);

            verify(transactionRepository, never()).saveAndFlush(any());
            verify(outboxRepository).save(argThat(msg ->
                    msg.getRoutingKey().equals("transaction.transfer.failed")
            ));
        }

        @Test
        @DisplayName("Zero amount in SAGA command → Failure event published")
        void sagaCommand_zeroAmount_publishesFailure() {
            TransferRequest command = buildTransferCommand(aliceAccountId, bobAccountId, "0", "edge-002");
            when(transactionRepository.findByIdempotencyKey("edge-002")).thenReturn(Optional.empty());

            ledgerCommandListener.handleExecuteTransferCommand(command);

            verify(transactionRepository, never()).saveAndFlush(any());
            verify(outboxRepository).save(argThat(msg ->
                    msg.getRoutingKey().equals("transaction.transfer.failed")
            ));
        }

        @Test
        @DisplayName("Same source and destination in SAGA command → Failure event published")
        void sagaCommand_sameSourceDest_publishesFailure() {
            TransferRequest command = buildTransferCommand(aliceAccountId, aliceAccountId, "100.00", "edge-003");
            when(transactionRepository.findByIdempotencyKey("edge-003")).thenReturn(Optional.empty());

            ledgerCommandListener.handleExecuteTransferCommand(command);

            verify(transactionRepository, never()).saveAndFlush(any());
            verify(outboxRepository).save(argThat(msg ->
                    msg.getRoutingKey().equals("transaction.transfer.failed") &&
                    msg.getPayload().contains("cannot be the same")
            ));
        }

        @Test
        @DisplayName("Multiple rapid SAGA commands with same idempotency key → Only first processed")
        void sagaCommand_rapidDuplicates_onlyFirstProcessed() throws Exception {
            TransferRequest command = buildTransferCommand(aliceAccountId, bobAccountId, "100.00", "edge-004");

            setupLedgerBalance(aliceAccountId, "500.00");
            setupLedgerBalance(bobAccountId, "200.00");

            // First call: not yet processed
            when(transactionRepository.findByIdempotencyKey("edge-004"))
                    .thenReturn(Optional.empty());
            when(transactionRepository.saveAndFlush(any(Transaction.class))).thenAnswer(inv -> {
                Transaction txn = inv.getArgument(0);
                txn.setId(UUID.randomUUID());
                txn.setCreatedAt(LocalDateTime.now());
                return txn;
            });

            ledgerCommandListener.handleExecuteTransferCommand(command);

            // Second call: already processed
            Transaction existing = Transaction.builder()
                    .referenceNumber("TXN-EXIST")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .createdAt(LocalDateTime.now())
                    .build();
            when(transactionRepository.findByIdempotencyKey("edge-004"))
                    .thenReturn(Optional.of(existing));

            ledgerCommandListener.handleExecuteTransferCommand(command);

            // Only one saveAndFlush call (first invocation)
            verify(transactionRepository, times(1)).saveAndFlush(any());
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════

    private TransferRequest buildTransferCommand(UUID source, UUID dest, String amount, String idempotencyKey) {
        TransferRequest cmd = new TransferRequest();
        cmd.setIdempotencyKey(idempotencyKey);
        cmd.setSourceAccountId(source);
        cmd.setDestinationAccountId(dest);
        cmd.setAmount(new BigDecimal(amount));
        cmd.setCurrency("USD");
        cmd.setType(TransactionType.TRANSFER);
        return cmd;
    }

    private LedgerBalance setupLedgerBalance(UUID accountId, String balance) {
        LedgerBalance ledger = new LedgerBalance(accountId);
        ledger.setAvailableBalance(new BigDecimal(balance));
        ledger.setCurrency("USD");

        when(ledgerBalanceRepository.getLedgerBalanceByAccountId(accountId))
                .thenReturn(Optional.of(ledger));
        return ledger;
    }
}

