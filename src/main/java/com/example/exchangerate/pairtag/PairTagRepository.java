package com.example.exchangerate.pairtag;

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
public class PairTagRepository {

    private final ConcurrentHashMap<String, PairTag> tags = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<PairTag> tagList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public PairTag save(PairTag tag) {
        String id = tag.getId() != null ? tag.getId() : String.valueOf(idCounter.incrementAndGet());
        PairTag stored = PairTag.builder()
                .id(id)
                .fromCurrency(tag.getFromCurrency())
                .toCurrency(tag.getToCurrency())
                .tag(tag.getTag())
                .createdAt(tag.getCreatedAt() != null ? tag.getCreatedAt() : java.time.Instant.now())
                .build();

        if (tags.putIfAbsent(id, stored) == null) {
            tagList.add(stored);
        } else {
            tags.put(id, stored);
            for (int i = 0; i < tagList.size(); i++) {
                if (id.equals(tagList.get(i).getId())) {
                    tagList.set(i, stored);
                    break;
                }
            }
        }

        log.debug("PairTag saved: id={} {}->{} tag={}", id, stored.getFromCurrency(), stored.getToCurrency(), stored.getTag());
        return stored;
    }

    public Optional<PairTag> findById(String id) {
        return Optional.ofNullable(tags.get(id));
    }

    public List<PairTag> findAll() {
        return new ArrayList<>(tagList);
    }

    public List<PairTag> findByCurrencyPair(String from, String to) {
        return tagList.stream()
                .filter(t -> from.equalsIgnoreCase(t.getFromCurrency())
                        && to.equalsIgnoreCase(t.getToCurrency()))
                .collect(Collectors.toList());
    }

    public List<PairTag> findByTag(String tag) {
        return tagList.stream()
                .filter(t -> tag != null && tag.equalsIgnoreCase(t.getTag()))
                .collect(Collectors.toList());
    }

    public List<String> findDistinctTags() {
        return tagList.stream()
                .map(PairTag::getTag)
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean deleteById(String id) {
        PairTag removed = tags.remove(id);
        if (removed != null) {
            tagList.remove(removed);
            log.info("PairTag deleted: id={}", id);
            return true;
        }
        return false;
    }

    public long count() {
        return tags.size();
    }
}
