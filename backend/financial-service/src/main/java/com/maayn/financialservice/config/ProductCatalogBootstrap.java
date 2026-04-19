package com.maayn.financialservice.config;

import com.maayn.financialservice.domain.certificate.*;
import com.maayn.financialservice.domain.loan.*;
import com.maayn.financialservice.entity.CertificateProductDefinitionDocument;
import com.maayn.financialservice.entity.LoanProductDefinitionDocument;
import com.maayn.financialservice.repo.CertificateProductDefinitionRepo;
import com.maayn.financialservice.repo.LoanProductDefinitionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class ProductCatalogBootstrap {

    @Bean
    CommandLineRunner seedFinancialProducts(LoanProductDefinitionRepo loanRepo, CertificateProductDefinitionRepo certificateRepo) {
        return args -> {
            if (loanRepo.count() == 0) {
                loanRepo.saveAll(List.of(
                        loan(LoanType.MORTGAGE, "mortgage", "Mortgage", LoanInterestMethod.AMORTIZED, LoanRateMode.FIXED, LoanRepaymentMethod.INSTALLMENT, new BigDecimal("0.065"), new BigDecimal("0"), new BigDecimal("0.03"), 360, 120, 360, new BigDecimal("50000"), new BigDecimal("5000000")),
                        loan(LoanType.AUTO, "auto", "Auto Loan", LoanInterestMethod.AMORTIZED, LoanRateMode.FIXED, LoanRepaymentMethod.INSTALLMENT, new BigDecimal("0.085"), new BigDecimal("0"), new BigDecimal("0.05"), 60, 12, 84, new BigDecimal("1000"), new BigDecimal("500000")),
                        loan(LoanType.PERSONAL, "personal", "Personal Loan", LoanInterestMethod.AMORTIZED, LoanRateMode.FIXED, LoanRepaymentMethod.INSTALLMENT, new BigDecimal("0.12"), new BigDecimal("0"), new BigDecimal("0.05"), 36, 6, 84, new BigDecimal("500"), new BigDecimal("250000")),
                        loan(LoanType.STUDENT, "student", "Student Loan", LoanInterestMethod.INTEREST_ONLY, LoanRateMode.VARIABLE, LoanRepaymentMethod.INSTALLMENT, new BigDecimal("0.075"), new BigDecimal("0"), new BigDecimal("0.03"), 120, 36, 240, new BigDecimal("1000"), new BigDecimal("2000000")),
                        loan(LoanType.BUSINESS, "business", "Business Loan", LoanInterestMethod.INTEREST_ONLY, LoanRateMode.VARIABLE, LoanRepaymentMethod.BULLET, new BigDecimal("0.105"), new BigDecimal("15"), new BigDecimal("0.05"), 24, 6, 120, new BigDecimal("10000"), new BigDecimal("10000000")),
                        loan(LoanType.CREDIT_CARD, "credit-card", "Credit Card", LoanInterestMethod.COMPOUND, LoanRateMode.VARIABLE, LoanRepaymentMethod.REVOLVING, new BigDecimal("0.24"), new BigDecimal("0"), new BigDecimal("0.03"), 9999, 1, 9999, new BigDecimal("100"), new BigDecimal("200000")),
                        loan(LoanType.PAYDAY, "payday", "Payday Loan", LoanInterestMethod.SIMPLE, LoanRateMode.FIXED, LoanRepaymentMethod.BULLET, new BigDecimal("0.36"), new BigDecimal("25"), new BigDecimal("0.10"), 1, 1, 60, new BigDecimal("100"), new BigDecimal("10000")),
                        loan(LoanType.BRIDGE, "bridge", "Bridge Loan", LoanInterestMethod.INTEREST_ONLY, LoanRateMode.FIXED, LoanRepaymentMethod.BULLET, new BigDecimal("0.14"), new BigDecimal("20"), new BigDecimal("0.05"), 12, 1, 36, new BigDecimal("10000"), new BigDecimal("10000000")),
                        loan(LoanType.LINE_OF_CREDIT, "line-of-credit", "Line of Credit", LoanInterestMethod.COMPOUND, LoanRateMode.VARIABLE, LoanRepaymentMethod.REVOLVING, new BigDecimal("0.18"), new BigDecimal("0"), new BigDecimal("0.02"), 9999, 1, 9999, new BigDecimal("500"), new BigDecimal("5000000"))
                ));
            }

            if (certificateRepo.count() == 0) {
                certificateRepo.saveAll(List.of(
                        certificate("fixed-term", "Fixed Term Certificate", CertificateInterestMethod.SIMPLE, PayoutMethod.AT_MATURITY, LiquidityMethod.LOCKED, RateBehaviorMethod.FIXED, new BigDecimal("0.05"), 365, 365, new BigDecimal("0"), new BigDecimal("1000"), new BigDecimal("10000000")),
                        certificate("interest-payout", "Interest Payout Certificate", CertificateInterestMethod.SIMPLE, PayoutMethod.PERIODIC, LiquidityMethod.LOCKED, RateBehaviorMethod.FIXED, new BigDecimal("0.055"), 90, 365, new BigDecimal("0"), new BigDecimal("1000"), new BigDecimal("10000000")),
                        certificate("cumulative", "Cumulative Certificate", CertificateInterestMethod.COMPOUND, PayoutMethod.AT_MATURITY, LiquidityMethod.LOCKED, RateBehaviorMethod.FIXED, new BigDecimal("0.06"), 365, 365, new BigDecimal("0"), new BigDecimal("1000"), new BigDecimal("10000000")),
                        certificate("short-term", "Short Term Certificate", CertificateInterestMethod.SIMPLE, PayoutMethod.AT_MATURITY, LiquidityMethod.LOCKED, RateBehaviorMethod.FIXED, new BigDecimal("0.045"), 30, 90, new BigDecimal("0"), new BigDecimal("500"), new BigDecimal("5000000")),
                        certificate("flexible", "Flexible Certificate", CertificateInterestMethod.SIMPLE, PayoutMethod.AT_MATURITY, LiquidityMethod.PENALIZED, RateBehaviorMethod.FIXED, new BigDecimal("0.05"), 90, 365, new BigDecimal("0.02"), new BigDecimal("1000"), new BigDecimal("10000000")),
                        certificate("step-up", "Step Up Certificate", CertificateInterestMethod.SIMPLE, PayoutMethod.AT_MATURITY, LiquidityMethod.LOCKED, RateBehaviorMethod.STEP_UP, new BigDecimal("0.04"), 365, 1095, new BigDecimal("0"), new BigDecimal("1000"), new BigDecimal("10000000")),
                        certificate("indexed", "Indexed Certificate", CertificateInterestMethod.VARIABLE, PayoutMethod.PERIODIC, LiquidityMethod.PENALIZED, RateBehaviorMethod.INDEXED, new BigDecimal("0.02"), 180, 730, new BigDecimal("0.02"), new BigDecimal("1000"), new BigDecimal("10000000")),
                        certificate("zero-coupon", "Zero Coupon Certificate", CertificateInterestMethod.DISCOUNT, PayoutMethod.AT_MATURITY, LiquidityMethod.LOCKED, RateBehaviorMethod.FIXED, new BigDecimal("0.07"), 365, 365, new BigDecimal("0"), new BigDecimal("1000"), new BigDecimal("10000000"))
                ));
            }
        };
    }

    private LoanProductDefinitionDocument loan(LoanType loanType, String code, String name, LoanInterestMethod interestMethod, LoanRateMode rateMode, LoanRepaymentMethod repaymentMethod, BigDecimal baseRate, BigDecimal fee, BigDecimal penaltyRate, int defaultTenure, int minTenure, int maxTenure, BigDecimal minPrincipal, BigDecimal maxPrincipal) {
        LoanProductDefinitionDocument document = new LoanProductDefinitionDocument();
        document.setId(UUID.randomUUID());
        document.setCode(code);
        document.setName(name);
        document.setLoanType(loanType);
        document.setInterestMethod(interestMethod);
        document.setRateMode(rateMode);
        document.setRepaymentMethod(repaymentMethod);
        document.setBaseAnnualRate(baseRate);
        document.setMonthlyFee(fee);
        document.setPenaltyRate(penaltyRate);
        document.setDefaultTenureMonths(defaultTenure);
        document.setMinimumTenureMonths(minTenure);
        document.setMaximumTenureMonths(maxTenure);
        document.setMinimumPrincipal(minPrincipal);
        document.setMaximumPrincipal(maxPrincipal);
        document.setActive(Boolean.TRUE);
        return document;
    }

    private CertificateProductDefinitionDocument certificate(String code, String name, CertificateInterestMethod interestMethod, PayoutMethod payoutMethod, LiquidityMethod liquidityMethod, RateBehaviorMethod rateBehaviorMethod, BigDecimal baseRate, Integer payoutIntervalDays, Integer termDays, BigDecimal penaltyRate, BigDecimal minPrincipal, BigDecimal maxPrincipal) {
        CertificateProductDefinitionDocument document = new CertificateProductDefinitionDocument();
        document.setId(UUID.randomUUID());
        document.setCode(code);
        document.setName(name);
        document.setInterestMethod(interestMethod);
        document.setPayoutMethod(payoutMethod);
        document.setLiquidityMethod(liquidityMethod);
        document.setRateBehaviorMethod(rateBehaviorMethod);
        document.setBaseAnnualRate(baseRate);
        document.setPayoutIntervalDays(payoutIntervalDays);
        document.setTermDays(termDays);
        document.setPenaltyRate(penaltyRate);
        document.setMinimumPrincipal(minPrincipal);
        document.setMaximumPrincipal(maxPrincipal);
        document.setActive(Boolean.TRUE);
        return document;
    }
}
