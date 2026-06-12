package com.example.exchangerate.config;

import com.example.exchangerate.services.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditCleanupScheduler {

    private final AuditService auditService;
    private final AuditConfig auditConfig;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldRecords() {
        int removed = auditService.cleanupOldRecords(auditConfig.getRetention());
        log.info("Scheduled cleanup removed {} old audit records", removed);
    }
}
