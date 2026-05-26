package ch.jaywalker.stu.partnerbillingservice.exchanger.controller;

import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.CurrencyNotFoundException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.ExternalApiException;
import ch.jaywalker.stu.partnerbillingservice.exchanger.exception.GlobalExceptionHandler;
import ch.jaywalker.stu.partnerbillingservice.exchanger.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.stream.Stream;

import static ch.jaywalker.stu.partnerbillingservice.exchanger.TestDataProvider.*;
import static org.junit.jupiter.api.Assertions.assertAll;
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
    
    
    @ParameterizedTest
    @MethodSource("missingRequiredParamProvider")
    void givenMissingRequiredParam_whenConvertEndpointCalled_thenReturns400(
            MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }
    
    
    @ParameterizedTest
    @MethodSource("validCurrencyPairsProvider")
    void givenDifferentValidCurrencyPairs_whenGetRate_thenAlwaysReturns200(
            String origin, String target) throws Exception {
        when(exchangeRateService.getRate(origin, target)).thenReturn(rateResponse(origin, target));

        mockMvc.perform(get(URL_RATES, origin, target))
                .andExpect(status().isOk());
    }

    @Test
    void givenUnknownCurrency_whenGetRate_thenReturns404WithProblemDetail() throws Exception {
        when(exchangeRateService.getRate(USD, UNKNOWN_CURRENCY))
                .thenThrow(new CurrencyNotFoundException(UNKNOWN_CURRENCY));

        ResultActions result = mockMvc.perform(get(URL_RATES, USD, UNKNOWN_CURRENCY));

        assertAll(
            () -> result.andExpect(status().isNotFound()),
            () -> result.andExpect(jsonPath(JSON_ERROR_TITLE).value(ERROR_TITLE_CURRENCY_NOT_FOUND))
        );
    }
    
    @Test
    void givenValidCurrencyPair_whenGetRate_thenReturns200WithRateResponse() throws Exception {
        when(exchangeRateService.getRate(USD, EUR)).thenReturn(rateResponse());
        
        ResultActions result = mockMvc.perform(get(URL_RATES, USD, EUR));
        
        assertAll(
                () -> result.andExpect(status().isOk()),
                () -> result.andExpect(jsonPath(JSON_ORIGIN_CURRENCY).value(USD)),
                () -> result.andExpect(jsonPath(JSON_TARGET_CURRENCY).value(EUR)),
                () -> result.andExpect(jsonPath(JSON_RATE).value(EUR_RATE))
        );
    }

    @Test
    void givenExternalApiUnavailable_whenGetRate_thenReturns502WithProblemDetail() throws Exception {
        when(exchangeRateService.getRate(USD, EUR))
                .thenThrow(new ExternalApiException("provider down"));

        ResultActions result = mockMvc.perform(get(URL_RATES, USD, EUR));

        assertAll(
            () -> result.andExpect(status().isBadGateway()),
            () -> result.andExpect(jsonPath(JSON_ERROR_TITLE).value(ERROR_TITLE_EXTERNAL_API_UNAVAILABLE))
        );
    }

    @Test
    void givenValidBase_whenGetAllRates_thenReturns200WithRatesMap() throws Exception {
        when(exchangeRateService.getAllRates(USD)).thenReturn(ratesResponse());

        ResultActions result = mockMvc.perform(get(URL_RATES_ALL, USD));

        assertAll(
            () -> result.andExpect(status().isOk()),
            () -> result.andExpect(jsonPath(JSON_BASE).value(USD)),
            () -> result.andExpect(jsonPath("$.rates." + EUR).value(EUR_RATE)),
            () -> result.andExpect(jsonPath("$.rates." + GBP).value(GBP_RATE))
        );
    }

    @Test
    void givenValidInput_whenConvert_thenReturns200WithConversionResponse() throws Exception {
        when(exchangeRateService.convert(USD, EUR, AMOUNT_100)).thenReturn(conversionResponse());

        ResultActions result = mockMvc.perform(get(URL_CONVERT, USD, EUR)
                .param(PARAM_AMOUNT, AMOUNT_100.toPlainString()));

        assertAll(
            () -> result.andExpect(status().isOk()),
            () -> result.andExpect(jsonPath(JSON_ORIGIN_CURRENCY).value(USD)),
            () -> result.andExpect(jsonPath(JSON_TARGET_CURRENCY).value(EUR)),
            () -> result.andExpect(jsonPath(JSON_CONVERTED).value(EUR_CONVERTED_100))
        );
    }
    
    @Test
    void givenValidInput_whenConvertToMany_thenReturns200WithMultiConversionResponse() throws Exception {
        when(exchangeRateService.convertToMany(USD, AMOUNT_100, java.util.List.of(EUR, GBP)))
                .thenReturn(multiConversionResponse());
        
        ResultActions result = mockMvc.perform(get(URL_CONVERT_MULTI, USD)
                .param(PARAM_AMOUNT, AMOUNT_100.toPlainString())
                .param(PARAM_TARGETS, EUR + "," + GBP));
        
        assertAll(
                () -> result.andExpect(status().isOk()),
                () -> result.andExpect(jsonPath(JSON_ORIGIN_CURRENCY).value(USD)),
                () -> result.andExpect(jsonPath(JSON_RESULT_0_TARGET).value(EUR)),
                () -> result.andExpect(jsonPath(JSON_RESULT_1_TARGET).value(GBP))
        );
    }

    static Stream<MockHttpServletRequestBuilder> missingRequiredParamProvider() {
        return Stream.of(
                get(URL_CONVERT, USD, EUR),
                get(URL_CONVERT_MULTI, USD).param(PARAM_AMOUNT, AMOUNT_100.toPlainString()),
                get(URL_CONVERT_MULTI, USD).param(PARAM_TARGETS, EUR + "," + GBP)
        );
    }
    
    static Stream<Arguments> validCurrencyPairsProvider() {
        return Stream.of(
                Arguments.of(USD, GBP),
                Arguments.of(EUR, USD),
                Arguments.of(GBP, JPY)
        );
    }
}
