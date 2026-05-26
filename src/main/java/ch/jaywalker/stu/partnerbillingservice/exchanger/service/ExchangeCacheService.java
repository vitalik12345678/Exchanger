package ch.jaywalker.stu.partnerbillingservice.exchanger.service;

import ch.jaywalker.stu.partnerbillingservice.exchanger.client.ExchangeRateHostClient;
import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.ExternalApiException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.Currency;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.external.ApiLatestRatesResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.internal.RatesSnapshot;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeCacheService {

	private final ExchangeRateHostClient client;

	@Cacheable(value = "rates", key = "#baseCurrency.name()")
	public RatesSnapshot getRates(Currency baseCurrency) {
		ApiLatestRatesResponse response = client.fetchLatestRates(baseCurrency.name());
		if (response.quotes() == null || response.quotes().isEmpty()) {
			throw new ExternalApiException("External API returned no quotes for " + baseCurrency.name());
		}
		LocalDate date = Instant.ofEpochSecond(response.timestamp()).atZone(ZoneOffset.UTC).toLocalDate();
		Map<String, BigDecimal> rates = response.quotes().entrySet().stream().collect(
				Collectors.toMap(e -> e.getKey().substring(baseCurrency.name().length()), Map.Entry::getValue));
		return new RatesSnapshot(date, rates);
	}
}
