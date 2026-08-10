package com.example.exchangerate.currencynickname;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyNicknameService {

    private final CurrencyNicknameRepository nicknameRepository;

    public CurrencyNickname createNickname(CurrencyNickname nickname) {
        CurrencyNickname saved = nicknameRepository.save(nickname);
        log.info("Nickname created: id={} {}={}", saved.getId(), saved.getCurrencyCode(), saved.getNickname());
        return saved;
    }

    public Optional<CurrencyNickname> getNickname(String id) {
        return nicknameRepository.findById(id);
    }

    public List<CurrencyNickname> getAllNicknames() {
        return nicknameRepository.findAll();
    }

    public List<CurrencyNickname> getNicknamesByCode(String code) {
        return nicknameRepository.findByCurrencyCode(code);
    }

    public List<CurrencyNickname> getNicknamesByNickname(String nickname) {
        return nicknameRepository.findByNickname(nickname);
    }

    public CurrencyNickname updateNickname(String id, CurrencyNickname updated) {
        return nicknameRepository.findById(id)
                .map(existing -> {
                    CurrencyNickname merged = CurrencyNickname.builder()
                            .id(existing.getId())
                            .currencyCode(updated.getCurrencyCode() != null ? updated.getCurrencyCode() : existing.getCurrencyCode())
                            .nickname(updated.getNickname() != null ? updated.getNickname() : existing.getNickname())
                            .createdAt(existing.getCreatedAt())
                            .build();
                    CurrencyNickname saved = nicknameRepository.save(merged);
                    log.info("Nickname updated: id={} {}={}", id, saved.getCurrencyCode(), saved.getNickname());
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Nickname not found: " + id));
    }

    public boolean deleteNickname(String id) {
        boolean deleted = nicknameRepository.deleteById(id);
        if (deleted) {
            log.info("Nickname deleted: id={}", id);
        }
        return deleted;
    }

    public long getNicknameCount() {
        return nicknameRepository.count();
    }
}
