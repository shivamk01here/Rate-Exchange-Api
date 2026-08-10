package com.example.exchangerate.currencynickname;

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
public class CurrencyNicknameRepository {

    private final ConcurrentHashMap<String, CurrencyNickname> nicknames = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<CurrencyNickname> nicknameList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public CurrencyNickname save(CurrencyNickname nickname) {
        String id = nickname.getId() != null ? nickname.getId() : String.valueOf(idCounter.incrementAndGet());
        CurrencyNickname stored = CurrencyNickname.builder()
                .id(id)
                .currencyCode(nickname.getCurrencyCode() != null ? nickname.getCurrencyCode().toUpperCase() : null)
                .nickname(nickname.getNickname())
                .createdAt(nickname.getCreatedAt() != null ? nickname.getCreatedAt() : java.time.Instant.now())
                .build();

        if (nicknames.putIfAbsent(id, stored) == null) {
            nicknameList.add(stored);
        } else {
            nicknames.put(id, stored);
            for (int i = 0; i < nicknameList.size(); i++) {
                if (id.equals(nicknameList.get(i).getId())) {
                    nicknameList.set(i, stored);
                    break;
                }
            }
        }

        log.debug("CurrencyNickname saved: id={} {}={}", id, stored.getCurrencyCode(), stored.getNickname());
        return stored;
    }

    public Optional<CurrencyNickname> findById(String id) {
        return Optional.ofNullable(nicknames.get(id));
    }

    public List<CurrencyNickname> findAll() {
        return new ArrayList<>(nicknameList);
    }

    public List<CurrencyNickname> findByCurrencyCode(String code) {
        return nicknameList.stream()
                .filter(n -> code.equalsIgnoreCase(n.getCurrencyCode()))
                .collect(Collectors.toList());
    }

    public List<CurrencyNickname> findByNickname(String nickname) {
        return nicknameList.stream()
                .filter(n -> nickname.equalsIgnoreCase(n.getNickname()))
                .collect(Collectors.toList());
    }

    public boolean deleteById(String id) {
        CurrencyNickname removed = nicknames.remove(id);
        if (removed != null) {
            nicknameList.remove(removed);
            log.info("CurrencyNickname deleted: id={}", id);
            return true;
        }
        return false;
    }

    public long count() {
        return nicknames.size();
    }
}
