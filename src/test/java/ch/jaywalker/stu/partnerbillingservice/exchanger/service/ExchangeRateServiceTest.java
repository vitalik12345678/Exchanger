package ch.jaywalker.stu.partnerbillingservice.exchanger.service;

import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.CurrencyNotFoundException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.ExternalApiException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResult;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.MultiConversionResponse;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static ch.jaywalker.stu.partnerbillingservice.exchanger.TestDataProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeCacheService cacheService;

    @InjectMocks
    private ExchangeRateService service;

    @Test
    void givenValidCurrencies_whenGetRate_thenReturnsCorrectRate() {
        when(cacheService.getRates(USD)).thenReturn(ratesSnapshot());

        var result = service.getRate(USD, EUR);

        assertAll(
            () -> assertThat(result.originCurrency()).isEqualTo(USD),
            () -> assertThat(result.targetCurrency()).isEqualTo(EUR),
            () -> assertThat(result.rate()).isEqualByComparingTo(EUR_RATE),
            () -> assertThat(result.date()).isEqualTo(SNAPSHOT_DATE)
        );
    }

    @Test
    void givenExternalApiException_whenGetRate_thenExceptionPropagates() {
        when(cacheService.getRates(USD)).thenThrow(new ExternalApiException("provider down"));

        assertThatThrownBy(() -> service.getRate(USD, EUR))
                .isInstanceOf(ExternalApiException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"usd", "Usd", "USD"})
    void givenAnyCaseCurrencyCode_whenGetRate_thenNormalizesToUpperCase(String inputCurrency) {
        when(cacheService.getRates(USD)).thenReturn(ratesSnapshot());

        service.getRate(inputCurrency, EUR);

        verify(cacheService).getRates(USD);
    }

    @ParameterizedTest
    @MethodSource("conversionAmountProvider")
    void givenDifferentAmounts_whenConvert_thenReturnsCorrectConvertedValue(
            BigDecimal amount, BigDecimal expectedConverted) {
        when(cacheService.getRates(USD)).thenReturn(ratesSnapshot());

        ConversionResponse result = service.convert(USD, EUR, amount);

        assertThat(result.converted()).isEqualByComparingTo(expectedConverted);
    }

    @Test
    void givenMultipleTargets_whenConvertToMany_thenReturnsResultsForEachTarget() {
        when(cacheService.getRates(USD)).thenReturn(ratesSnapshot());

        MultiConversionResponse result = service.convertToMany(USD, AMOUNT_100, List.of(EUR, GBP));
        ConversionResult eurResult = result.results().getFirst();
        ConversionResult gbpResult = result.results().get(1);

        assertAll(
            () -> assertThat(result.originCurrency()).isEqualTo(USD),
            () -> assertThat(result.amount()).isEqualByComparingTo(AMOUNT_100),
            () -> assertThat(result.results()).hasSize(2),
            () -> assertThat(eurResult.targetCurrency()).isEqualTo(EUR),
            () -> assertThat(eurResult.converted()).isEqualByComparingTo(EUR_CONVERTED_100),
            () -> assertThat(gbpResult.targetCurrency()).isEqualTo(GBP),
            () -> assertThat(gbpResult.converted()).isEqualByComparingTo(GBP_CONVERTED_100)
        );
    }

    @ParameterizedTest
    @MethodSource("targetListProvider")
    void givenDifferentTargetLists_whenConvertToMany_thenReturnsMatchingResultCount(
            List<String> targets, int expectedCount) {
        when(cacheService.getRates(USD)).thenReturn(ratesSnapshot());

        MultiConversionResponse result = service.convertToMany(USD, AMOUNT_100, targets);

        assertThat(result.results()).hasSize(expectedCount);
    }

    @ParameterizedTest
    @MethodSource("unknownCurrencyCallsProvider")
    void givenUnknownCurrency_whenAnyServiceMethodCalled_thenThrowsCurrencyNotFoundException(
            ThrowingCallable call) {
        when(cacheService.getRates(USD)).thenReturn(ratesSnapshot());

        assertThatThrownBy(call)
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessageContaining(UNKNOWN_CURRENCY);
    }

    Stream<ThrowingCallable> unknownCurrencyCallsProvider() {
        return Stream.of(
                () -> service.getRate(USD, UNKNOWN_CURRENCY),
                () -> service.convert(USD, UNKNOWN_CURRENCY, AMOUNT_100),
                () -> service.convertToMany(USD, AMOUNT_100, List.of(EUR, UNKNOWN_CURRENCY))
        );
    }

    static Stream<Arguments> conversionAmountProvider() {
        return Stream.of(
                Arguments.of(AMOUNT_50,   EUR_CONVERTED_50),
                Arguments.of(AMOUNT_200,  EUR_CONVERTED_200),
                Arguments.of(AMOUNT_1000, EUR_CONVERTED_1000)
        );
    }

    static Stream<Arguments> targetListProvider() {
        return Stream.of(
                Arguments.of(List.of(EUR),           1),
                Arguments.of(List.of(EUR, GBP, JPY), 3)
        );
    }
}
