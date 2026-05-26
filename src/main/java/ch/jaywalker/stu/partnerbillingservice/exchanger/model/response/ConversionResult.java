package ch.jaywalker.stu.partnerbillingservice.exchanger.model.response;

import java.math.BigDecimal;

public record ConversionResult(String to, BigDecimal converted, BigDecimal rate) {}
