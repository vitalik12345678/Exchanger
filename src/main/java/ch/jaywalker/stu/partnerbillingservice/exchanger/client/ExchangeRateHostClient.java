package ch.jaywalker.stu.partnerbillingservice.exchanger.client;

import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.ExternalApiException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.external.ApiLatestRatesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateHostClient {
    
    private final RestClient exchangeRateRestClient;

    public ApiLatestRatesResponse fetchLatestRates(String baseCurrency) {
        try {
            ApiLatestRatesResponse response = exchangeRateRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/latest")
                            .queryParam("base", baseCurrency.toUpperCase())
                            .build())
                    .retrieve()
                    .body(ApiLatestRatesResponse.class);

            if (response == null || !response.success()) {
                String detail = (response != null && response.error() != null)
                        ? response.error().info()
                        : "unknown error";
                throw new ExternalApiException("External API returned failure: " + detail);
            }

            return response;
        } catch (RestClientException ex) {
            throw new ExternalApiException("Failed to reach exchange rate provider", ex);
        }
    }
}
