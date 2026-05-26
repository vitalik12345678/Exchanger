package ch.jaywalker.stu.partnerbillingservice.exchanger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "exchanger.api")
public class ApiProperties {
	private String baseUrl;
	private String key;
}
