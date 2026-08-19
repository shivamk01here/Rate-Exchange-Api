package com.example.exchangerate.controllers;

import com.example.exchangerate.notes.ConversionNote;
import com.example.exchangerate.notes.ConversionNoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class ConversionNoteController {

    private final ConversionNoteService noteService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ConversionNote createNote(@Valid @RequestBody ConversionNote note) {
        if (note.getNoteText() == null || note.getNoteText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note text is required");
        }
        log.info("Creating note: {}->{}", note.getFromCurrency(), note.getToCurrency());
        return noteService.createNote(note);
    }

    @GetMapping
    public List<ConversionNote> getAllNotes() {
        return noteService.getAllNotes();
    }

    @GetMapping("/{id}")
    public ConversionNote getNote(@PathVariable String id) {
        return noteService.getNote(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found: " + id));
    }

    @GetMapping("/by-pair")
    public List<ConversionNote> getNotesByPair(@RequestParam String from, @RequestParam String to) {
        return noteService.getNotesByPair(from, to);
    }

    @PutMapping("/{id}")
    public ConversionNote updateNote(@PathVariable String id, @Valid @RequestBody ConversionNote note) {
        try {
            return noteService.updateNote(id, note);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteNote(@PathVariable String id) {
        boolean deleted = noteService.deleteNote(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @GetMapping("/count")
    public Map<String, Object> getNoteCount() {
        return Map.of("count", noteService.getNoteCount());
    }
}
