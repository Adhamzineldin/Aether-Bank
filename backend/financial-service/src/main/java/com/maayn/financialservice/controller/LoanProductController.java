package com.maayn.financialservice.controller;

import com.maayn.financialservice.entity.LoanProductDefinitionDocument;
import com.maayn.financialservice.repo.LoanProductDefinitionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only catalog endpoints for loan + mortgage product definitions. The
 * frontend uses these to populate product dropdowns so customers pick a real
 * product instead of typing a raw UUID.
 */
@RestController
@RequestMapping("/api/financial_service/products")
@RequiredArgsConstructor
public class LoanProductController {

    private final LoanProductDefinitionRepo loanProductRepo;

    @GetMapping("/loans")
    public ResponseEntity<List<LoanProductDefinitionDocument>> listActiveLoanProducts() {
        List<LoanProductDefinitionDocument> active = loanProductRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .toList();
        return ResponseEntity.ok(active);
    }
}
