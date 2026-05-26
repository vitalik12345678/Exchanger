package ch.jaywalker.stu.partnerbillingservice.exchanger.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record RatesResponse(String base, Map<String, BigDecimal> rates, LocalDate date) {
}
