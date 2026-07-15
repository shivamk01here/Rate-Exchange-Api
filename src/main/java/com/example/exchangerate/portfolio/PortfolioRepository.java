package com.example.exchangerate.portfolio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class PortfolioRepository {

    private final ConcurrentHashMap<String, CurrencyPortfolio> portfolios = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<CurrencyPortfolio> portfolioList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public CurrencyPortfolio save(CurrencyPortfolio portfolio) {
        String id = portfolio.getId() != null ? portfolio.getId() : String.valueOf(idCounter.incrementAndGet());
        CurrencyPortfolio stored = CurrencyPortfolio.builder()
                .id(id)
                .name(portfolio.getName())
                .baseCurrency(portfolio.getBaseCurrency())
                .holdings(portfolio.getHoldings() != null ? new LinkedHashMap<>(portfolio.getHoldings()) : new LinkedHashMap<>())
                .createdAt(portfolio.getCreatedAt() != null ? portfolio.getCreatedAt() : java.time.Instant.now())
                .build();

        if (portfolios.putIfAbsent(id, stored) == null) {
            portfolioList.add(stored);
        } else {
            portfolios.put(id, stored);
            int index = -1;
            for (int i = 0; i < portfolioList.size(); i++) {
                if (id.equals(portfolioList.get(i).getId())) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                portfolioList.set(index, stored);
            }
        }

        log.debug("Portfolio saved: id={} name={}", id, stored.getName());
        return stored;
    }

    public Optional<CurrencyPortfolio> findById(String id) {
        return Optional.ofNullable(portfolios.get(id));
    }

    public List<CurrencyPortfolio> findAll() {
        return new ArrayList<>(portfolioList);
    }

    public boolean deleteById(String id) {
        CurrencyPortfolio removed = portfolios.remove(id);
        if (removed != null) {
            portfolioList.remove(removed);
            log.info("Portfolio deleted: id={}", id);
            return true;
        }
        return false;
    }

    public long count() {
        return portfolios.size();
    }
}
