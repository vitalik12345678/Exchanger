package ch.jaywalker.stu.partnerbillingservice.exchanger.model.external;

import java.math.BigDecimal;
import java.util.Map;

public record ApiLatestRatesResponse(boolean success, String source, long timestamp, Map<String, BigDecimal> quotes,
		ApiError error) {
}
