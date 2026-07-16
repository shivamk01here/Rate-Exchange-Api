package com.example.exchangerate.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SymbolLookupServiceTest {

    private SymbolLookupService symbolLookupService;

    @BeforeEach
    void setUp() {
        CurrencyService currencyService = new CurrencyService();
        symbolLookupService = new SymbolLookupService(currencyService);
    }

    @Test
    void getSymbol_returnsSymbolForValidCode() {
        assertEquals("$", symbolLookupService.getSymbol("USD"));
    }

    @Test
    void getSymbol_returnsSymbolCaseInsensitive() {
        assertEquals("\u20AC", symbolLookupService.getSymbol("eur"));
    }

    @Test
    void getSymbol_returnsNullForInvalidCode() {
        assertNull(symbolLookupService.getSymbol("XYZ"));
    }

    @Test
    void getSymbol_returnsNullForNullCode() {
        assertNull(symbolLookupService.getSymbol(null));
    }

    @Test
    void getSymbol_returnsNullForBlankCode() {
        assertNull(symbolLookupService.getSymbol("  "));
    }

    @Test
    void getAllSymbols_returnsAllCurrencySymbols() {
        Map<String, String> symbols = symbolLookupService.getAllSymbols();
        assertNotNull(symbols);
        assertEquals(20, symbols.size());
        assertEquals("$", symbols.get("USD"));
        assertEquals("\u20AC", symbols.get("EUR"));
        assertEquals("\u00A3", symbols.get("GBP"));
        assertEquals("\u20B9", symbols.get("INR"));
    }

    @Test
    void isKnownSymbol_returnsTrueForValidSymbol() {
        assertTrue(symbolLookupService.isKnownSymbol("$"));
    }

    @Test
    void isKnownSymbol_returnsTrueForUniqueSymbol() {
        assertTrue(symbolLookupService.isKnownSymbol("\u20B9"));
    }

    @Test
    void isKnownSymbol_returnsFalseForUnknownSymbol() {
        assertFalse(symbolLookupService.isKnownSymbol("XYZ"));
    }

    @Test
    void isKnownSymbol_returnsFalseForNullSymbol() {
        assertFalse(symbolLookupService.isKnownSymbol(null));
    }

    @Test
    void isKnownSymbol_returnsFalseForBlankSymbol() {
        assertFalse(symbolLookupService.isKnownSymbol("  "));
    }
}
