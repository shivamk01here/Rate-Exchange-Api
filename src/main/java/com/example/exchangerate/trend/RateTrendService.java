package com.example.exchangerate.trend;

import com.example.exchangerate.models.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateTrendService {

    private final RateTrendRepository rateTrendRepository;
    private final RateTrendConfig trendConfig;
    private final RateTrendMetricsCollector metricsCollector;

    public RateSnapshot recordRate(ExchangeRateResponse response) {
        RateSnapshot snapshot = RateSnapshot.builder()
                .fromCurrency(response.getFromCurrency())
                .toCurrency(response.getToCurrency())
                .rate(response.getRate())
                .providerCode(response.getProviderCode() != null ? response.getProviderCode().name() : "UNKNOWN")
                .build();

        RateSnapshot saved = rateTrendRepository.save(snapshot);
        metricsCollector.recordRateCapture();

        RateTrend trend = computeTrendPoint(response.getFromCurrency(), response.getToCurrency(), snapshot);
        if (trend != null) {
            metricsCollector.recordDirection(trend.getDirection());
            log.debug("Trend for {}->{}: {} ({})",
                    response.getFromCurrency(), response.getToCurrency(),
                    trend.getDirection(), trend.getPercentChange());
        }

        return saved;
    }

    public List<RateSnapshot> getSnapshots(String from, String to) {
        metricsCollector.recordQuery();
        return rateTrendRepository.findByCurrencyPair(from, to);
    }

    public Optional<RateSnapshot> getLatestSnapshot(String from, String to) {
        return rateTrendRepository.findLatestByCurrencyPair(from, to);
    }

    public List<RateTrend> getRecentTrends(String from, String to, int limit) {
        List<RateSnapshot> recent = rateTrendRepository.findRecentByCurrencyPair(from, to, limit + 1);
        List<RateTrend> trends = new ArrayList<>();

        for (int i = 0; i < recent.size() - 1; i++) {
            RateSnapshot current = recent.get(i);
            RateSnapshot previous = recent.get(i + 1);
            trends.add(computeTrendBetween(current, previous));
        }

        return trends;
    }

    public TrendSummary getTrendSummary(String from, String to) {
        metricsCollector.recordSummary();
        List<RateSnapshot> allSnapshots = rateTrendRepository.findByCurrencyPair(from, to);

        if (allSnapshots.isEmpty()) {
            return TrendSummary.builder()
                    .fromCurrency(from)
                    .toCurrency(to)
                    .totalSnapshots(0)
                    .overallDirection(RateTrend.TrendDirection.STABLE)
                    .overallPercentChange(BigDecimal.ZERO)
                    .recentTrends(List.of())
                    .build();
        }

        List<RateSnapshot> sorted = allSnapshots.stream()
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .collect(Collectors.toList());

        RateSnapshot oldest = sorted.get(0);
        RateSnapshot latest = sorted.get(sorted.size() - 1);

        BigDecimal highest = allSnapshots.stream()
                .map(RateSnapshot::getRate)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal lowest = allSnapshots.stream()
                .map(RateSnapshot::getRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal average = allSnapshots.stream()
                .map(RateSnapshot::getRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(allSnapshots.size()), 6, RoundingMode.HALF_UP);

        BigDecimal percentChange = BigDecimal.ZERO;
        if (oldest.getRate().compareTo(BigDecimal.ZERO) != 0) {
            percentChange = latest.getRate()
                    .subtract(oldest.getRate())
                    .divide(oldest.getRate(), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        RateTrend.TrendDirection direction = classifyDirection(percentChange);

        List<RateTrend> recentTrends = getRecentTrends(from, to, trendConfig.getDisplayLimit());

        return TrendSummary.builder()
                .fromCurrency(from)
                .toCurrency(to)
                .totalSnapshots(allSnapshots.size())
                .latestRate(latest.getRate())
                .oldestRate(oldest.getRate())
                .highestRate(highest)
                .lowestRate(lowest)
                .averageRate(average)
                .overallPercentChange(percentChange)
                .overallDirection(direction)
                .recentTrends(recentTrends)
                .build();
    }

    public MapStats getStats() {
        return new MapStats(
                rateTrendRepository.count(),
                rateTrendRepository.getPairCounts()
        );
    }

    public void clearAll() {
        rateTrendRepository.clear();
        log.info("All rate trend data cleared");
    }

    public void clearByPair(String from, String to) {
        rateTrendRepository.deleteByCurrencyPair(from, to);
        log.info("Rate trend data cleared for {}->{}", from, to);
    }

    private RateTrend computeTrendPoint(String from, String to, RateSnapshot current) {
        Optional<RateSnapshot> previousOpt = rateTrendRepository.findRecentByCurrencyPair(from, to, 2)
                .stream()
                .filter(s -> s.getTimestamp().isBefore(current.getTimestamp()))
                .findFirst();

        if (previousOpt.isEmpty()) {
            return RateTrend.builder()
                    .fromCurrency(from)
                    .toCurrency(to)
                    .rate(current.getRate())
                    .providerCode(current.getProviderCode())
                    .direction(RateTrend.TrendDirection.STABLE)
                    .percentChange(BigDecimal.ZERO)
                    .recordedAt(current.getTimestamp())
                    .build();
        }

        return computeTrendBetween(current, previousOpt.get());
    }

    private RateTrend computeTrendBetween(RateSnapshot current, RateSnapshot previous) {
        BigDecimal percentChange = BigDecimal.ZERO;
        if (previous.getRate().compareTo(BigDecimal.ZERO) != 0) {
            percentChange = current.getRate()
                    .subtract(previous.getRate())
                    .divide(previous.getRate(), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        RateTrend.TrendDirection direction = classifyDirection(percentChange);

        return RateTrend.builder()
                .fromCurrency(current.getFromCurrency())
                .toCurrency(current.getToCurrency())
                .rate(current.getRate())
                .providerCode(current.getProviderCode())
                .direction(direction)
                .percentChange(percentChange)
                .recordedAt(current.getTimestamp())
                .build();
    }

    private RateTrend.TrendDirection classifyDirection(BigDecimal percentChange) {
        BigDecimal threshold = BigDecimal.valueOf(trendConfig.getStabilityThresholdPercent());
        if (percentChange.compareTo(threshold) > 0) {
            return RateTrend.TrendDirection.RISING;
        } else if (percentChange.compareTo(threshold.negate()) < 0) {
            return RateTrend.TrendDirection.FALLING;
        }
        return RateTrend.TrendDirection.STABLE;
    }

    public record MapStats(long totalSnapshots, Map<String, Long> pairCounts) {}
}
