package com.maayn.financialservice.mappers;

import com.maayn.financialservice.entity.Loan;
import maayn.veld.generated.models.loan.LoanApplication;
import maayn.veld.generated.models.loan.LoanApplicationResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "applicationStatus", ignore = true)
    Loan toEntity(LoanApplication dto);

    LoanApplication toModel(Loan entity);

    @Mapping(target = "applicationId", source = "id") // now String → String
    @Mapping(target = "status", source = "applicationStatus")
    @Mapping(target = "submittedAt", source = "submittedAt")
    LoanApplicationResponse toResponse(Loan entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(LoanApplication dto, @MappingTarget Loan entity);
}