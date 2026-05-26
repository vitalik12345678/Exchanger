package ch.jaywalker.stu.partnerbillingservice.exchanger.exception;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final String ERROR_BASE_URI = "https://exchanger.example.com/errors/";

	@ExceptionHandler(CurrencyNotFoundException.class)
	public ProblemDetail handleCurrencyNotFound(CurrencyNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("Currency Not Found");
		problem.setType(URI.create(ERROR_BASE_URI + "currency-not-found"));
		return problem;
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		String value = ex.getValue() != null ? String.valueOf(ex.getValue()).toUpperCase() : "unknown";
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Currency not found: " + value);
		problem.setTitle("Currency Not Found");
		problem.setType(URI.create(ERROR_BASE_URI + "currency-not-found"));
		return problem;
	}

	@ExceptionHandler(ExternalApiException.class)
	public ProblemDetail handleExternalApiException(ExternalApiException ex) {
		log.error("External API failure: {}", ex.getMessage(), ex);
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
				"The exchange rate provider is currently unavailable. Please try again later.");
		problem.setTitle("External API Unavailable");
		problem.setType(URI.create(ERROR_BASE_URI + "external-api-error"));
		return problem;
	}
}
