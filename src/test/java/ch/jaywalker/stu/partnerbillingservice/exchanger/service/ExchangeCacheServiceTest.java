package ch.jaywalker.stu.partnerbillingservice.exchanger.service;

import static ch.jaywalker.stu.partnerbillingservice.exchanger.TestDataProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.jaywalker.stu.partnerbillingservice.exchanger.client.ExchangeRateHostClient;
import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.ExternalApiException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.internal.RatesSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExchangeCacheServiceTest {

	@Mock
	private ExchangeRateHostClient client;

	@InjectMocks
	private ExchangeCacheService cacheService;

	@Test
    void givenValidBaseCurrency_whenGetRates_thenMapsToRatesSnapshot() {
        when(client.fetchLatestRates(USD)).thenReturn(usdSnapshot());

        RatesSnapshot result = cacheService.getRates(USD);

        assertThat(result).isEqualTo(ratesSnapshot());
        verify(client).fetchLatestRates(USD);
    }

	@Test
    void givenClientThrowsExternalApiException_whenGetRates_thenExceptionPropagates() {
        when(client.fetchLatestRates(USD)).thenThrow(new ExternalApiException("provider down"));

        assertThatThrownBy(() -> cacheService.getRates(USD)).isInstanceOf(ExternalApiException.class);
    }
}
