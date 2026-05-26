package ch.jaywalker.stu.partnerbillingservice.exchanger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI exchangerOpenAPI() {
		return new OpenAPI().info(new Info().title("Exchange Rate API")
				.description("Currency exchange rate lookups and conversions powered by exchangerate.host. "
						+ "Rates are cached for up to 60 seconds to minimise external API calls.")
				.version("1.0.0").contact(new Contact().name("Vitalii").email("bretsko.vitalii@gmail.com")));
	}
}
