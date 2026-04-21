package com.maayn.financialservice.controller;

import com.maayn.financialservice.service.FxRateService;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.models.fx.GetRatesResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Query-param flavour of the Veld-generated {@code FXController}. The
 * generated one requires a {@code @RequestBody} on a {@code GET} which the
 * browser {@code fetch}/axios cannot send reliably, so we expose the same
 * logic as a plain {@code GET ?from=USD&to=EUR}.
 */
@RestController
@RequestMapping("/api/financial_service/fx")
@RequiredArgsConstructor
public class FxQueryController {

    private final FxRateService fxRateService;

    @GetMapping("/rate")
    public ResponseEntity<GetRatesResponse> getRate(
            @RequestParam("from") String sourceCurrency,
            @RequestParam("to") String destinationCurrency
    ) {
        GetRatesResponse response = new GetRatesResponse();
        response.setSourceCurrency(sourceCurrency);
        response.setDestinationCurrency(destinationCurrency);
        response.setExchangeRate(fxRateService.getRate(sourceCurrency, destinationCurrency));
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
