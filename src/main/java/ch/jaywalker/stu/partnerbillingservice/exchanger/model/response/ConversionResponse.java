package ch.jaywalker.stu.partnerbillingservice.exchanger.model.response;

import java.math.BigDecimal;

public record ConversionResponse(String from, String to, BigDecimal amount, BigDecimal converted, BigDecimal rate) {}
