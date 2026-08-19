package com.example.exchangerate.notes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConversionNoteServiceTest {

    private ConversionNoteService noteService;
    private ConversionNoteRepository noteRepository;

    @BeforeEach
    void setUp() {
        noteRepository = new ConversionNoteRepository();
        noteService = new ConversionNoteService(noteRepository);
    }

    @Test
    void createNote_returnsSavedNoteWithId() {
        ConversionNote note = ConversionNote.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .noteText("Checking rate before trip")
                .build();

        ConversionNote saved = noteService.createNote(note);

        assertNotNull(saved.getId());
        assertEquals("USD", saved.getFromCurrency());
        assertEquals("INR", saved.getToCurrency());
    }

    @Test
    void getNote_returnsNoteWhenExists() {
        ConversionNote saved = noteService.createNote(ConversionNote.builder()
                .fromCurrency("EUR").toCurrency("GBP")
                .noteText("Weekend check").build());

        ConversionNote found = noteService.getNote(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void getNote_returnsEmptyWhenNotFound() {
        assertTrue(noteService.getNote("nonexistent").isEmpty());
    }

    @Test
    void getAllNotes_returnsAllCreatedNotes() {
        noteService.createNote(ConversionNote.builder()
                .fromCurrency("USD").toCurrency("EUR").noteText("Note 1").build());
        noteService.createNote(ConversionNote.builder()
                .fromCurrency("GBP").toCurrency("JPY").noteText("Note 2").build());

        List<ConversionNote> all = noteService.getAllNotes();

        assertEquals(2, all.size());
    }

    @Test
    void getNotesByPair_filtersCorrectly() {
        noteService.createNote(ConversionNote.builder()
                .fromCurrency("USD").toCurrency("INR").noteText("First").build());
        noteService.createNote(ConversionNote.builder()
                .fromCurrency("USD").toCurrency("EUR").noteText("Second").build());
        noteService.createNote(ConversionNote.builder()
                .fromCurrency("USD").toCurrency("INR").noteText("Third").build());

        List<ConversionNote> result = noteService.getNotesByPair("USD", "INR");

        assertEquals(2, result.size());
    }

    @Test
    void deleteNote_removesNote() {
        ConversionNote saved = noteService.createNote(ConversionNote.builder()
                .fromCurrency("USD").toCurrency("CAD").noteText("Delete me").build());

        assertTrue(noteService.deleteNote(saved.getId()));
        assertTrue(noteService.getNote(saved.getId()).isEmpty());
    }

    @Test
    void deleteNote_returnsFalseForNonexistent() {
        assertFalse(noteService.deleteNote("nonexistent"));
    }

    @Test
    void updateNote_updatesFields() {
        ConversionNote saved = noteService.createNote(ConversionNote.builder()
                .fromCurrency("USD").toCurrency("INR").noteText("Old text").build());

        ConversionNote updated = noteService.updateNote(saved.getId(),
                ConversionNote.builder().noteText("New text").build());

        assertEquals("New text", updated.getNoteText());
        assertEquals("USD", updated.getFromCurrency());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void updateNote_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> noteService.updateNote("bad-id", ConversionNote.builder().noteText("x").build()));
    }

    @Test
    void getNoteCount_returnsCorrectCount() {
        assertEquals(0, noteService.getNoteCount());

        noteService.createNote(ConversionNote.builder()
                .fromCurrency("USD").toCurrency("EUR").noteText("Count").build());

        assertEquals(1, noteService.getNoteCount());
    }
}
