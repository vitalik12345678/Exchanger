package ch.jaywalker.stu.partnerbillingservice.exchanger.service;

import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.CurrencyNotFoundException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.ExternalApiException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.external.ApiLatestRatesResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResult;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.MultiConversionResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.RateResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.RatesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeCacheService cacheService;

    @InjectMocks
    private ExchangeRateService service;

    private ApiLatestRatesResponse usdSnapshot;

    @BeforeEach
    void setUp() {
        usdSnapshot = new ApiLatestRatesResponse(
                true,
                "USD",
                LocalDate.of(2024, 5, 26),
                Map.of(
                        "EUR", new BigDecimal("0.9234"),
                        "GBP", new BigDecimal("0.7801"),
                        "JPY", new BigDecimal("156.78")
                ),
                null
        );
    }

    @Test
    void getRate_returnsCorrectRate() {
        when(cacheService.getRates("USD")).thenReturn(usdSnapshot);

        RateResponse result = service.getRate("USD", "EUR");

        assertThat(result.from()).isEqualTo("USD");
        assertThat(result.to()).isEqualTo("EUR");
        assertThat(result.rate()).isEqualByComparingTo("0.9234");
    }

    @Test
    void getRate_unknownTargetCurrency_throwsCurrencyNotFoundException() {
        when(cacheService.getRates("USD")).thenReturn(usdSnapshot);

        assertThatThrownBy(() -> service.getRate("USD", "XYZ"))
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessageContaining("XYZ");
    }

    @Test
    void getAllRates_returnsAllRates() {
        when(cacheService.getRates("USD")).thenReturn(usdSnapshot);

        RatesResponse result = service.getAllRates("USD");

        assertThat(result.base()).isEqualTo("USD");
        assertThat(result.rates()).containsKeys("EUR", "GBP", "JPY");
    }

    @Test
    void convert_calculatesCorrectly() {
        when(cacheService.getRates("USD")).thenReturn(usdSnapshot);

        ConversionResponse result = service.convert("USD", "EUR", new BigDecimal("100"));

        assertThat(result.from()).isEqualTo("USD");
        assertThat(result.to()).isEqualTo("EUR");
        assertThat(result.amount()).isEqualByComparingTo("100");
        assertThat(result.rate()).isEqualByComparingTo("0.9234");
        assertThat(result.converted()).isEqualByComparingTo("92.3400");
    }

    @Test
    void convertToMany_returnsResultsForAllTargets() {
        when(cacheService.getRates("USD")).thenReturn(usdSnapshot);

        MultiConversionResponse result = service.convertToMany("USD", new BigDecimal("100"), List.of("EUR", "GBP"));

        assertThat(result.from()).isEqualTo("USD");
        assertThat(result.amount()).isEqualByComparingTo("100");
        assertThat(result.results()).hasSize(2);

        ConversionResult eur = result.results().get(0);
        assertThat(eur.to()).isEqualTo("EUR");
        assertThat(eur.converted()).isEqualByComparingTo("92.3400");

        ConversionResult gbp = result.results().get(1);
        assertThat(gbp.to()).isEqualTo("GBP");
        assertThat(gbp.converted()).isEqualByComparingTo("78.0100");
    }

    @Test
    void convertToMany_unknownTarget_throwsCurrencyNotFoundException() {
        when(cacheService.getRates("USD")).thenReturn(usdSnapshot);

        assertThatThrownBy(() -> service.convertToMany("USD", new BigDecimal("100"), List.of("EUR", "UNKNOWN")))
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    void fetchRates_propagatesExternalApiException() {
        when(cacheService.getRates("USD"))
                .thenThrow(new ExternalApiException("provider down"));

        assertThatThrownBy(() -> service.getRate("USD", "EUR"))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("provider down");
    }

    @Test
    void getRate_delegatesToCacheServiceWithUpperCaseBase() {
        when(cacheService.getRates("USD")).thenReturn(usdSnapshot);

        service.getRate("usd", "eur");

        verify(cacheService).getRates("USD");
    }
}
