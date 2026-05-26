package ch.jaywalker.stu.partnerbillingservice.exchanger.exception;

public class CurrencyNotFoundException extends RuntimeException {

	public CurrencyNotFoundException(String currency) {
		super("Currency not found: " + currency);
	}
}
