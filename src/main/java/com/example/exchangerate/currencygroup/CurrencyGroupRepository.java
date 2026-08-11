package com.example.exchangerate.currencygroup;

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
public class CurrencyGroupRepository {

    private final ConcurrentHashMap<String, CurrencyGroup> groups = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<CurrencyGroup> groupList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public CurrencyGroup save(CurrencyGroup group) {
        String id = group.getId() != null ? group.getId() : String.valueOf(idCounter.incrementAndGet());
        CurrencyGroup stored = CurrencyGroup.builder()
                .id(id)
                .name(group.getName())
                .description(group.getDescription())
                .pairs(group.getPairs() != null ? new ArrayList<>(group.getPairs()) : new ArrayList<>())
                .createdAt(group.getCreatedAt() != null ? group.getCreatedAt() : java.time.Instant.now())
                .build();

        if (groups.putIfAbsent(id, stored) == null) {
            groupList.add(stored);
        } else {
            groups.put(id, stored);
            for (int i = 0; i < groupList.size(); i++) {
                if (id.equals(groupList.get(i).getId())) {
                    groupList.set(i, stored);
                    break;
                }
            }
        }

        log.debug("CurrencyGroup saved: id={} name={} pairs={}", id, stored.getName(), stored.getPairs().size());
        return stored;
    }

    public Optional<CurrencyGroup> findById(String id) {
        return Optional.ofNullable(groups.get(id));
    }

    public List<CurrencyGroup> findAll() {
        return new ArrayList<>(groupList);
    }

    public List<CurrencyGroup> findByName(String name) {
        return groupList.stream()
                .filter(g -> name.equalsIgnoreCase(g.getName()))
                .collect(Collectors.toList());
    }

    public List<CurrencyGroup> findByPair(String from, String to) {
        return groupList.stream()
                .filter(g -> g.getPairs().stream()
                        .anyMatch(p -> from.equalsIgnoreCase(p.getFromCurrency())
                                && to.equalsIgnoreCase(p.getToCurrency())))
                .collect(Collectors.toList());
    }

    public boolean deleteById(String id) {
        CurrencyGroup removed = groups.remove(id);
        if (removed != null) {
            groupList.remove(removed);
            log.info("CurrencyGroup deleted: id={}", id);
            return true;
        }
        return false;
    }

    public long count() {
        return groups.size();
    }
}
