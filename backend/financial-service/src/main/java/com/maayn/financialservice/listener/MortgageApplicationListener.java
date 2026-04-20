package com.maayn.financialservice.listener;

import com.maayn.financialservice.entity.MortgageApplicationDocument;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MortgageApplicationListener implements BeforeConvertCallback<MortgageApplicationDocument> {

    @Override
    public MortgageApplicationDocument onBeforeConvert(MortgageApplicationDocument entity, String collection) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        return entity;
    }
}
