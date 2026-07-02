package com.example.exchangerate.controllers;

import com.example.exchangerate.alert.Alert;
import com.example.exchangerate.alert.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Alert createAlert(@Valid @RequestBody Alert alert) {
        if (alert.getEmail() == null || alert.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (alert.getThreshold() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Threshold is required");
        }
        log.info("Creating alert: {}->{} {} {} email={}",
                alert.getFromCurrency(), alert.getToCurrency(),
                alert.getCondition(), alert.getThreshold(), alert.getEmail());
        return alertService.createAlert(alert);
    }

    @GetMapping
    public List<Alert> getAllAlerts() {
        return alertService.getAllAlerts();
    }

    @GetMapping("/{id}")
    public Alert getAlert(@PathVariable String id) {
        return alertService.getAlert(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found: " + id));
    }

    @GetMapping("/by-pair")
    public List<Alert> getAlertsByPair(@RequestParam String from, @RequestParam String to) {
        return alertService.getAlertsByCurrencyPair(from, to);
    }

    @GetMapping("/by-email")
    public List<Alert> getAlertsByEmail(@RequestParam String email) {
        return alertService.getAlertsByEmail(email);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteAlert(@PathVariable String id) {
        boolean deleted = alertService.deleteAlert(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @PatchMapping("/{id}/toggle")
    public Alert toggleAlert(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        boolean enabled = body.getOrDefault("enabled", true);
        return alertService.toggleAlert(id, enabled);
    }
}
