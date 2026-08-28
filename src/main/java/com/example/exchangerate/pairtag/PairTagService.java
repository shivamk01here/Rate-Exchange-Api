package com.example.exchangerate.pairtag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PairTagService {

    private final PairTagRepository pairTagRepository;

    public PairTag createTag(PairTag tag) {
        PairTag saved = pairTagRepository.save(tag);
        log.info("Pair tag created: id={} {}->{} tag={}", saved.getId(), saved.getFromCurrency(), saved.getToCurrency(), saved.getTag());
        return saved;
    }

    public Optional<PairTag> getTag(String id) {
        return pairTagRepository.findById(id);
    }

    public List<PairTag> getAllTags() {
        return pairTagRepository.findAll();
    }

    public List<PairTag> getTagsByPair(String from, String to) {
        return pairTagRepository.findByCurrencyPair(from, to);
    }

    public List<PairTag> getTagsByTag(String tag) {
        return pairTagRepository.findByTag(tag);
    }

    public List<String> getDistinctTags() {
        return pairTagRepository.findDistinctTags();
    }

    public boolean deleteTag(String id) {
        boolean deleted = pairTagRepository.deleteById(id);
        if (deleted) {
            log.info("Pair tag deleted: id={}", id);
        }
        return deleted;
    }

    public PairTag updateTag(String id, PairTag updated) {
        return pairTagRepository.findById(id)
                .map(existing -> {
                    PairTag merged = PairTag.builder()
                            .id(existing.getId())
                            .fromCurrency(updated.getFromCurrency() != null ? updated.getFromCurrency() : existing.getFromCurrency())
                            .toCurrency(updated.getToCurrency() != null ? updated.getToCurrency() : existing.getToCurrency())
                            .tag(updated.getTag() != null ? updated.getTag() : existing.getTag())
                            .createdAt(existing.getCreatedAt())
                            .build();
                    PairTag saved = pairTagRepository.save(merged);
                    log.info("Pair tag updated: id={}", id);
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Pair tag not found: " + id));
    }

    public long getTagCount() {
        return pairTagRepository.count();
    }
}
