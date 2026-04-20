package com.maayn.financialservice.mappers;

import com.maayn.financialservice.entity.MortgageApplicationDocument;
import maayn.veld.generated.models.mortgage.MortgageApplication;
import maayn.veld.generated.models.mortgage.MortgageApplicationResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MortgageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mortgageNumber", ignore = true)
    @Mapping(target = "applicationStatus", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "mortgageStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MortgageApplicationDocument toEntity(MortgageApplication dto);

    @Mapping(target = "applicationId", source = "id")
    @Mapping(target = "status", source = "applicationStatus")
    @Mapping(target = "submittedAt", source = "submittedAt")
    MortgageApplicationResponse toResponse(MortgageApplicationDocument entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(MortgageApplication dto, @MappingTarget MortgageApplicationDocument entity);
}
