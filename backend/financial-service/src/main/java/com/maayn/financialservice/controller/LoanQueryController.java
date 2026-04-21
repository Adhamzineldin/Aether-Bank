package com.maayn.financialservice.controller;

import com.maayn.financialservice.entity.LoanApplicationDocument;
import com.maayn.financialservice.mappers.LoanMapper;
import com.maayn.financialservice.repo.LoanRepo;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.models.loan.LoanApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Hand-written read endpoints that complement the Veld-generated
 * {@code LoanController} (which only exposes the submit endpoint). These are
 * needed by the frontend "my loans" list and loan detail pages.
 */
@RestController
@RequestMapping("/api/financial_service/api/loan")
@RequiredArgsConstructor
public class LoanQueryController {

    private final LoanRepo loanRepo;
    private final LoanMapper loanMapper;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanApplication>> listCustomerLoans(@PathVariable UUID customerId) {
        List<LoanApplication> result = loanRepo.findByCustomerId(customerId).stream()
                .map(loanMapper::toModel)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanApplication> getLoan(@PathVariable UUID loanId) {
        LoanApplicationDocument doc = loanRepo.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
        return ResponseEntity.ok(loanMapper.toModel(doc));
    }
}
