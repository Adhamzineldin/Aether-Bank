package com.maayn.financialservice.mapper;

import com.maayn.financialservice.entity.MortgageApplicationDocument;
import com.maayn.financialservice.model.MortgageApplicationRequest;
import com.maayn.financialservice.model.MortgageApplicationResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MortgageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mortgageNumber", ignore = true)
    @Mapping(target = "applicationStatus", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "remarks", ignore = true)
    @Mapping(target = "mortgageStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MortgageApplicationDocument toEntity(MortgageApplicationRequest dto);

    @Mapping(target = "applicationId", source = "id")
    @Mapping(target = "status", source = "applicationStatus")
    MortgageApplicationResponse toResponse(MortgageApplicationDocument entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(MortgageApplicationRequest dto, @MappingTarget MortgageApplicationDocument entity);
}
