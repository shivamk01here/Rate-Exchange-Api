package com.example.exchangerate;

import com.example.exchangerate.models.CurrencyInfo;
import com.example.exchangerate.services.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyServiceTest {

    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        currencyService = new CurrencyService();
    }

    @Test
    void getSupportedCurrencies_returnsAllCurrencies() {
        Collection<CurrencyInfo> currencies = currencyService.getSupportedCurrencies();
        assertNotNull(currencies);
        assertFalse(currencies.isEmpty());
        assertEquals(20, currencies.size());
    }

    @Test
    void getSupportedCurrencies_containsUSD() {
        Collection<CurrencyInfo> currencies = currencyService.getSupportedCurrencies();
        assertTrue(currencies.stream().anyMatch(c -> "USD".equals(c.getCode())));
    }

    @Test
    void getCurrency_returnsCurrencyForValidCode() {
        CurrencyInfo usd = currencyService.getCurrency("USD");
        assertNotNull(usd);
        assertEquals("USD", usd.getCode());
        assertEquals("US Dollar", usd.getName());
        assertEquals("$", usd.getSymbol());
        assertEquals(2, usd.getDecimalPlaces());
        assertEquals(840, usd.getNumericCode());
    }

    @Test
    void getCurrency_returnsCurrencyCaseInsensitive() {
        CurrencyInfo currency = currencyService.getCurrency("usd");
        assertNotNull(currency);
        assertEquals("USD", currency.getCode());
    }

    @Test
    void getCurrency_returnsNullForInvalidCode() {
        assertNull(currencyService.getCurrency("XYZ"));
    }

    @Test
    void getCurrency_returnsNullForNullCode() {
        assertNull(currencyService.getCurrency(null));
    }

    @Test
    void isSupported_returnsTrueForValidCode() {
        assertTrue(currencyService.isSupported("EUR"));
    }

    @Test
    void isSupported_returnsFalseForInvalidCode() {
        assertFalse(currencyService.isSupported("XYZ"));
    }

    @Test
    void isSupported_returnsFalseForNullCode() {
        assertFalse(currencyService.isSupported(null));
    }

    @Test
    void getCurrency_returnsCorrectJPYDetails() {
        CurrencyInfo jpy = currencyService.getCurrency("JPY");
        assertNotNull(jpy);
        assertEquals("JPY", jpy.getCode());
        assertEquals(0, jpy.getDecimalPlaces());
        assertEquals(392, jpy.getNumericCode());
    }

    @Test
    void getCurrency_returnsCorrectINRDetails() {
        CurrencyInfo inr = currencyService.getCurrency("INR");
        assertNotNull(inr);
        assertEquals("INR", inr.getCode());
        assertEquals("\u20B9", inr.getSymbol());
        assertEquals(356, inr.getNumericCode());
    }
}
