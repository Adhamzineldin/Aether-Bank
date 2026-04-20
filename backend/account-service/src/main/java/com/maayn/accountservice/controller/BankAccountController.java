package com.maayn.accountservice.controller;

import com.maayn.accountservice.dto.*;
import com.maayn.accountservice.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    public ResponseEntity<AccountResponse> openAccount(@Valid @RequestBody OpenAccountRequest request) {
        AccountResponse response = bankAccountService.openAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID accountId) {
        AccountResponse response = bankAccountService.getAccount(accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> listCustomerAccounts(@PathVariable UUID customerId) {
        List<AccountResponse> accounts = bankAccountService.listCustomerAccounts(customerId);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/{accountId}/close")
    public ResponseEntity<AccountResponse> closeAccount(
            @PathVariable UUID accountId,
            @Valid @RequestBody CloseAccountRequest request) {
        AccountResponse response = bankAccountService.closeAccount(accountId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{accountId}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable UUID accountId,
            @Valid @RequestBody UpdateAccountStatusRequest request) {
        AccountResponse response = bankAccountService.updateAccountStatus(accountId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountId}/exists")
    public ResponseEntity<Boolean> doesAccountExist(@PathVariable UUID accountId) {
        boolean exists = bankAccountService.doesAccountExist(accountId);
        return ResponseEntity.ok(exists);
    }
}

