package com.example.exchangerate.models;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConversionQueryParamsTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void toExchangeRateRequest_convertsCorrectly() {
        ConversionQueryParams params = ConversionQueryParams.builder()
                .from("usd")
                .to("eur")
                .amount(new BigDecimal("100"))
                .build();

        ExchangeRateRequest request = params.toExchangeRateRequest();

        assertEquals("USD", request.getFromCurrency());
        assertEquals("EUR", request.getToCurrency());
        assertEquals(new BigDecimal("100"), request.getAmount());
    }

    @Test
    void toExchangeRateRequest_preservesUppercaseInput() {
        ConversionQueryParams params = ConversionQueryParams.builder()
                .from("GBP")
                .to("JPY")
                .amount(new BigDecimal("50.50"))
                .build();

        ExchangeRateRequest request = params.toExchangeRateRequest();

        assertEquals("GBP", request.getFromCurrency());
        assertEquals("JPY", request.getToCurrency());
    }

    @Test
    void validation_failsWhenFromCurrencyIsNull() {
        ConversionQueryParams params = ConversionQueryParams.builder()
                .from(null)
                .to("EUR")
                .amount(new BigDecimal("100"))
                .build();

        Set<ConstraintViolation<ConversionQueryParams>> violations = validator.validate(params);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("from")));
    }

    @Test
    void validation_failsWhenToCurrencyIsNull() {
        ConversionQueryParams params = ConversionQueryParams.builder()
                .from("USD")
                .to(null)
                .amount(new BigDecimal("100"))
                .build();

        Set<ConstraintViolation<ConversionQueryParams>> violations = validator.validate(params);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("to")));
    }

    @Test
    void validation_failsWhenAmountIsNull() {
        ConversionQueryParams params = ConversionQueryParams.builder()
                .from("USD")
                .to("EUR")
                .amount(null)
                .build();

        Set<ConstraintViolation<ConversionQueryParams>> violations = validator.validate(params);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void validation_failsWhenAmountIsNegative() {
        ConversionQueryParams params = ConversionQueryParams.builder()
                .from("USD")
                .to("EUR")
                .amount(new BigDecimal("-10"))
                .build();

        Set<ConstraintViolation<ConversionQueryParams>> violations = validator.validate(params);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void validation_failsWhenAmountIsZero() {
        ConversionQueryParams params = ConversionQueryParams.builder()
                .from("USD")
                .to("EUR")
                .amount(BigDecimal.ZERO)
                .build();

        Set<ConstraintViolation<ConversionQueryParams>> violations = validator.validate(params);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void validation_passesForValidParams() {
        ConversionQueryParams params = ConversionQueryParams.builder()
                .from("USD")
                .to("EUR")
                .amount(new BigDecimal("100"))
                .build();

        Set<ConstraintViolation<ConversionQueryParams>> violations = validator.validate(params);
        assertTrue(violations.isEmpty());
    }

    @Test
    void builder_defaultsWorkCorrectly() {
        ConversionQueryParams params = new ConversionQueryParams();
        assertNull(params.getFrom());
        assertNull(params.getTo());
        assertNull(params.getAmount());
    }
}
