package com.example.exchangerate.alert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlertServiceTest {

    private AlertService alertService;
    private AlertRepository alertRepository;

    @BeforeEach
    void setUp() {
        alertRepository = new AlertRepository();
        alertService = new AlertService(alertRepository);
    }

    @Test
    void createAlert_returnsSavedAlertWithId() {
        Alert alert = Alert.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("85.00"))
                .email("test@example.com")
                .enabled(true)
                .build();

        Alert saved = alertService.createAlert(alert);

        assertNotNull(saved.getId());
        assertEquals("USD", saved.getFromCurrency());
        assertEquals("INR", saved.getToCurrency());
        assertTrue(saved.isEnabled());
    }

    @Test
    void getAlert_returnsAlertWhenExists() {
        Alert alert = Alert.builder()
                .fromCurrency("EUR").toCurrency("USD")
                .condition(Alert.AlertCondition.RATE_BELOW)
                .threshold(new BigDecimal("1.05"))
                .email("user@example.com")
                .enabled(true)
                .build();
        Alert saved = alertService.createAlert(alert);

        Alert found = alertService.getAlert(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void getAlert_returnsEmptyWhenNotFound() {
        assertTrue(alertService.getAlert("nonexistent").isEmpty());
    }

    @Test
    void getAllAlerts_returnsAllCreatedAlerts() {
        alertService.createAlert(Alert.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com").enabled(true).build());
        alertService.createAlert(Alert.builder()
                .fromCurrency("GBP").toCurrency("USD")
                .condition(Alert.AlertCondition.RATE_BELOW)
                .threshold(BigDecimal.TEN).email("c@d.com").enabled(false).build());

        List<Alert> all = alertService.getAllAlerts();

        assertEquals(2, all.size());
    }

    @Test
    void deleteAlert_removesAlert() {
        Alert saved = alertService.createAlert(Alert.builder()
                .fromCurrency("USD").toCurrency("JPY")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("150")).email("x@y.com").enabled(true).build());

        assertTrue(alertService.deleteAlert(saved.getId()));
        assertTrue(alertService.getAlert(saved.getId()).isEmpty());
    }

    @Test
    void deleteAlert_returnsFalseForNonexistent() {
        assertFalse(alertService.deleteAlert("nonexistent"));
    }

    @Test
    void toggleAlert_enablesAndDisables() {
        Alert saved = alertService.createAlert(Alert.builder()
                .fromCurrency("USD").toCurrency("CAD")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("1.30")).email("z@z.com").enabled(false).build());

        Alert toggledOn = alertService.toggleAlert(saved.getId(), true);
        assertTrue(toggledOn.isEnabled());

        Alert toggledOff = alertService.toggleAlert(saved.getId(), false);
        assertFalse(toggledOff.isEnabled());
    }

    @Test
    void getAlertsByCurrencyPair_returnsMatchingAlerts() {
        alertService.createAlert(Alert.builder()
                .fromCurrency("USD").toCurrency("INR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("80")).email("a@b.com").enabled(true).build());
        alertService.createAlert(Alert.builder()
                .fromCurrency("USD").toCurrency("INR")
                .condition(Alert.AlertCondition.RATE_BELOW)
                .threshold(new BigDecimal("75")).email("c@d.com").enabled(true).build());
        alertService.createAlert(Alert.builder()
                .fromCurrency("EUR").toCurrency("USD")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("1.20")).email("e@f.com").enabled(true).build());

        List<Alert> usdInrAlerts = alertService.getAlertsByCurrencyPair("USD", "INR");

        assertEquals(2, usdInrAlerts.size());
    }

    @Test
    void getAlertsByEmail_returnsMatchingAlerts() {
        alertService.createAlert(Alert.builder()
                .fromCurrency("USD").toCurrency("GBP")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("0.80")).email("same@example.com").enabled(true).build());
        alertService.createAlert(Alert.builder()
                .fromCurrency("EUR").toCurrency("GBP")
                .condition(Alert.AlertCondition.RATE_BELOW)
                .threshold(new BigDecimal("0.85")).email("same@example.com").enabled(true).build());

        List<Alert> byEmail = alertService.getAlertsByEmail("same@example.com");

        assertEquals(2, byEmail.size());
    }
}
