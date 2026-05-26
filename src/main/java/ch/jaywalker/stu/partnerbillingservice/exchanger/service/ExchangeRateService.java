package ch.jaywalker.stu.partnerbillingservice.exchanger.service;

import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.CurrencyNotFoundException;
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

	private static final int CONVERSION_SCALE = 4;

	private final ExchangeCacheService cacheService;

	public RateResponse getRate(String originCurrency, String targetCurrency) {
		String origin = originCurrency.toUpperCase();
		String target = targetCurrency.toUpperCase();
		RatesSnapshot snapshot = cacheService.getRates(origin);
		return new RateResponse(origin, target, extractRate(snapshot, target), snapshot.date());
	}

	public RatesResponse getAllRates(String originCurrency) {
		String origin = originCurrency.toUpperCase();
		RatesSnapshot snapshot = cacheService.getRates(origin);
		return new RatesResponse(origin, snapshot.rates(), snapshot.date());
	}

	public ConversionResponse convert(String originCurrency, String targetCurrency, BigDecimal amount) {
		String origin = originCurrency.toUpperCase();
		String target = targetCurrency.toUpperCase();
		RatesSnapshot snapshot = cacheService.getRates(origin);
		BigDecimal rate = extractRate(snapshot, target);
		BigDecimal converted = amount.multiply(rate).setScale(CONVERSION_SCALE, RoundingMode.HALF_UP);
		return new ConversionResponse(origin, target, amount, converted, rate);
	}

	public MultiConversionResponse convertToMany(String originCurrency, BigDecimal amount, List<String> targets) {
		String origin = originCurrency.toUpperCase();
		RatesSnapshot snapshot = cacheService.getRates(origin);
		List<ConversionResult> results = targets.stream().map(String::toUpperCase).map(target -> {
			BigDecimal rate = extractRate(snapshot, target);
			BigDecimal converted = amount.multiply(rate).setScale(CONVERSION_SCALE, RoundingMode.HALF_UP);
			return new ConversionResult(target, converted, rate);
		}).toList();
		return new MultiConversionResponse(origin, amount, results);
	}

	private BigDecimal extractRate(RatesSnapshot snapshot, String target) {
		Map<String, BigDecimal> rates = snapshot.rates();
		if (rates == null || !rates.containsKey(target)) {
			throw new CurrencyNotFoundException(target);
		}
		return rates.get(target);
	}
}
