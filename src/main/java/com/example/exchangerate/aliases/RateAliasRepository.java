package com.example.exchangerate.aliases;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RateAliasRepository {

    private final ConcurrentHashMap<String, RateAlias> byId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateAlias> byAlias = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<RateAlias> aliasList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public RateAlias save(RateAlias alias) {
        String id = alias.getId() != null ? alias.getId() : String.valueOf(idCounter.incrementAndGet());
        RateAlias stored = RateAlias.builder()
                .id(id)
                .alias(alias.getAlias().toLowerCase())
                .fromCurrency(alias.getFromCurrency())
                .toCurrency(alias.getToCurrency())
                .createdAt(alias.getCreatedAt() != null ? alias.getCreatedAt() : java.time.Instant.now())
                .build();

        RateAlias existing = byId.put(id, stored);
        if (existing != null) {
            byAlias.remove(existing.getAlias());
        }

        byAlias.put(stored.getAlias(), stored);

        if (existing == null) {
            aliasList.add(stored);
        } else {
            for (int i = 0; i < aliasList.size(); i++) {
                if (id.equals(aliasList.get(i).getId())) {
                    aliasList.set(i, stored);
                    break;
                }
            }
        }

        log.debug("RateAlias saved: id={} alias={} {}->{}", id, stored.getAlias(), stored.getFromCurrency(), stored.getToCurrency());
        return stored;
    }

    public Optional<RateAlias> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Optional<RateAlias> findByAlias(String alias) {
        return Optional.ofNullable(byAlias.get(alias.toLowerCase()));
    }

    public List<RateAlias> findAll() {
        return new ArrayList<>(aliasList);
    }

    public boolean deleteById(String id) {
        RateAlias removed = byId.remove(id);
        if (removed != null) {
            byAlias.remove(removed.getAlias());
            aliasList.remove(removed);
            log.info("RateAlias deleted: id={}", id);
            return true;
        }
        return false;
    }

    public long count() {
        return byId.size();
    }
}
