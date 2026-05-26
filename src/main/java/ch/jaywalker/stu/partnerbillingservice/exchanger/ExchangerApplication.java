package ch.jaywalker.stu.partnerbillingservice.exchanger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ExchangerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExchangerApplication.class, args);
	}
}
