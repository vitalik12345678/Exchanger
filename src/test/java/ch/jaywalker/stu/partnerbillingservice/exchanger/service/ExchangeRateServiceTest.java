package ch.jaywalker.stu.partnerbillingservice.exchanger.service;

import static ch.jaywalker.stu.partnerbillingservice.exchanger.TestDataProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import ch.jaywalker.stu.partnerbillingservice.exchanger.config.ConversionProperties;
import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.CurrencyNotFoundException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.ExternalApiException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.Currency;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResult;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.MultiConversionResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

	@Mock
	private ExchangeCacheService cacheService;

	@Mock
	private ConversionProperties conversionProperties;

	@InjectMocks
	private ExchangeRateService service;

	@BeforeEach
	void setUp() {
		lenient().when(conversionProperties.getScale()).thenReturn(4);
	}

	@Test
    void givenValidCurrencies_whenGetRate_thenReturnsCorrectRate() {
        when(cacheService.getRates(Currency.USD)).thenReturn(ratesSnapshot());

        var result = service.getRate(Currency.USD, Currency.EUR);

        assertAll(
                () -> assertThat(result.originCurrency()).isEqualTo(USD),
                () -> assertThat(result.targetCurrency()).isEqualTo(EUR),
                () -> assertThat(result.rate()).isEqualByComparingTo(EUR_RATE),
                () -> assertThat(result.date()).isEqualTo(SNAPSHOT_DATE));
    }

	@Test
    void givenExternalApiException_whenGetRate_thenExceptionPropagates() {
        when(cacheService.getRates(Currency.USD)).thenThrow(new ExternalApiException("provider down"));

        assertThatThrownBy(() -> service.getRate(Currency.USD, Currency.EUR))
                .isInstanceOf(ExternalApiException.class);
    }

	@Test
    void givenValidCurrency_whenGetAllRates_thenReturnsAllRates() {
        when(cacheService.getRates(Currency.USD)).thenReturn(ratesSnapshot());

        var result = service.getAllRates(Currency.USD);

        assertAll(
                () -> assertThat(result.base()).isEqualTo(USD),
                () -> assertThat(result.rates()).containsKey(EUR),
                () -> assertThat(result.date()).isEqualTo(SNAPSHOT_DATE));
    }

	@ParameterizedTest
    @MethodSource("conversionAmountProvider")
    void givenDifferentAmounts_whenConvert_thenReturnsCorrectConvertedValue(
            BigDecimal amount, BigDecimal expectedConverted) {
        when(cacheService.getRates(Currency.USD)).thenReturn(ratesSnapshot());

        ConversionResponse result = service.convert(Currency.USD, Currency.EUR, amount);

        assertThat(result.converted()).isEqualByComparingTo(expectedConverted);
    }

	@Test
    void givenMultipleTargets_whenConvertToMany_thenReturnsResultsForEachTarget() {
        when(cacheService.getRates(Currency.USD)).thenReturn(ratesSnapshot());

        MultiConversionResponse result =
                service.convertToMany(Currency.USD, AMOUNT_100, List.of(Currency.EUR, Currency.GBP));
        ConversionResult eurResult = result.results().getFirst();
        ConversionResult gbpResult = result.results().get(1);

        assertAll(
                () -> assertThat(result.originCurrency()).isEqualTo(USD),
                () -> assertThat(result.amount()).isEqualByComparingTo(AMOUNT_100),
                () -> assertThat(result.results()).hasSize(2),
                () -> assertThat(eurResult.targetCurrency()).isEqualTo(EUR),
                () -> assertThat(eurResult.converted()).isEqualByComparingTo(EUR_CONVERTED_100),
                () -> assertThat(gbpResult.targetCurrency()).isEqualTo(GBP),
                () -> assertThat(gbpResult.converted()).isEqualByComparingTo(GBP_CONVERTED_100));
    }

	@ParameterizedTest
    @MethodSource("targetListProvider")
    void givenDifferentTargetLists_whenConvertToMany_thenReturnsMatchingResultCount(
            List<Currency> targets, int expectedCount) {
        when(cacheService.getRates(Currency.USD)).thenReturn(ratesSnapshot());

        MultiConversionResponse result = service.convertToMany(Currency.USD, AMOUNT_100, targets);

        assertThat(result.results()).hasSize(expectedCount);
    }

	@Test
    void givenNullRatesInSnapshot_whenGetRate_thenThrowsCurrencyNotFoundException() {
        when(cacheService.getRates(Currency.USD)).thenReturn(ratesSnapshotNullRates());

        assertThatThrownBy(() -> service.getRate(Currency.USD, Currency.EUR))
                .isInstanceOf(CurrencyNotFoundException.class);
    }

	@ParameterizedTest
    @ValueSource(strings = {"getRate", "convert", "convertToMany"})
    void givenCurrencyAbsentFromSnapshot_whenAnyServiceMethodCalled_thenThrowsCurrencyNotFoundException(
            String operation) {
        when(cacheService.getRates(Currency.USD)).thenReturn(ratesSnapshot());

        ThrowingCallable call =
                switch (operation) {
                    case "getRate" -> () -> service.getRate(Currency.USD, ABSENT_CURRENCY);
                    case "convert" -> () -> service.convert(Currency.USD, ABSENT_CURRENCY, AMOUNT_100);
                    default -> () -> service.convertToMany(
                            Currency.USD, AMOUNT_100, List.of(Currency.EUR, ABSENT_CURRENCY));
                };

        assertThatThrownBy(call)
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessageContaining(ABSENT_CURRENCY.name());
    }

	static Stream<Arguments> conversionAmountProvider() {
		return Stream.of(Arguments.of(AMOUNT_50, EUR_CONVERTED_50), Arguments.of(AMOUNT_200, EUR_CONVERTED_200),
				Arguments.of(AMOUNT_1000, EUR_CONVERTED_1000));
	}

	static Stream<Arguments> targetListProvider() {
		return Stream.of(Arguments.of(List.of(Currency.EUR), 1),
				Arguments.of(List.of(Currency.EUR, Currency.GBP, Currency.JPY), 3));
	}
}
