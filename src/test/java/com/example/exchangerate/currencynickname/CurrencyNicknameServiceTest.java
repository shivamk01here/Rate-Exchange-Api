package com.example.exchangerate.currencynickname;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyNicknameServiceTest {

    private CurrencyNicknameService nicknameService;
    private CurrencyNicknameRepository nicknameRepository;

    @BeforeEach
    void setUp() {
        nicknameRepository = new CurrencyNicknameRepository();
        nicknameService = new CurrencyNicknameService(nicknameRepository);
    }

    @Test
    void createNickname_returnsSavedNicknameWithId() {
        CurrencyNickname created = nicknameService.createNickname(CurrencyNickname.builder()
                .currencyCode("USD")
                .nickname("bucks")
                .build());

        assertNotNull(created.getId());
        assertEquals("USD", created.getCurrencyCode());
        assertEquals("bucks", created.getNickname());
    }

    @Test
    void getNickname_returnsEmptyWhenNotFound() {
        assertTrue(nicknameService.getNickname("nonexistent").isEmpty());
    }

    @Test
    void getAllNicknames_returnsAllCreated() {
        nicknameService.createNickname(CurrencyNickname.builder().currencyCode("USD").nickname("bucks").build());
        nicknameService.createNickname(CurrencyNickname.builder().currencyCode("EUR").nickname("euro").build());

        List<CurrencyNickname> all = nicknameService.getAllNicknames();

        assertEquals(2, all.size());
    }

    @Test
    void getNicknamesByCode_matchesCaseInsensitively() {
        nicknameService.createNickname(CurrencyNickname.builder().currencyCode("USD").nickname("bucks").build());

        List<CurrencyNickname> matches = nicknameService.getNicknamesByCode("usd");

        assertEquals(1, matches.size());
    }

    @Test
    void getNicknamesByNickname_matchesCaseInsensitively() {
        nicknameService.createNickname(CurrencyNickname.builder().currencyCode("GBP").nickname("pounds").build());

        List<CurrencyNickname> matches = nicknameService.getNicknamesByNickname("Pounds");

        assertEquals(1, matches.size());
    }

    @Test
    void updateNickname_keepsUnsetFields() {
        CurrencyNickname saved = nicknameService.createNickname(CurrencyNickname.builder()
                .currencyCode("USD")
                .nickname("old name")
                .build());

        CurrencyNickname updated = nicknameService.updateNickname(saved.getId(),
                CurrencyNickname.builder().nickname("new name").build());

        assertEquals("new name", updated.getNickname());
        assertEquals("USD", updated.getCurrencyCode());
    }

    @Test
    void updateNickname_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> nicknameService.updateNickname("bad-id", CurrencyNickname.builder().build()));
    }

    @Test
    void deleteNickname_removesAndReturnsFalseForMissing() {
        CurrencyNickname saved = nicknameService.createNickname(CurrencyNickname.builder()
                .currencyCode("JPY")
                .nickname("yen")
                .build());

        assertTrue(nicknameService.deleteNickname(saved.getId()));
        assertTrue(nicknameService.getNickname(saved.getId()).isEmpty());
        assertFalse(nicknameService.deleteNickname("bad-id"));
    }

    @Test
    void createNickname_normalizesCodeToUppercase() {
        CurrencyNickname created = nicknameService.createNickname(CurrencyNickname.builder()
                .currencyCode("usd")
                .nickname("bucks")
                .build());

        assertEquals("USD", created.getCurrencyCode());
    }

    @Test
    void getNicknameCount_returnsCorrectCount() {
        assertEquals(0, nicknameService.getNicknameCount());

        nicknameService.createNickname(CurrencyNickname.builder().currencyCode("USD").nickname("bucks").build());

        assertEquals(1, nicknameService.getNicknameCount());
    }
}
