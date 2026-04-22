package com.maayn.financialservice.mappers;

import com.maayn.financialservice.entity.LoanApplicationDocument;
import maayn.veld.generated.models.loan.LoanApplication;
import maayn.veld.generated.models.loan.LoanApplicationResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "applicationStatus", ignore = true)
    @Mapping(target = "loanNumber", ignore = true)
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
    @Mapping(target = "submittedAt", source = "submittedAt")
    LoanApplicationResponse toResponse(LoanApplicationDocument entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(LoanApplication dto, @MappingTarget LoanApplicationDocument entity);
}
