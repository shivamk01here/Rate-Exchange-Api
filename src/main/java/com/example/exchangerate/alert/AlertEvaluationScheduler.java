package com.example.exchangerate.alert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEvaluationScheduler {

    private final AlertEvaluationService alertEvaluationService;

    @Scheduled(fixedRateString = "${alert.evaluation.interval-ms:60000}")
    public void evaluateAlerts() {
        log.debug("Running scheduled alert evaluation");
        alertEvaluationService.evaluateAllAlerts();
    }
}
