package com.example.exchangerate.recentpair;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RecentCurrencyPairServiceTest {

    private RecentCurrencyPairService pairService;
    private RecentCurrencyPairRepository pairRepository;

    @BeforeEach
    void setUp() {
        pairRepository = new RecentCurrencyPairRepository();
        pairService = new RecentCurrencyPairService(pairRepository);
    }

    @Test
    void recordPair_createsPairWithCountOne() {
        RecentCurrencyPair pair = pairService.recordPair("USD", "INR");

        assertEquals("USD", pair.getFromCurrency());
        assertEquals("INR", pair.getToCurrency());
        assertEquals(1, pair.getUseCount());
        assertNotNull(pair.getLastUsedAt());
    }

    @Test
    void recordPair_incrementsCountAndNormalizesCase() {
        pairService.recordPair("usd", "inr");
        RecentCurrencyPair pair = pairService.recordPair("USD", "INR");

        assertEquals(2, pair.getUseCount());
        assertEquals("USD", pair.getFromCurrency());
        assertEquals("INR", pair.getToCurrency());
    }

    @Test
    void recordPair_throwsForBlankCodes() {
        assertThrows(IllegalArgumentException.class, () -> pairService.recordPair("", "INR"));
        assertThrows(IllegalArgumentException.class, () -> pairService.recordPair("USD", null));
    }

    @Test
    void getPair_returnsPairWhenExists() {
        pairService.recordPair("USD", "INR");

        Optional<RecentCurrencyPair> found = pairService.getPair("usd", "inr");

        assertTrue(found.isPresent());
        assertEquals("USD", found.get().getFromCurrency());
    }

    @Test
    void getRecentPairs_returnsNewestFirst() {
        RecentCurrencyPairRepository fresh = new RecentCurrencyPairRepository();
        RecentCurrencyPairService service = new RecentCurrencyPairService(fresh);
        fresh.recordUse("USD", "INR", Instant.parse("2026-08-01T10:00:00Z"));
        fresh.recordUse("EUR", "GBP", Instant.parse("2026-08-02T10:00:00Z"));
        fresh.recordUse("JPY", "USD", Instant.parse("2026-08-03T10:00:00Z"));

        List<RecentCurrencyPair> recent = service.getRecentPairs();

        assertEquals(3, recent.size());
        assertEquals("JPY", recent.get(0).getFromCurrency());
        assertEquals("EUR", recent.get(1).getFromCurrency());
        assertEquals("USD", recent.get(2).getFromCurrency());
    }

    @Test
    void getTopRecent_limitsResults() {
        pairService.recordPair("USD", "INR");
        pairService.recordPair("EUR", "GBP");

        List<RecentCurrencyPair> top = pairService.getTopRecent(1);

        assertEquals(1, top.size());
    }

    @Test
    void getMostUsed_ordersByUseCount() {
        pairService.recordPair("USD", "INR");
        pairService.recordPair("USD", "INR");
        pairService.recordPair("USD", "INR");
        pairService.recordPair("EUR", "GBP");
        pairService.recordPair("EUR", "GBP");

        List<RecentCurrencyPair> mostUsed = pairService.getMostUsed(2);

        assertEquals("USD", mostUsed.get(0).getFromCurrency());
        assertEquals(3, mostUsed.get(0).getUseCount());
    }

    @Test
    void deletePair_removesPair() {
        pairService.recordPair("USD", "INR");

        assertTrue(pairService.deletePair("usd", "inr"));
        assertFalse(pairService.deletePair("usd", "inr"));
        assertTrue(pairService.getPair("USD", "INR").isEmpty());
    }

    @Test
    void clearAll_emptiesStore() {
        pairService.recordPair("USD", "INR");
        pairService.recordPair("EUR", "GBP");

        pairService.clearAll();

        assertEquals(0, pairService.getPairCount());
    }

    @Test
    void getPairCount_returnsCorrectCount() {
        assertEquals(0, pairService.getPairCount());

        pairService.recordPair("USD", "INR");

        assertEquals(1, pairService.getPairCount());
    }
}
