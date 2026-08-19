package com.example.exchangerate.notes;

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
public class ConversionNoteRepository {

    private final ConcurrentHashMap<String, ConversionNote> notes = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<ConversionNote> noteList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public ConversionNote save(ConversionNote note) {
        String id = note.getId() != null ? note.getId() : String.valueOf(idCounter.incrementAndGet());
        ConversionNote stored = ConversionNote.builder()
                .id(id)
                .fromCurrency(note.getFromCurrency())
                .toCurrency(note.getToCurrency())
                .noteText(note.getNoteText())
                .createdAt(note.getCreatedAt() != null ? note.getCreatedAt() : java.time.Instant.now())
                .updatedAt(note.getUpdatedAt())
                .build();

        if (notes.putIfAbsent(id, stored) == null) {
            noteList.add(stored);
        } else {
            notes.put(id, stored);
            for (int i = 0; i < noteList.size(); i++) {
                if (id.equals(noteList.get(i).getId())) {
                    noteList.set(i, stored);
                    break;
                }
            }
        }

        log.debug("ConversionNote saved: id={} {}->{}", id, stored.getFromCurrency(), stored.getToCurrency());
        return stored;
    }

    public Optional<ConversionNote> findById(String id) {
        return Optional.ofNullable(notes.get(id));
    }

    public List<ConversionNote> findAll() {
        return new ArrayList<>(noteList);
    }

    public List<ConversionNote> findByCurrencyPair(String from, String to) {
        return noteList.stream()
                .filter(n -> from.equalsIgnoreCase(n.getFromCurrency())
                        && to.equalsIgnoreCase(n.getToCurrency()))
                .collect(Collectors.toList());
    }

    public boolean deleteById(String id) {
        ConversionNote removed = notes.remove(id);
        if (removed != null) {
            noteList.remove(removed);
            log.info("ConversionNote deleted: id={}", id);
            return true;
        }
        return false;
    }

    public long count() {
        return notes.size();
    }
}
