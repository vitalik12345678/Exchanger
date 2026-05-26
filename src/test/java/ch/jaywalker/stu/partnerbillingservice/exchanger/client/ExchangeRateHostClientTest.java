package ch.jaywalker.stu.partnerbillingservice.exchanger.client;

import static ch.jaywalker.stu.partnerbillingservice.exchanger.TestDataProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

import ch.jaywalker.stu.partnerbillingservice.exchanger.config.ApiProperties;
import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.ExternalApiException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.external.ApiError;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.external.ApiLatestRatesResponse;
import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

@ExtendWith(MockitoExtension.class)
class ExchangeRateHostClientTest {

	@Mock(answer = RETURNS_DEEP_STUBS)
	private RestClient exchangeRateRestClient;

	@Mock
	private ApiProperties apiProperties;

	@InjectMocks
	private ExchangeRateHostClient client;

	@Test
    void givenSuccessfulResponse_whenFetchLatestRates_thenReturnsResponse() {
        when(exchangeRateRestClient
                        .get()
                        .uri(ArgumentMatchers.<Function<UriBuilder, URI>>any())
                        .retrieve()
                        .body(ApiLatestRatesResponse.class))
                .thenReturn(usdSnapshot());

        ApiLatestRatesResponse result = client.fetchLatestRates(USD);

        assertThat(result).isEqualTo(usdSnapshot());
    }

	@Test
    void givenNullResponse_whenFetchLatestRates_thenThrowsExternalApiException() {
        when(exchangeRateRestClient
                        .get()
                        .uri(ArgumentMatchers.<Function<UriBuilder, URI>>any())
                        .retrieve()
                        .body(ApiLatestRatesResponse.class))
                .thenReturn(null);

        assertThatThrownBy(() -> client.fetchLatestRates(USD))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("unknown error");
    }

	@Test
	void givenFailureResponseWithErrorInfo_whenFetchLatestRates_thenThrowsExceptionWithDetail() {
		ApiLatestRatesResponse failResponse = new ApiLatestRatesResponse(false, USD, SNAPSHOT_TIMESTAMP, Map.of(),
				new ApiError(104, "base currency unavailable"));
		when(exchangeRateRestClient.get().uri(ArgumentMatchers.<Function<UriBuilder, URI>>any()).retrieve()
				.body(ApiLatestRatesResponse.class)).thenReturn(failResponse);

		assertThatThrownBy(() -> client.fetchLatestRates(USD)).isInstanceOf(ExternalApiException.class)
				.hasMessageContaining("base currency unavailable");
	}

	@Test
	void givenFailureResponseWithoutErrorInfo_whenFetchLatestRates_thenThrowsExceptionWithUnknownError() {
		ApiLatestRatesResponse failResponse = new ApiLatestRatesResponse(false, USD, SNAPSHOT_TIMESTAMP, Map.of(),
				null);
		when(exchangeRateRestClient.get().uri(ArgumentMatchers.<Function<UriBuilder, URI>>any()).retrieve()
				.body(ApiLatestRatesResponse.class)).thenReturn(failResponse);

		assertThatThrownBy(() -> client.fetchLatestRates(USD)).isInstanceOf(ExternalApiException.class)
				.hasMessageContaining("unknown error");
	}

	@Test
    void givenRestClientException_whenFetchLatestRates_thenThrowsExternalApiExceptionWithCause() {
        when(exchangeRateRestClient
                        .get()
                        .uri(ArgumentMatchers.<Function<UriBuilder, URI>>any())
                        .retrieve()
                        .body(ApiLatestRatesResponse.class))
                .thenThrow(new RestClientException("network error"));

        assertThatThrownBy(() -> client.fetchLatestRates(USD))
                .isInstanceOf(ExternalApiException.class)
                .hasCauseInstanceOf(RestClientException.class);
    }
}
