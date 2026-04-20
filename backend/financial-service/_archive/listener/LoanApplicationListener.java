package com.maayn.financialservice.listener;

import com.maayn.financialservice.entity.LoanApplicationDocument;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoanApplicationListener implements BeforeConvertCallback<LoanApplicationDocument> {

    @Override
    public LoanApplicationDocument onBeforeConvert(LoanApplicationDocument entity, String collection) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        return entity;
    }
}
