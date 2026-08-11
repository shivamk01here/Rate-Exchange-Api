package com.example.exchangerate.currencygroup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyGroupService {

    private final CurrencyGroupRepository groupRepository;

    public CurrencyGroup createGroup(CurrencyGroup group) {
        CurrencyGroup saved = groupRepository.save(group);
        log.info("Group created: id={} name={} pairs={}",
                saved.getId(), saved.getName(), saved.getPairs().size());
        return saved;
    }

    public Optional<CurrencyGroup> getGroup(String id) {
        return groupRepository.findById(id);
    }

    public List<CurrencyGroup> getAllGroups() {
        return groupRepository.findAll();
    }

    public List<CurrencyGroup> getGroupsByName(String name) {
        return groupRepository.findByName(name);
    }

    public List<CurrencyGroup> getGroupsByPair(String from, String to) {
        return groupRepository.findByPair(from, to);
    }

    public CurrencyGroup updateGroup(String id, CurrencyGroup updated) {
        return groupRepository.findById(id)
                .map(existing -> {
                    CurrencyGroup merged = CurrencyGroup.builder()
                            .id(existing.getId())
                            .name(updated.getName() != null ? updated.getName() : existing.getName())
                            .description(updated.getDescription() != null ? updated.getDescription() : existing.getDescription())
                            .pairs(updated.getPairs() != null ? updated.getPairs() : existing.getPairs())
                            .createdAt(existing.getCreatedAt())
                            .build();
                    CurrencyGroup saved = groupRepository.save(merged);
                    log.info("Group updated: id={} name={}", id, saved.getName());
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));
    }

    public boolean deleteGroup(String id) {
        boolean deleted = groupRepository.deleteById(id);
        if (deleted) {
            log.info("Group deleted: id={}", id);
        }
        return deleted;
    }

    public long getGroupCount() {
        return groupRepository.count();
    }

    public CurrencyGroup addPair(String id, CurrencyGroupPair pair) {
        return groupRepository.findById(id)
                .map(existing -> {
                    List<CurrencyGroupPair> pairs = new ArrayList<>(existing.getPairs());
                    boolean duplicate = pairs.stream()
                            .anyMatch(p -> p.getFromCurrency().equalsIgnoreCase(pair.getFromCurrency())
                                    && p.getToCurrency().equalsIgnoreCase(pair.getToCurrency()));
                    if (duplicate) {
                        throw new IllegalArgumentException("Pair already in group: " + id);
                    }
                    pairs.add(pair);
                    CurrencyGroup merged = CurrencyGroup.builder()
                            .id(existing.getId())
                            .name(existing.getName())
                            .description(existing.getDescription())
                            .pairs(pairs)
                            .createdAt(existing.getCreatedAt())
                            .build();
                    CurrencyGroup saved = groupRepository.save(merged);
                    log.info("Pair added to group: id={} {}->{}", id, pair.getFromCurrency(), pair.getToCurrency());
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));
    }

    public CurrencyGroup removePair(String id, String from, String to) {
        return groupRepository.findById(id)
                .map(existing -> {
                    List<CurrencyGroupPair> pairs = new ArrayList<>(existing.getPairs());
                    boolean removed = pairs.removeIf(p -> p.getFromCurrency().equalsIgnoreCase(from)
                            && p.getToCurrency().equalsIgnoreCase(to));
                    if (!removed) {
                        throw new IllegalArgumentException("Pair not in group: " + id);
                    }
                    CurrencyGroup merged = CurrencyGroup.builder()
                            .id(existing.getId())
                            .name(existing.getName())
                            .description(existing.getDescription())
                            .pairs(pairs)
                            .createdAt(existing.getCreatedAt())
                            .build();
                    CurrencyGroup saved = groupRepository.save(merged);
                    log.info("Pair removed from group: id={} {}->{}", id, from, to);
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));
    }
}
