package com.maayn.financialservice.mappers;

import com.maayn.financialservice.entity.MortgageApplicationDocument;
import maayn.veld.generated.models.mortgage.Mortgage;
import maayn.veld.generated.models.mortgage.MortgageApplication;
import maayn.veld.generated.models.mortgage.MortgageApplicationResponse;
import org.springframework.stereotype.Component;

@Component
public class MortgageMapper {

    public MortgageApplicationDocument toEntity(MortgageApplication application) {
        return MortgageApplicationDocument.builder()
                .accountId(application.getAccountId())
                .customerId(application.getCustomerId())
                .propertyAddress(application.getPropertyAddress())
                .propertyValue(application.getPropertyValue())
                .downPayment(application.getDownPayment())
                .requestedAmount(application.getRequestedAmount())
                .termYears(application.getTermYears())
                .employmentStatus(application.getEmploymentStatus())
                .annualIncome(application.getAnnualIncome())
                .creditScore(application.getCreditScore())
                .build();
    }

    public MortgageApplicationResponse toResponse(MortgageApplicationDocument document) {
        MortgageApplicationResponse response = new MortgageApplicationResponse();
        response.setApplicationId(document.getId());
        response.setStatus(document.getApplicationStatus());
        response.setSubmittedAt(document.getSubmittedAt());
        return response;
    }

    public Mortgage toModel(MortgageApplicationDocument document) {
        Mortgage mortgage = new Mortgage();
        mortgage.setId(document.getId());
        mortgage.setMortgageNumber(document.getMortgageNumber());
        mortgage.setApplicationId(document.getId());
        mortgage.setCustomerId(document.getCustomerId());
        mortgage.setAccountId(document.getAccountId());
        mortgage.setPropertyAddress(document.getPropertyAddress());
        mortgage.setPropertyValue(document.getPropertyValue());
        mortgage.setPrincipalAmount(document.getPrincipalAmount());
        mortgage.setInterestRate(document.getInterestRate());
        mortgage.setTermMonths(document.getTermMonths());
        mortgage.setMonthlyPayment(document.getMonthlyPayment());
        mortgage.setOutstandingBalance(document.getOutstandingBalance());
        mortgage.setStatus(parseMortgageStatus(document.getMortgageStatus()));
        mortgage.setStartDate(document.getStartDate());
        mortgage.setEndDate(document.getEndDate());
        mortgage.setDisbursementDate(document.getDisbursementDate());
        mortgage.setCreatedAt(document.getCreatedAt());
        mortgage.setUpdatedAt(document.getUpdatedAt());
        return mortgage;
    }

    private maayn.veld.generated.models.mortgage.MortgageStatus parseMortgageStatus(String status) {
        try {
            return maayn.veld.generated.models.mortgage.MortgageStatus.valueOf(status);
        } catch (Exception e) {
            return maayn.veld.generated.models.mortgage.MortgageStatus.PENDING;
        }
    }
}

