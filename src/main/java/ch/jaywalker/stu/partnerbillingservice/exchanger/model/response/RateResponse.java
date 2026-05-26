package ch.jaywalker.stu.partnerbillingservice.exchanger.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RateResponse(String from, String to, BigDecimal rate, LocalDate date) {}
