package ch.jaywalker.stu.partnerbillingservice.exchanger.service;

import ch.jaywalker.stu.partnerbillingservice.exchanger.config.ConversionProperties;
import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.CurrencyNotFoundException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.Currency;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.internal.RatesSnapshot;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResult;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.MultiConversionResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.RateResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.RatesResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

	private final ExchangeCacheService cacheService;
	private final ConversionProperties conversionProperties;

	public RateResponse getRate(Currency originCurrency, Currency targetCurrency) {
		RatesSnapshot snapshot = cacheService.getRates(originCurrency);
		return new RateResponse(originCurrency.name(), targetCurrency.name(), extractRate(snapshot, targetCurrency),
				snapshot.date());
	}

	public RatesResponse getAllRates(Currency originCurrency) {
		RatesSnapshot snapshot = cacheService.getRates(originCurrency);
		return new RatesResponse(originCurrency.name(), snapshot.rates(), snapshot.date());
	}

	public ConversionResponse convert(Currency originCurrency, Currency targetCurrency, BigDecimal amount) {
		RatesSnapshot snapshot = cacheService.getRates(originCurrency);
		BigDecimal rate = extractRate(snapshot, targetCurrency);
		BigDecimal converted = amount.multiply(rate).setScale(conversionProperties.getScale(), RoundingMode.HALF_UP);
		return new ConversionResponse(originCurrency.name(), targetCurrency.name(), amount, converted, rate);
	}

	public MultiConversionResponse convertToMany(Currency originCurrency, BigDecimal amount, List<Currency> targets) {
		RatesSnapshot snapshot = cacheService.getRates(originCurrency);
		List<ConversionResult> results = targets.stream().map(target -> {
			BigDecimal rate = extractRate(snapshot, target);
			BigDecimal converted = amount.multiply(rate).setScale(conversionProperties.getScale(),
					RoundingMode.HALF_UP);
			return new ConversionResult(target.name(), converted, rate);
		}).toList();
		return new MultiConversionResponse(originCurrency.name(), amount, results);
	}

	private BigDecimal extractRate(RatesSnapshot snapshot, Currency target) {
		Map<String, BigDecimal> rates = snapshot.rates();
		if (rates == null || !rates.containsKey(target.name())) {
			throw new CurrencyNotFoundException(target.name());
		}
		return rates.get(target.name());
	}
}
