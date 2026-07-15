package com.example.exchangerate.portfolio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class PortfolioMetricsCollector {

    private final AtomicLong createCount = new AtomicLong(0);
    private final AtomicLong deleteCount = new AtomicLong(0);
    private final AtomicLong updateCount = new AtomicLong(0);
    private final AtomicLong valuationCount = new AtomicLong(0);
    private final AtomicLong holdingAddCount = new AtomicLong(0);
    private final AtomicLong holdingRemoveCount = new AtomicLong(0);

    public void recordCreate() {
        createCount.incrementAndGet();
    }

    public void recordDelete() {
        deleteCount.incrementAndGet();
    }

    public void recordUpdate() {
        updateCount.incrementAndGet();
    }

    public void recordValuation() {
        valuationCount.incrementAndGet();
    }

    public void recordHoldingAdd() {
        holdingAddCount.incrementAndGet();
    }

    public void recordHoldingRemove() {
        holdingRemoveCount.incrementAndGet();
    }

    public Map<String, Object> getStats() {
        return Map.of(
                "creates", createCount.get(),
                "deletes", deleteCount.get(),
                "updates", updateCount.get(),
                "valuations", valuationCount.get(),
                "holdingAdds", holdingAddCount.get(),
                "holdingRemoves", holdingRemoveCount.get()
        );
    }

    public void reset() {
        createCount.set(0);
        deleteCount.set(0);
        updateCount.set(0);
        valuationCount.set(0);
        holdingAddCount.set(0);
        holdingRemoveCount.set(0);
        log.info("Portfolio metrics reset");
    }
}
