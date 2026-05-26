package ch.jaywalker.stu.partnerbillingservice.exchanger.model.response;

import java.math.BigDecimal;
import java.util.List;

public record MultiConversionResponse(String originCurrency, BigDecimal amount, List<ConversionResult> results) {}
