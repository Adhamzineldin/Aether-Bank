package com.maayn.financialservice.mapper;

import com.maayn.financialservice.entity.LoanApplicationDocument;
import maayn.veld.generated.models.loan.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "applicationStatus", ignore = true)
    @Mapping(target = "loanNumber", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "principalAmount", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "interestType", ignore = true)
    @Mapping(target = "tenureMonths", ignore = true)
    @Mapping(target = "emiAmount", ignore = true)
    @Mapping(target = "outstandingBalance", ignore = true)
    @Mapping(target = "disbursedAmount", ignore = true)
    @Mapping(target = "repaymentFrequency", ignore = true)
    @Mapping(target = "loanStatus", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "disbursementDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LoanApplicationDocument toEntity(LoanApplication dto);

    LoanApplication toModel(LoanApplicationDocument entity);

    @Mapping(target = "applicationId", source = "id")
    @Mapping(target = "status", source = "applicationStatus")
    LoanApplicationResponse toResponse(LoanApplicationDocument entity);

    @Mapping(target = "applicationId", source = "applicationId")
    Loan toLoan(LoanApplicationDocument entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(LoanApplication dto, @MappingTarget LoanApplicationDocument entity);
}
