package com.example.exchangerate.services;

import com.example.exchangerate.audit.AuditRepository;
import com.example.exchangerate.config.AuditConfig;
import com.example.exchangerate.models.ConversionRecord;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.HistoryPageRequest;
import com.example.exchangerate.models.HistoryPageResponse;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.models.RateCompareResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;
    private final AuditConfig auditConfig;

    public List<ConversionRecord> recordConversion(RateCompareResponse response) {
        log.info("Auditing rate comparison: {}->{} with {} provider results",
                response.getFromCurrency(), response.getToCurrency(), response.getProviderRates().size());

        List<ConversionRecord> records = response.getProviderRates().stream()
                .map(detail -> {
                    ConversionRecord record = ConversionRecord.builder()
                            .providerCode(detail.getProviderCode())
                            .fromCurrency(response.getFromCurrency())
                            .toCurrency(response.getToCurrency())
                            .amount(java.math.BigDecimal.ONE)
                            .rate(detail.getRate())
                            .convertedAmount(detail.getRate())
                            .status(detail.getStatus())
                            .timestamp(Instant.now())
                            .build();
                    return auditRepository.save(record);
                })
                .collect(Collectors.toList());

        if (response.getBestProvider() != null) {
            log.info("Best rate for {}->{}: {} from {}",
                    response.getFromCurrency(), response.getToCurrency(),
                    response.getBestRate(), response.getBestProvider());
        }

        return records;
    }

    public ConversionRecord recordConversion(ExchangeRateResponse response) {
        ConversionRecord record = ConversionRecord.builder()
                .providerCode(response.getProviderCode())
                .fromCurrency(response.getFromCurrency())
                .toCurrency(response.getToCurrency())
                .amount(response.getAmount())
                .rate(response.getRate())
                .convertedAmount(response.getConvertedAmount())
                .status(response.getStatus())
                .timestamp(Instant.now())
                .build();

        return auditRepository.save(record);
    }

    public List<ConversionRecord> getHistory(int limit) {
        return auditRepository.findRecent(limit);
    }

    public List<ConversionRecord> getHistoryByPair(String from, String to) {
        return auditRepository.findByCurrencyPair(from, to);
    }

    public List<ConversionRecord> getHistoryByProvider(ProviderCodes code) {
        return auditRepository.findByProvider(code);
    }

    public List<ConversionRecord> getHistoryByTimeRange(Instant from, Instant to) {
        return auditRepository.findByTimeRange(from, to);
    }

    public long getTotalConversions() {
        return auditRepository.count();
    }

    public long getSuccessCount() {
        return auditRepository.countByStatus("SUCCESS");
    }

    public long getFailureCount() {
        return auditRepository.countByStatusStartingWith("FAILED");
    }

    public Map<String, Long> getPopularPairs(int topN) {
        return auditRepository.getPopularPairs(topN);
    }

    public HistoryPageResponse getHistoryPage(HistoryPageRequest request) {
        return auditRepository.findPage(request);
    }

    public int cleanupOldRecords(java.time.Duration maxAge) {
        Instant cutoff = Instant.now().minus(maxAge);
        return auditRepository.removeOlderThan(cutoff);
    }
}
