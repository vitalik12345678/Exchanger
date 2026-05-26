package ch.jaywalker.stu.partnerbillingservice.exchanger;

import ch.jaywalker.stu.partnerbillingservice.exchanger.model.Currency;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.external.ApiLatestRatesResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.internal.RatesSnapshot;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResult;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.MultiConversionResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.RateResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.RatesResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class TestDataProvider {

	public static final String USD = "USD";
	public static final String EUR = "EUR";
	public static final String GBP = "GBP";
	public static final String JPY = "JPY";
	public static final String UNKNOWN_CURRENCY = "XYZ";
	public static final Currency ABSENT_CURRENCY = Currency.CHF;

	public static final BigDecimal EUR_RATE = new BigDecimal("0.9234");
	public static final BigDecimal GBP_RATE = new BigDecimal("0.7801");
	public static final BigDecimal JPY_RATE = new BigDecimal("156.78");

	public static final BigDecimal AMOUNT_50 = new BigDecimal("50");
	public static final BigDecimal AMOUNT_100 = new BigDecimal("100");
	public static final BigDecimal AMOUNT_200 = new BigDecimal("200");
	public static final BigDecimal AMOUNT_1000 = new BigDecimal("1000");

	public static final BigDecimal EUR_CONVERTED_50 = new BigDecimal("46.17");
	public static final BigDecimal EUR_CONVERTED_100 = new BigDecimal("92.34");
	public static final BigDecimal EUR_CONVERTED_200 = new BigDecimal("184.68");
	public static final BigDecimal EUR_CONVERTED_1000 = new BigDecimal("923.4");

	public static final BigDecimal GBP_CONVERTED_100 = new BigDecimal("78.01");
	public static final BigDecimal GBP_CONVERTED_200 = new BigDecimal("156.02");

	public static final LocalDate SNAPSHOT_DATE = LocalDate.of(2024, 5, 26);
	public static final long SNAPSHOT_TIMESTAMP = 1716681600L;

	public static final String ERROR_TITLE_CURRENCY_NOT_FOUND = "Currency Not Found";
	public static final String ERROR_TITLE_EXTERNAL_API_UNAVAILABLE = "External API Unavailable";

	public static final String JSON_ORIGIN_CURRENCY = "$.originCurrency";
	public static final String JSON_TARGET_CURRENCY = "$.targetCurrency";
	public static final String JSON_RATE = "$.rate";
	public static final String JSON_BASE = "$.base";
	public static final String JSON_AMOUNT = "$.amount";
	public static final String JSON_CONVERTED = "$.converted";
	public static final String JSON_RESULTS = "$.results";
	public static final String JSON_RESULT_0_TARGET = "$.results[0].targetCurrency";
	public static final String JSON_RESULT_1_TARGET = "$.results[1].targetCurrency";
	public static final String JSON_ERROR_TITLE = "$.title";

	public static final String URL_RATES = "/api/v1/rates/{originCurrency}/{targetCurrency}";
	public static final String URL_RATES_ALL = "/api/v1/rates/{originCurrency}";
	public static final String URL_CONVERT = "/api/v1/convert/{originCurrency}/{targetCurrency}";
	public static final String URL_CONVERT_MULTI = "/api/v1/convert/{originCurrency}";

	public static final String PARAM_AMOUNT = "amount";
	public static final String PARAM_TARGETS = "targets";

	public static ApiLatestRatesResponse usdSnapshot() {
		return new ApiLatestRatesResponse(true, USD, SNAPSHOT_TIMESTAMP,
				Map.of(USD + EUR, EUR_RATE, USD + GBP, GBP_RATE, USD + JPY, JPY_RATE), null);
	}

	public static ApiLatestRatesResponse usdSnapshotNullQuotes() {
		return new ApiLatestRatesResponse(true, USD, SNAPSHOT_TIMESTAMP, null, null);
	}

	public static RatesSnapshot ratesSnapshot() {
		return new RatesSnapshot(SNAPSHOT_DATE, Map.of(EUR, EUR_RATE, GBP, GBP_RATE, JPY, JPY_RATE));
	}

	public static RatesSnapshot ratesSnapshotNullRates() {
		return new RatesSnapshot(SNAPSHOT_DATE, null);
	}

	public static RateResponse rateResponse() {
		return new RateResponse(USD, EUR, EUR_RATE, SNAPSHOT_DATE);
	}

	public static RateResponse rateResponse(String origin, String target) {
		return new RateResponse(origin, target, EUR_RATE, SNAPSHOT_DATE);
	}

	public static RateResponse rateResponse(Currency origin, Currency target) {
		return new RateResponse(origin.name(), target.name(), EUR_RATE, SNAPSHOT_DATE);
	}

	public static RatesResponse ratesResponse() {
		return new RatesResponse(USD, Map.of(EUR, EUR_RATE, GBP, GBP_RATE), SNAPSHOT_DATE);
	}

	public static ConversionResponse conversionResponse() {
		return new ConversionResponse(USD, EUR, AMOUNT_100, EUR_CONVERTED_100, EUR_RATE);
	}

	public static MultiConversionResponse multiConversionResponse() {
		return new MultiConversionResponse(USD, AMOUNT_100,
				List.of(new ConversionResult(EUR, EUR_CONVERTED_100, EUR_RATE),
						new ConversionResult(GBP, GBP_CONVERTED_100, GBP_RATE)));
	}

	private TestDataProvider() {
	}
}
