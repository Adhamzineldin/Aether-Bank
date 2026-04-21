package com.maayn.financialservice.mappers;

import com.maayn.financialservice.entity.CertificateApplicationDocument;
import maayn.veld.generated.models.certificate.CertificateApplication;
import maayn.veld.generated.models.certificate.CertificateApplicationResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CertificateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "certificateNumber", ignore = true)
    @Mapping(target = "applicationStatus", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "certificateStatus", ignore = true)
    @Mapping(target = "openDate", ignore = true)
    @Mapping(target = "maturityDate", ignore = true)
    @Mapping(target = "interestEarned", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CertificateApplicationDocument toEntity(CertificateApplication dto);

    @Mapping(target = "applicationId", source = "id")
    @Mapping(target = "status", source = "applicationStatus")
    @Mapping(target = "submittedAt", source = "submittedAt")
    CertificateApplicationResponse toResponse(CertificateApplicationDocument entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CertificateApplication dto, @MappingTarget CertificateApplicationDocument entity);
}
