package ch.jaywalker.stu.partnerbillingservice.exchanger.model.response;

import java.math.BigDecimal;

public record ConversionResult(String targetCurrency, BigDecimal converted, BigDecimal rate) {
}
