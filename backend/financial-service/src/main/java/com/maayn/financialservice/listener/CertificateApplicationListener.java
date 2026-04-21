package com.maayn.financialservice.listener;

import com.maayn.financialservice.entity.CertificateApplicationDocument;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CertificateApplicationListener implements BeforeConvertCallback<CertificateApplicationDocument> {

    @Override
    public CertificateApplicationDocument onBeforeConvert(CertificateApplicationDocument entity, String collection) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        return entity;
    }
}
