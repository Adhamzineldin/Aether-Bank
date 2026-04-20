package com.maayn.financialservice.service;

import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.FXErrors;
import maayn.veld.generated.models.fx.GetRatesRequest;
import maayn.veld.generated.models.fx.GetRatesResponse;
import maayn.veld.generated.services.IFXService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FXService implements IFXService {

    private final FxRateService fxRateService;

    @Override
    public GetRatesResponse getExchangeRate(GetRatesRequest input) throws Exception {
        if (input == null) {
            throw FXErrors.getExchangeRate.invalidCurrencyPair("Exchange rate request is required.");
        }

        try {
            GetRatesResponse response = new GetRatesResponse();
            response.setSourceCurrency(input.getSourceCurrency());
            response.setDestinationCurrency(input.getDestinationCurrency());
            response.setExchangeRate(
                    fxRateService.getRate(input.getSourceCurrency(), input.getDestinationCurrency())
            );
            response.setTimestamp(LocalDateTime.now());
            return response;
        } catch (IllegalArgumentException ex) {
            throw FXErrors.getExchangeRate.invalidCurrencyPair(ex.getMessage());
        }
    }
}
