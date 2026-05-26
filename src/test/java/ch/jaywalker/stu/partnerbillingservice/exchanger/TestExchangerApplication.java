package ch.jaywalker.stu.partnerbillingservice.exchanger;

import org.springframework.boot.SpringApplication;

public class TestExchangerApplication {
    
    public static void main(String[] args) {
        SpringApplication.from(ExchangerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
    
}
