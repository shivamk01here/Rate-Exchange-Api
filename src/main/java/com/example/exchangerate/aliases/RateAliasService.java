package com.example.exchangerate.aliases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateAliasService {

    private final RateAliasRepository aliasRepository;

    public RateAlias createAlias(RateAlias alias) {
        RateAlias saved = aliasRepository.save(alias);
        log.info("Alias created: id={} alias={} {}->{}", saved.getId(), saved.getAlias(),
                saved.getFromCurrency(), saved.getToCurrency());
        return saved;
    }

    public Optional<RateAlias> getAlias(String id) {
        return aliasRepository.findById(id);
    }

    public Optional<RateAlias> lookupByAlias(String alias) {
        return aliasRepository.findByAlias(alias);
    }

    public List<RateAlias> getAllAliases() {
        return aliasRepository.findAll();
    }

    public boolean deleteAlias(String id) {
        boolean deleted = aliasRepository.deleteById(id);
        if (deleted) {
            log.info("Alias deleted: id={}", id);
        }
        return deleted;
    }

    public long getAliasCount() {
        return aliasRepository.count();
    }
}
