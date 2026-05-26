package ch.jaywalker.stu.partnerbillingservice.exchanger.service;

import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.CurrencyNotFoundException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.external.ApiLatestRatesResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResult;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.MultiConversionResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.RateResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.RatesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private static final int CONVERSION_SCALE = 4;

    private final ExchangeCacheService cacheService;

    public RateResponse getRate(String from, String to) {
        ApiLatestRatesResponse snapshot = cacheService.getRates(from.toUpperCase());
        BigDecimal rate = extractRate(snapshot, to.toUpperCase());
        return new RateResponse(snapshot.base(), to.toUpperCase(), rate, snapshot.date());
    }

    public RatesResponse getAllRates(String from) {
        ApiLatestRatesResponse snapshot = cacheService.getRates(from.toUpperCase());
        return new RatesResponse(snapshot.base(), snapshot.rates(), snapshot.date());
    }

    public ConversionResponse convert(String from, String to, BigDecimal amount) {
        ApiLatestRatesResponse snapshot = cacheService.getRates(from.toUpperCase());
        BigDecimal rate = extractRate(snapshot, to.toUpperCase());
        BigDecimal converted = amount.multiply(rate).setScale(CONVERSION_SCALE, RoundingMode.HALF_UP);
        return new ConversionResponse(snapshot.base(), to.toUpperCase(), amount, converted, rate);
    }

    public MultiConversionResponse convertToMany(String from, BigDecimal amount, List<String> targets) {
        ApiLatestRatesResponse snapshot = cacheService.getRates(from.toUpperCase());

        List<ConversionResult> results = targets.stream()
                .map(String::toUpperCase)
                .map(target -> {
                    BigDecimal rate = extractRate(snapshot, target);
                    BigDecimal converted = amount.multiply(rate).setScale(CONVERSION_SCALE, RoundingMode.HALF_UP);
                    return new ConversionResult(target, converted, rate);
                })
                .toList();

        return new MultiConversionResponse(snapshot.base(), amount, results);
    }

    private BigDecimal extractRate(ApiLatestRatesResponse snapshot, String target) {
        Map<String, BigDecimal> rates = snapshot.rates();
        if (rates == null || !rates.containsKey(target)) {
            throw new CurrencyNotFoundException(target);
        }
        return rates.get(target);
    }
}
