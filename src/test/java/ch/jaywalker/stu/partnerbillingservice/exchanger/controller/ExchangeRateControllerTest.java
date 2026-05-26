package ch.jaywalker.stu.partnerbillingservice.exchanger.controller;

import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.CurrencyNotFoundException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.ExternalApiException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.GlobalExceptionHandler;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.ConversionResult;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.MultiConversionResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.RateResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.model.response.RatesResponse;
import ch.jaywalker.stu.partnerbillingservice.exchanger.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ExchangeRateControllerTest {

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private ExchangeRateController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getRate_returns200WithRate() throws Exception {
        when(exchangeRateService.getRate("USD", "EUR"))
                .thenReturn(new RateResponse("USD", "EUR", new BigDecimal("0.9234"), LocalDate.of(2024, 5, 26)));

        mockMvc.perform(get("/api/v1/rates/USD/EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("USD"))
                .andExpect(jsonPath("$.to").value("EUR"))
                .andExpect(jsonPath("$.rate").value(0.9234));
    }

    @Test
    void getRate_unknownCurrency_returns404() throws Exception {
        when(exchangeRateService.getRate("USD", "XYZ"))
                .thenThrow(new CurrencyNotFoundException("XYZ"));

        mockMvc.perform(get("/api/v1/rates/USD/XYZ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Currency Not Found"));
    }

    @Test
    void getRate_externalApiDown_returns502() throws Exception {
        when(exchangeRateService.getRate("USD", "EUR"))
                .thenThrow(new ExternalApiException("provider down"));

        mockMvc.perform(get("/api/v1/rates/USD/EUR"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("External API Unavailable"));
    }

    @Test
    void getAllRates_returns200WithRatesMap() throws Exception {
        when(exchangeRateService.getAllRates("USD"))
                .thenReturn(new RatesResponse("USD",
                        Map.of("EUR", new BigDecimal("0.9234"), "GBP", new BigDecimal("0.7801")),
                        LocalDate.of(2024, 5, 26)));

        mockMvc.perform(get("/api/v1/rates/USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base").value("USD"))
                .andExpect(jsonPath("$.rates.EUR").value(0.9234))
                .andExpect(jsonPath("$.rates.GBP").value(0.7801));
    }

    @Test
    void convert_returns200WithConversion() throws Exception {
        when(exchangeRateService.convert("USD", "EUR", new BigDecimal("100.0")))
                .thenReturn(new ConversionResponse("USD", "EUR",
                        new BigDecimal("100.0"), new BigDecimal("92.3400"), new BigDecimal("0.9234")));

        mockMvc.perform(get("/api/v1/convert/USD/EUR").param("amount", "100.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("USD"))
                .andExpect(jsonPath("$.to").value("EUR"))
                .andExpect(jsonPath("$.converted").value(92.34));
    }

    @Test
    void convert_missingAmount_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/convert/USD/EUR"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void convertToMany_returns200WithAllResults() throws Exception {
        when(exchangeRateService.convertToMany("USD", new BigDecimal("100.0"), List.of("EUR", "GBP")))
                .thenReturn(new MultiConversionResponse("USD", new BigDecimal("100.0"), List.of(
                        new ConversionResult("EUR", new BigDecimal("92.3400"), new BigDecimal("0.9234")),
                        new ConversionResult("GBP", new BigDecimal("78.0100"), new BigDecimal("0.7801"))
                )));

        mockMvc.perform(get("/api/v1/convert/USD")
                        .param("amount", "100.0")
                        .param("targets", "EUR,GBP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("USD"))
                .andExpect(jsonPath("$.results[0].to").value("EUR"))
                .andExpect(jsonPath("$.results[1].to").value("GBP"));
    }

    @Test
    void convertToMany_missingTargets_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/convert/USD").param("amount", "100.0"))
                .andExpect(status().isBadRequest());
    }
}
