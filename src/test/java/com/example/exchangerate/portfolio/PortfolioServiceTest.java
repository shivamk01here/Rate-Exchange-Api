package com.example.exchangerate.portfolio;

import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.services.ExchangeRateOrchestrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    private PortfolioService portfolioService;
    private PortfolioRepository portfolioRepository;

    @Mock
    private ExchangeRateOrchestrationService orchestrationService;

    @BeforeEach
    void setUp() {
        portfolioRepository = new PortfolioRepository();
        portfolioService = new PortfolioService(portfolioRepository, orchestrationService);
    }

    @Test
    void createPortfolio_returnsSavedPortfolioWithId() {
        CurrencyPortfolio portfolio = CurrencyPortfolio.builder()
                .name("My Portfolio")
                .baseCurrency("USD")
                .build();

        CurrencyPortfolio saved = portfolioService.createPortfolio(portfolio);

        assertNotNull(saved.getId());
        assertEquals("My Portfolio", saved.getName());
        assertEquals("USD", saved.getBaseCurrency());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void getPortfolio_returnsPortfolioWhenExists() {
        CurrencyPortfolio saved = portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("Test Portfolio")
                .baseCurrency("EUR")
                .build());

        CurrencyPortfolio found = portfolioService.getPortfolio(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
        assertEquals("Test Portfolio", found.getName());
    }

    @Test
    void getPortfolio_returnsEmptyWhenNotFound() {
        assertTrue(portfolioService.getPortfolio("nonexistent").isEmpty());
    }

    @Test
    void getAllPortfolios_returnsAllCreatedPortfolios() {
        portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("Portfolio 1").baseCurrency("USD").build());
        portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("Portfolio 2").baseCurrency("EUR").build());

        List<CurrencyPortfolio> all = portfolioService.getAllPortfolios();

        assertEquals(2, all.size());
    }

    @Test
    void deletePortfolio_removesPortfolio() {
        CurrencyPortfolio saved = portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("Delete Me").baseCurrency("USD").build());

        assertTrue(portfolioService.deletePortfolio(saved.getId()));
        assertTrue(portfolioService.getPortfolio(saved.getId()).isEmpty());
    }

    @Test
    void deletePortfolio_returnsFalseForNonexistent() {
        assertFalse(portfolioService.deletePortfolio("nonexistent"));
    }

    @Test
    void updatePortfolio_updatesFields() {
        CurrencyPortfolio saved = portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("Old Name").baseCurrency("USD").build());

        CurrencyPortfolio updated = portfolioService.updatePortfolio(saved.getId(),
                CurrencyPortfolio.builder().name("New Name").build());

        assertEquals("New Name", updated.getName());
        assertEquals("USD", updated.getBaseCurrency());
    }

    @Test
    void updatePortfolio_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> portfolioService.updatePortfolio("bad-id",
                        CurrencyPortfolio.builder().name("test").build()));
    }

    @Test
    void addHolding_addsNewCurrencyHolding() {
        CurrencyPortfolio saved = portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("Test").baseCurrency("USD").build());

        CurrencyPortfolio updated = portfolioService.addHolding(saved.getId(), "EUR", new BigDecimal("500"));

        assertEquals(1, updated.getHoldings().size());
        assertEquals(new BigDecimal("500"), updated.getHoldings().get("EUR"));
    }

    @Test
    void addHolding_mergesExistingAmount() {
        CurrencyPortfolio saved = portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("Test").baseCurrency("USD")
                .holdings(new LinkedHashMap<>(Map.of("EUR", new BigDecimal("100"))))
                .build());

        CurrencyPortfolio updated = portfolioService.addHolding(saved.getId(), "EUR", new BigDecimal("200"));

        assertEquals(new BigDecimal("300"), updated.getHoldings().get("EUR"));
    }

    @Test
    void addHolding_throwsForNegativeAmount() {
        CurrencyPortfolio saved = portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("Test").baseCurrency("USD").build());

        assertThrows(IllegalArgumentException.class,
                () -> portfolioService.addHolding(saved.getId(), "EUR", new BigDecimal("-100")));
    }

    @Test
    void removeHolding_removesCurrencyFromPortfolio() {
        CurrencyPortfolio saved = portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("Test").baseCurrency("USD")
                .holdings(new LinkedHashMap<>(Map.of("EUR", new BigDecimal("500"), "GBP", new BigDecimal("300"))))
                .build());

        CurrencyPortfolio updated = portfolioService.removeHolding(saved.getId(), "EUR");

        assertEquals(1, updated.getHoldings().size());
        assertNull(updated.getHoldings().get("EUR"));
        assertNotNull(updated.getHoldings().get("GBP"));
    }

    @Test
    void removeHolding_throwsForNonexistentCurrency() {
        CurrencyPortfolio saved = portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("Test").baseCurrency("USD").build());

        assertThrows(IllegalArgumentException.class,
                () -> portfolioService.removeHolding(saved.getId(), "EUR"));
    }

    @Test
    void removeHolding_throwsForNonexistentPortfolio() {
        assertThrows(IllegalArgumentException.class,
                () -> portfolioService.removeHolding("bad-id", "EUR"));
    }

    @Test
    void getPortfolioCount_returnsCorrectCount() {
        assertEquals(0, portfolioService.getPortfolioCount());

        portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("P1").baseCurrency("USD").build());
        portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("P2").baseCurrency("EUR").build());

        assertEquals(2, portfolioService.getPortfolioCount());
    }

    @Test
    void valuatePortfolio_returnsEmptyStatusForNoHoldings() throws Exception {
        CurrencyPortfolio saved = portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("Empty").baseCurrency("USD").build());

        PortfolioValuation valuation = portfolioService.valuatePortfolio(saved.getId()).get();

        assertEquals("EMPTY", valuation.getStatus());
        assertEquals(BigDecimal.ZERO, valuation.getTotalValue());
    }

    @Test
    void valuatePortfolio_returnsSameCurrencyValue() throws Exception {
        CurrencyPortfolio saved = portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("USD Only").baseCurrency("USD")
                .holdings(new LinkedHashMap<>(Map.of("USD", new BigDecimal("1000"))))
                .build());

        PortfolioValuation valuation = portfolioService.valuatePortfolio(saved.getId()).get();

        assertEquals("SUCCESS", valuation.getStatus());
        assertEquals(new BigDecimal("1000"), valuation.getTotalValue());
        assertEquals("SAME_CURRENCY", valuation.getHoldingValues().get("USD").getStatus());
    }

    @Test
    void valuatePortfolio_convertsForeignCurrency() throws Exception {
        CurrencyPortfolio saved = portfolioService.createPortfolio(CurrencyPortfolio.builder()
                .name("Mixed").baseCurrency("USD")
                .holdings(new LinkedHashMap<>(Map.of(
                        "USD", new BigDecimal("1000"),
                        "EUR", new BigDecimal("500"))))
                .build());

        ExchangeRateResponse response = ExchangeRateResponse.builder()
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .fromCurrency("EUR")
                .toCurrency("USD")
                .rate(new BigDecimal("1.1"))
                .convertedAmount(new BigDecimal("550"))
                .status("SUCCESS")
                .build();

        when(orchestrationService.getRate(any(ExchangeRateRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        PortfolioValuation valuation = portfolioService.valuatePortfolio(saved.getId()).get();

        assertEquals("SUCCESS", valuation.getStatus());
        assertEquals(new BigDecimal("1550.0000"), valuation.getTotalValue());
    }

    @Test
    void valuatePortfolio_throwsForNonexistent() {
        assertThrows(Exception.class,
                () -> portfolioService.valuatePortfolio("bad-id").get());
    }
}
