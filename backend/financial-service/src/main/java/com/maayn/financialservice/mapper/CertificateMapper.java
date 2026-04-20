package com.maayn.financialservice.mapper;

import com.maayn.financialservice.entity.CertificateApplicationDocument;
import com.maayn.financialservice.model.CertificateApplicationRequest;
import com.maayn.financialservice.model.CertificateApplicationResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CertificateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "certificateNumber", ignore = true)
    @Mapping(target = "applicationStatus", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "remarks", ignore = true)
    @Mapping(target = "certificateStatus", ignore = true)
    @Mapping(target = "openDate", ignore = true)
    @Mapping(target = "maturityDate", ignore = true)
    @Mapping(target = "interestEarned", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CertificateApplicationDocument toEntity(CertificateApplicationRequest dto);

    @Mapping(target = "applicationId", source = "id")
    @Mapping(target = "status", source = "applicationStatus")
    CertificateApplicationResponse toResponse(CertificateApplicationDocument entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CertificateApplicationRequest dto, @MappingTarget CertificateApplicationDocument entity);
}
