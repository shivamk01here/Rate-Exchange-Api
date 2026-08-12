package com.example.exchangerate.controllers;

import com.example.exchangerate.recentpair.RecentCurrencyPair;
import com.example.exchangerate.recentpair.RecentCurrencyPairRepository;
import com.example.exchangerate.recentpair.RecentCurrencyPairService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RecentCurrencyPairControllerTest {

    private RecentCurrencyPairController controller;

    @BeforeEach
    void setUp() {
        RecentCurrencyPairRepository repository = new RecentCurrencyPairRepository();
        RecentCurrencyPairService service = new RecentCurrencyPairService(repository);
        controller = new RecentCurrencyPairController(service);
    }

    @Test
    void recordPair_returnsRecordedPair() {
        RecentCurrencyPair result = controller.recordPair("USD", "INR");

        assertEquals("USD", result.getFromCurrency());
        assertEquals("INR", result.getToCurrency());
        assertEquals(1, result.getUseCount());
    }

    @Test
    void recordPair_throwsForBlankCodes() {
        assertThrows(ResponseStatusException.class, () -> controller.recordPair("", "INR"));
    }

    @Test
    void getRecentPairs_returnsAll() {
        controller.recordPair("USD", "INR");
        controller.recordPair("EUR", "GBP");

        List<RecentCurrencyPair> all = controller.getRecentPairs();

        assertEquals(2, all.size());
    }

    @Test
    void getPair_returnsPair() {
        controller.recordPair("USD", "INR");

        RecentCurrencyPair result = controller.getPair("USD", "INR");

        assertEquals("USD", result.getFromCurrency());
    }

    @Test
    void getPair_throwsForMissingPair() {
        assertThrows(ResponseStatusException.class, () -> controller.getPair("USD", "JPY"));
    }

    @Test
    void getTopRecent_limitsResults() {
        controller.recordPair("USD", "INR");
        controller.recordPair("EUR", "GBP");

        List<RecentCurrencyPair> top = controller.getTopRecent(1);

        assertEquals(1, top.size());
    }

    @Test
    void getMostUsed_returnsOrdered() {
        controller.recordPair("USD", "INR");
        controller.recordPair("USD", "INR");
        controller.recordPair("EUR", "GBP");

        List<RecentCurrencyPair> mostUsed = controller.getMostUsed(2);

        assertEquals("USD", mostUsed.get(0).getFromCurrency());
    }

    @Test
    void deletePair_returnsSuccess() {
        controller.recordPair("USD", "INR");

        Map<String, String> result = controller.deletePair("USD", "INR");

        assertEquals("deleted", result.get("status"));
    }

    @Test
    void clearAll_returnsCleared() {
        controller.recordPair("USD", "INR");

        Map<String, String> result = controller.clearAll();

        assertEquals("cleared", result.get("status"));
        assertEquals(0L, controller.getPairCount().get("count"));
    }

    @Test
    void getPairCount_returnsCount() {
        controller.recordPair("USD", "INR");

        Map<String, Object> result = controller.getPairCount();

        assertEquals(1L, result.get("count"));
    }
}
