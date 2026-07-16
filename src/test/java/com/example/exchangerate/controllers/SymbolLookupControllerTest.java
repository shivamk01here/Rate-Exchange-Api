package com.example.exchangerate.controllers;

import com.example.exchangerate.models.SymbolResponse;
import com.example.exchangerate.services.CurrencyService;
import com.example.exchangerate.services.SymbolLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SymbolLookupControllerTest {

    private SymbolLookupController symbolLookupController;

    @BeforeEach
    void setUp() {
        CurrencyService currencyService = new CurrencyService();
        SymbolLookupService symbolLookupService = new SymbolLookupService(currencyService);
        symbolLookupController = new SymbolLookupController(symbolLookupService);
    }

    @Test
    void getSymbol_returnsSymbolForValidCode() {
        SymbolResponse response = symbolLookupController.getSymbol("USD");
        assertNotNull(response);
        assertEquals("USD", response.getCode());
        assertEquals("$", response.getSymbol());
    }

    @Test
    void getSymbol_returnsSymbolCaseInsensitive() {
        SymbolResponse response = symbolLookupController.getSymbol("eur");
        assertNotNull(response);
        assertEquals("EUR", response.getCode());
        assertEquals("\u20AC", response.getSymbol());
    }

    @Test
    void getSymbol_throwsExceptionForInvalidCode() {
        assertThrows(ResponseStatusException.class, () -> symbolLookupController.getSymbol("XYZ"));
    }

    @Test
    void getAllSymbols_returnsAllSymbols() {
        Map<String, String> symbols = symbolLookupController.getAllSymbols();
        assertNotNull(symbols);
        assertEquals(20, symbols.size());
        assertEquals("$", symbols.get("USD"));
        assertEquals("\u20B9", symbols.get("INR"));
    }

    @Test
    void checkSymbol_returnsTrueForKnownSymbol() {
        Map<String, Object> result = symbolLookupController.checkSymbol("$");
        assertNotNull(result);
        assertEquals("$", result.get("symbol"));
        assertEquals(true, result.get("known"));
    }

    @Test
    void checkSymbol_returnsFalseForUnknownSymbol() {
        Map<String, Object> result = symbolLookupController.checkSymbol("XYZ");
        assertNotNull(result);
        assertEquals("XYZ", result.get("symbol"));
        assertEquals(false, result.get("known"));
    }
}
