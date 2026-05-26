package ch.jaywalker.stu.partnerbillingservice.exchanger.model.internal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record RatesSnapshot(LocalDate date, Map<String, BigDecimal> rates) {
}
