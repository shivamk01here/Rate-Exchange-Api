package com.example.exchangerate.controllers;

import com.example.exchangerate.portfolio.CurrencyPortfolio;
import com.example.exchangerate.portfolio.HoldingRequest;
import com.example.exchangerate.portfolio.PortfolioRepository;
import com.example.exchangerate.portfolio.PortfolioService;
import com.example.exchangerate.services.ExchangeRateOrchestrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PortfolioControllerTest {

    private PortfolioController controller;

    @BeforeEach
    void setUp() {
        PortfolioRepository repository = new PortfolioRepository();
        ExchangeRateOrchestrationService orchestrationService = mock(ExchangeRateOrchestrationService.class);
        PortfolioService service = new PortfolioService(repository, orchestrationService);
        controller = new PortfolioController(service);
    }

    @Test
    void createPortfolio_returnsCreatedPortfolio() {
        CurrencyPortfolio portfolio = CurrencyPortfolio.builder()
                .name("My Portfolio")
                .baseCurrency("USD")
                .build();

        CurrencyPortfolio result = controller.createPortfolio(portfolio);

        assertNotNull(result.getId());
        assertEquals("My Portfolio", result.getName());
        assertEquals("USD", result.getBaseCurrency());
    }

    @Test
    void createPortfolio_throwsWhenNameMissing() {
        CurrencyPortfolio portfolio = CurrencyPortfolio.builder()
                .baseCurrency("USD")
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.createPortfolio(portfolio));
    }

    @Test
    void createPortfolio_throwsWhenBaseCurrencyMissing() {
        CurrencyPortfolio portfolio = CurrencyPortfolio.builder()
                .name("Test")
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.createPortfolio(portfolio));
    }

    @Test
    void getAllPortfolios_returnsAllPortfolios() {
        controller.createPortfolio(CurrencyPortfolio.builder()
                .name("P1").baseCurrency("USD").build());
        controller.createPortfolio(CurrencyPortfolio.builder()
                .name("P2").baseCurrency("EUR").build());

        List<CurrencyPortfolio> all = controller.getAllPortfolios();

        assertEquals(2, all.size());
    }

    @Test
    void getPortfolio_returnsPortfolioById() {
        CurrencyPortfolio created = controller.createPortfolio(CurrencyPortfolio.builder()
                .name("Test").baseCurrency("USD").build());

        CurrencyPortfolio result = controller.getPortfolio(created.getId());

        assertEquals(created.getId(), result.getId());
        assertEquals("Test", result.getName());
    }

    @Test
    void getPortfolio_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getPortfolio("bad-id"));
    }

    @Test
    void updatePortfolio_updatesExistingPortfolio() {
        CurrencyPortfolio created = controller.createPortfolio(CurrencyPortfolio.builder()
                .name("Old Name").baseCurrency("USD").build());

        CurrencyPortfolio updated = controller.updatePortfolio(created.getId(),
                CurrencyPortfolio.builder().name("New Name").build());

        assertEquals("New Name", updated.getName());
        assertEquals("USD", updated.getBaseCurrency());
    }

    @Test
    void updatePortfolio_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class,
                () -> controller.updatePortfolio("bad-id", CurrencyPortfolio.builder().build()));
    }

    @Test
    void deletePortfolio_returnsSuccess() {
        CurrencyPortfolio created = controller.createPortfolio(CurrencyPortfolio.builder()
                .name("Delete Me").baseCurrency("USD").build());

        Map<String, String> result = controller.deletePortfolio(created.getId());

        assertEquals("deleted", result.get("status"));
        assertThrows(ResponseStatusException.class, () -> controller.getPortfolio(created.getId()));
    }

    @Test
    void deletePortfolio_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.deletePortfolio("bad-id"));
    }

    @Test
    void addHolding_addsHoldingToPortfolio() {
        CurrencyPortfolio created = controller.createPortfolio(CurrencyPortfolio.builder()
                .name("Test").baseCurrency("USD").build());

        CurrencyPortfolio updated = controller.addHolding(created.getId(),
                HoldingRequest.builder().currency("EUR").amount(new BigDecimal("500")).build());

        assertEquals(1, updated.getHoldings().size());
        assertEquals(new BigDecimal("500"), updated.getHoldings().get("EUR"));
    }

    @Test
    void addHolding_throwsForNonexistentPortfolio() {
        assertThrows(ResponseStatusException.class,
                () -> controller.addHolding("bad-id",
                        HoldingRequest.builder().currency("EUR").amount(new BigDecimal("100")).build()));
    }

    @Test
    void removeHolding_removesHoldingFromPortfolio() {
        CurrencyPortfolio created = controller.createPortfolio(CurrencyPortfolio.builder()
                .name("Test").baseCurrency("USD")
                .holdings(new java.util.LinkedHashMap<>(Map.of("EUR", new BigDecimal("500"))))
                .build());

        CurrencyPortfolio updated = controller.removeHolding(created.getId(), "EUR");

        assertTrue(updated.getHoldings().isEmpty());
    }

    @Test
    void removeHolding_throwsForNonexistentCurrency() {
        CurrencyPortfolio created = controller.createPortfolio(CurrencyPortfolio.builder()
                .name("Test").baseCurrency("USD").build());

        assertThrows(ResponseStatusException.class,
                () -> controller.removeHolding(created.getId(), "EUR"));
    }

    @Test
    void getPortfolioCount_returnsZeroInitially() {
        Map<String, Long> result = controller.getPortfolioCount();

        assertEquals(0L, result.get("count"));
    }

    @Test
    void getPortfolioCount_returnsCorrectCount() {
        controller.createPortfolio(CurrencyPortfolio.builder()
                .name("P1").baseCurrency("USD").build());
        controller.createPortfolio(CurrencyPortfolio.builder()
                .name("P2").baseCurrency("EUR").build());

        Map<String, Long> result = controller.getPortfolioCount();

        assertEquals(2L, result.get("count"));
    }
}
