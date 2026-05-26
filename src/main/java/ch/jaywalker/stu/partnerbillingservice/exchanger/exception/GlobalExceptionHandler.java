package ch.jaywalker.stu.partnerbillingservice.exchanger.exception;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(CurrencyNotFoundException.class)
	public ProblemDetail handleCurrencyNotFound(CurrencyNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("Currency Not Found");
		problem.setType(URI.create("https://exchanger.example.com/errors/currency-not-found"));
		return problem;
	}

	@ExceptionHandler(ExternalApiException.class)
	public ProblemDetail handleExternalApiException(ExternalApiException ex) {
		log.error("External API failure: {}", ex.getMessage(), ex);
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
				"The exchange rate provider is currently unavailable. Please try again later.");
		problem.setTitle("External API Unavailable");
		problem.setType(URI.create("https://exchanger.example.com/errors/external-api-error"));
		return problem;
	}
}
