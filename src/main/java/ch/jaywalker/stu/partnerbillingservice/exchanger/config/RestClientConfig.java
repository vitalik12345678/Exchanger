package ch.jaywalker.stu.partnerbillingservice.exchanger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

	@Bean
	public RestClient exchangeRateRestClient(ApiProperties props) {
		return RestClient.builder().baseUrl(props.getBaseUrl()).build();
	}
}
