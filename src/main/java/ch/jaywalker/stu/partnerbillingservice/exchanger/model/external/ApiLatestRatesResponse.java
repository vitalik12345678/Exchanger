package ch.jaywalker.stu.partnerbillingservice.exchanger.model.external;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record ApiLatestRatesResponse(
        boolean success,
        String base,
        LocalDate date,
        Map<String, BigDecimal> rates,
        ApiError error
) {}
