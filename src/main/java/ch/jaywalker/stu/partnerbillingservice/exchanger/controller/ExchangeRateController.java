package ch.jaywalker.stu.partnerbillingservice.exchanger.controller;

import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.MultiConversionResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.RateResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.RatesResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Exchange Rates", description = "Currency exchange rate lookups and conversions")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping("/rates/{from}/{to}")
    @Operation(
            summary = "Get exchange rate from currency A to currency B",
            description = "Returns the current exchange rate between two currencies. Rates are cached for 60 seconds.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Exchange rate retrieved",
                            content = @Content(schema = @Schema(implementation = RateResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Currency code not found", content = @Content),
                    @ApiResponse(responseCode = "502", description = "External rate provider unavailable", content = @Content)
            }
    )
    public ResponseEntity<RateResponse> getRate(
            @Parameter(description = "Source currency code (e.g. USD)", example = "USD")
            @PathVariable String from,
            @Parameter(description = "Target currency code (e.g. EUR)", example = "EUR")
            @PathVariable String to) {
        return ResponseEntity.ok(exchangeRateService.getRate(from, to));
    }

    @GetMapping("/rates/{from}")
    @Operation(
            summary = "Get all exchange rates from currency A",
            description = "Returns all available exchange rates relative to the given base currency. Rates are cached for 60 seconds.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "All rates retrieved",
                            content = @Content(schema = @Schema(implementation = RatesResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Base currency code not found", content = @Content),
                    @ApiResponse(responseCode = "502", description = "External rate provider unavailable", content = @Content)
            }
    )
    public ResponseEntity<RatesResponse> getAllRates(
            @Parameter(description = "Base currency code (e.g. USD)", example = "USD")
            @PathVariable String from) {
        return ResponseEntity.ok(exchangeRateService.getAllRates(from));
    }

    @GetMapping("/convert/{from}/{to}")
    @Operation(
            summary = "Convert an amount from currency A to currency B",
            description = "Converts the given amount using the current exchange rate. Rates are cached for 60 seconds.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Conversion result",
                            content = @Content(schema = @Schema(implementation = ConversionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid amount", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Currency code not found", content = @Content),
                    @ApiResponse(responseCode = "502", description = "External rate provider unavailable", content = @Content)
            }
    )
    public ResponseEntity<ConversionResponse> convert(
            @Parameter(description = "Source currency code (e.g. USD)", example = "USD")
            @PathVariable String from,
            @Parameter(description = "Target currency code (e.g. EUR)", example = "EUR")
            @PathVariable String to,
            @Parameter(description = "Amount to convert", example = "100.0")
            @RequestParam @Positive BigDecimal amount) {
        return ResponseEntity.ok(exchangeRateService.convert(from, to, amount));
    }

    @GetMapping("/convert/{from}")
    @Operation(
            summary = "Convert an amount from currency A to multiple target currencies",
            description = "Converts the given amount to each of the supplied target currencies in a single call. Rates are cached for 60 seconds.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Multi-currency conversion result",
                            content = @Content(schema = @Schema(implementation = MultiConversionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid amount or empty targets", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Currency code not found", content = @Content),
                    @ApiResponse(responseCode = "502", description = "External rate provider unavailable", content = @Content)
            }
    )
    public ResponseEntity<MultiConversionResponse> convertToMany(
            @Parameter(description = "Source currency code (e.g. USD)", example = "USD")
            @PathVariable String from,
            @Parameter(description = "Amount to convert", example = "100.0")
            @RequestParam @Positive BigDecimal amount,
            @Parameter(description = "Comma-separated list of target currency codes", example = "EUR,GBP,JPY")
            @RequestParam List<String> targets) {
        return ResponseEntity.ok(exchangeRateService.convertToMany(from, amount, targets));
    }
}
