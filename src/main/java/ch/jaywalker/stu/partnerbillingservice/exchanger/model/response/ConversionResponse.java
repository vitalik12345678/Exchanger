package ch.jaywalker.stu.partnerbillingservice.exchanger.model.response;

import java.math.BigDecimal;

public record ConversionResponse(String originCurrency, String targetCurrency, BigDecimal amount, BigDecimal converted, BigDecimal rate) {}
