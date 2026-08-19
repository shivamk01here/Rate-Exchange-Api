package com.example.exchangerate.notes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionNoteService {

    private final ConversionNoteRepository noteRepository;

    public ConversionNote createNote(ConversionNote note) {
        ConversionNote saved = noteRepository.save(note);
        log.info("Note created: id={} {}->{}", saved.getId(), saved.getFromCurrency(), saved.getToCurrency());
        return saved;
    }

    public Optional<ConversionNote> getNote(String id) {
        return noteRepository.findById(id);
    }

    public List<ConversionNote> getAllNotes() {
        return noteRepository.findAll();
    }

    public List<ConversionNote> getNotesByPair(String from, String to) {
        return noteRepository.findByCurrencyPair(from, to);
    }

    public boolean deleteNote(String id) {
        boolean deleted = noteRepository.deleteById(id);
        if (deleted) {
            log.info("Note deleted: id={}", id);
        }
        return deleted;
    }

    public ConversionNote updateNote(String id, ConversionNote updated) {
        return noteRepository.findById(id)
                .map(existing -> {
                    ConversionNote merged = ConversionNote.builder()
                            .id(existing.getId())
                            .fromCurrency(updated.getFromCurrency() != null ? updated.getFromCurrency() : existing.getFromCurrency())
                            .toCurrency(updated.getToCurrency() != null ? updated.getToCurrency() : existing.getToCurrency())
                            .noteText(updated.getNoteText() != null ? updated.getNoteText() : existing.getNoteText())
                            .createdAt(existing.getCreatedAt())
                            .updatedAt(Instant.now())
                            .build();
                    ConversionNote saved = noteRepository.save(merged);
                    log.info("Note updated: id={}", id);
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Note not found: " + id));
    }

    public long getNoteCount() {
        return noteRepository.count();
    }
}
