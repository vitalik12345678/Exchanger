package ch.jaywalker.stu.partnerbillingservice.exchanger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "exchanger.conversion")
public class ConversionProperties {
	private int scale;
}
