package ch.jaywalker.stu.partnerbillingservice.exchanger.service;

import ch.jaywalker.stu.partnerbillingservice.exchanger.client.ExchangeRateHostClient;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.external.ApiLatestRatesResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.internal.RatesSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeCacheService {

    private final ExchangeRateHostClient client;

    @Cacheable(value = "rates", key = "#baseCurrency")
    public RatesSnapshot getRates(String baseCurrency) {
        ApiLatestRatesResponse response = client.fetchLatestRates(baseCurrency);
        return new RatesSnapshot(response.date(), response.rates());
    }
}
