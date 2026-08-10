package com.example.exchangerate.controllers;

import com.example.exchangerate.currencynickname.CurrencyNickname;
import com.example.exchangerate.currencynickname.CurrencyNicknameRepository;
import com.example.exchangerate.currencynickname.CurrencyNicknameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyNicknameControllerTest {

    private CurrencyNicknameController controller;

    @BeforeEach
    void setUp() {
        CurrencyNicknameRepository repository = new CurrencyNicknameRepository();
        CurrencyNicknameService service = new CurrencyNicknameService(repository);
        controller = new CurrencyNicknameController(service);
    }

    @Test
    void createNickname_returnsCreatedNickname() {
        CurrencyNickname result = controller.createNickname(CurrencyNickname.builder()
                .currencyCode("USD")
                .nickname("bucks")
                .build());

        assertNotNull(result.getId());
        assertEquals("bucks", result.getNickname());
    }

    @Test
    void createNickname_throwsWhenNicknameMissing() {
        CurrencyNickname nickname = CurrencyNickname.builder()
                .currencyCode("USD")
                .nickname(" ")
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.createNickname(nickname));
    }

    @Test
    void getAllNicknames_returnsAll() {
        controller.createNickname(CurrencyNickname.builder().currencyCode("USD").nickname("bucks").build());
        controller.createNickname(CurrencyNickname.builder().currencyCode("EUR").nickname("euro").build());

        List<CurrencyNickname> all = controller.getAllNicknames();

        assertEquals(2, all.size());
    }

    @Test
    void getNickname_returnsById() {
        CurrencyNickname created = controller.createNickname(CurrencyNickname.builder()
                .currencyCode("USD")
                .nickname("bucks")
                .build());

        CurrencyNickname result = controller.getNickname(created.getId());

        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getNickname_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getNickname("bad-id"));
    }

    @Test
    void getNicknamesByCode_filtersByCode() {
        controller.createNickname(CurrencyNickname.builder().currencyCode("USD").nickname("bucks").build());
        controller.createNickname(CurrencyNickname.builder().currencyCode("EUR").nickname("euro").build());

        List<CurrencyNickname> matches = controller.getNicknamesByCode("USD");

        assertEquals(1, matches.size());
    }

    @Test
    void getNicknamesByName_filtersByNickname() {
        controller.createNickname(CurrencyNickname.builder().currencyCode("USD").nickname("bucks").build());

        List<CurrencyNickname> matches = controller.getNicknamesByName("bucks");

        assertEquals(1, matches.size());
    }

    @Test
    void deleteNickname_returnsSuccess() {
        CurrencyNickname created = controller.createNickname(CurrencyNickname.builder()
                .currencyCode("USD")
                .nickname("bucks")
                .build());

        Map<String, String> result = controller.deleteNickname(created.getId());

        assertEquals("deleted", result.get("status"));
    }

    @Test
    void updateNickname_updatesNickname() {
        CurrencyNickname created = controller.createNickname(CurrencyNickname.builder()
                .currencyCode("USD")
                .nickname("old")
                .build());

        CurrencyNickname updated = controller.updateNickname(created.getId(),
                CurrencyNickname.builder().nickname("new").build());

        assertEquals("new", updated.getNickname());
    }

    @Test
    void getNicknameCount_returnsCount() {
        controller.createNickname(CurrencyNickname.builder().currencyCode("USD").nickname("bucks").build());

        Map<String, Object> result = controller.getNicknameCount();

        assertEquals(1L, result.get("count"));
    }
}
