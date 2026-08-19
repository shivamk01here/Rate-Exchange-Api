package com.example.exchangerate.notes;

import com.example.exchangerate.controllers.ConversionNoteController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConversionNoteControllerTest {

    private ConversionNoteController controller;

    @BeforeEach
    void setUp() {
        ConversionNoteRepository repository = new ConversionNoteRepository();
        ConversionNoteService service = new ConversionNoteService(repository);
        controller = new ConversionNoteController(service);
    }

    @Test
    void createNote_returnsCreatedNote() {
        ConversionNote note = ConversionNote.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .noteText("Check this rate")
                .build();

        ConversionNote result = controller.createNote(note);

        assertNotNull(result.getId());
        assertEquals("Check this rate", result.getNoteText());
    }

    @Test
    void createNote_throwsWhenNoteTextBlank() {
        ConversionNote note = ConversionNote.builder()
                .fromCurrency("USD").toCurrency("EUR").noteText("").build();

        assertThrows(ResponseStatusException.class, () -> controller.createNote(note));
    }

    @Test
    void getAllNotes_returnsAll() {
        controller.createNote(ConversionNote.builder()
                .fromCurrency("USD").toCurrency("EUR").noteText("First").build());
        controller.createNote(ConversionNote.builder()
                .fromCurrency("GBP").toCurrency("JPY").noteText("Second").build());

        List<ConversionNote> all = controller.getAllNotes();

        assertEquals(2, all.size());
    }

    @Test
    void getNote_returnsById() {
        ConversionNote created = controller.createNote(ConversionNote.builder()
                .fromCurrency("USD").toCurrency("CAD").noteText("Find me").build());

        ConversionNote result = controller.getNote(created.getId());

        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getNote_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getNote("bad-id"));
    }

    @Test
    void deleteNote_returnsSuccess() {
        ConversionNote created = controller.createNote(ConversionNote.builder()
                .fromCurrency("USD").toCurrency("GBP").noteText("Delete").build());

        Map<String, String> result = controller.deleteNote(created.getId());

        assertEquals("deleted", result.get("status"));
    }

    @Test
    void deleteNote_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.deleteNote("nope"));
    }

    @Test
    void updateNote_updatesText() {
        ConversionNote created = controller.createNote(ConversionNote.builder()
                .fromCurrency("USD").toCurrency("INR").noteText("Old").build());

        ConversionNote updated = controller.updateNote(created.getId(),
                ConversionNote.builder().noteText("New").build());

        assertEquals("New", updated.getNoteText());
    }

    @Test
    void getNoteCount_returnsCount() {
        controller.createNote(ConversionNote.builder()
                .fromCurrency("USD").toCurrency("EUR").noteText("One").build());

        Map<String, Object> result = controller.getNoteCount();

        assertEquals(1L, result.get("count"));
    }
}
