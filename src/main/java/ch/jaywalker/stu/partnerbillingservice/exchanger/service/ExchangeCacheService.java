package ch.jaywalker.stu.partnerbillingservice.exchanger.service;

import ch.jaywalker.stu.partnerbillingservice.exchanger.client.ExchangeRateHostClient;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.external.ApiLatestRatesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeCacheService {

    private final ExchangeRateHostClient client;

    @Cacheable(value = "rates", key = "#baseCurrency")
    public ApiLatestRatesResponse getRates(String baseCurrency) {
        return client.fetchLatestRates(baseCurrency);
    }
}
