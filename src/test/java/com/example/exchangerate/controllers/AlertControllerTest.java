package com.example.exchangerate.controllers;

import com.example.exchangerate.alert.Alert;
import com.example.exchangerate.alert.AlertRepository;
import com.example.exchangerate.alert.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AlertControllerTest {

    private AlertController controller;

    @BeforeEach
    void setUp() {
        AlertRepository repository = new AlertRepository();
        AlertService service = new AlertService(repository);
        controller = new AlertController(service);
    }

    @Test
    void createAlert_returnsCreatedAlert() {
        Alert alert = Alert.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("85.00"))
                .email("test@example.com")
                .enabled(true)
                .build();

        Alert result = controller.createAlert(alert);

        assertNotNull(result.getId());
        assertEquals("USD", result.getFromCurrency());
    }

    @Test
    void createAlert_throwsWhenEmailMissing() {
        Alert alert = Alert.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE)
                .enabled(true)
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.createAlert(alert));
    }

    @Test
    void createAlert_throwsWhenThresholdMissing() {
        Alert alert = Alert.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .email("test@example.com")
                .enabled(true)
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.createAlert(alert));
    }

    @Test
    void getAllAlerts_returnsAllAlerts() {
        controller.createAlert(Alert.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("1.10")).email("a@b.com").enabled(true).build());
        controller.createAlert(Alert.builder()
                .fromCurrency("GBP").toCurrency("USD")
                .condition(Alert.AlertCondition.RATE_BELOW)
                .threshold(new BigDecimal("1.25")).email("c@d.com").enabled(false).build());

        List<Alert> all = controller.getAllAlerts();

        assertEquals(2, all.size());
    }

    @Test
    void getAlert_returnsAlertById() {
        Alert created = controller.createAlert(Alert.builder()
                .fromCurrency("USD").toCurrency("JPY")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("150")).email("x@y.com").enabled(true).build());

        Alert result = controller.getAlert(created.getId());

        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getAlert_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getAlert("bad-id"));
    }

    @Test
    void deleteAlert_returnsSuccess() {
        Alert created = controller.createAlert(Alert.builder()
                .fromCurrency("USD").toCurrency("CAD")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("1.30")).email("z@z.com").enabled(true).build());

        Map<String, String> result = controller.deleteAlert(created.getId());

        assertEquals("deleted", result.get("status"));
        assertThrows(ResponseStatusException.class, () -> controller.getAlert(created.getId()));
    }

    @Test
    void deleteAlert_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.deleteAlert("bad-id"));
    }

    @Test
    void toggleAlert_changesEnabledState() {
        Alert created = controller.createAlert(Alert.builder()
                .fromCurrency("USD").toCurrency("GBP")
                .condition(Alert.AlertCondition.RATE_BELOW)
                .threshold(new BigDecimal("0.75")).email("t@t.com").enabled(false).build());

        Alert toggled = controller.toggleAlert(created.getId(), Map.of("enabled", true));

        assertTrue(toggled.isEnabled());
    }

    @Test
    void getAlertsByPair_returnsFilteredAlerts() {
        controller.createAlert(Alert.builder()
                .fromCurrency("USD").toCurrency("INR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("80")).email("a@b.com").enabled(true).build());
        controller.createAlert(Alert.builder()
                .fromCurrency("USD").toCurrency("INR")
                .condition(Alert.AlertCondition.RATE_BELOW)
                .threshold(new BigDecimal("75")).email("c@d.com").enabled(true).build());

        List<Alert> filtered = controller.getAlertsByPair("USD", "INR");

        assertEquals(2, filtered.size());
    }

    @Test
    void getAlertsByEmail_returnsFilteredAlerts() {
        controller.createAlert(Alert.builder()
                .fromCurrency("USD").toCurrency("GBP")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("0.80")).email("same@example.com").enabled(true).build());
        controller.createAlert(Alert.builder()
                .fromCurrency("EUR").toCurrency("GBP")
                .condition(Alert.AlertCondition.RATE_BELOW)
                .threshold(new BigDecimal("0.85")).email("same@example.com").enabled(true).build());

        List<Alert> byEmail = controller.getAlertsByEmail("same@example.com");

        assertEquals(2, byEmail.size());
    }
}
