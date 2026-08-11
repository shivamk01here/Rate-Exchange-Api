package com.example.exchangerate.currencygroup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyGroupServiceTest {

    private CurrencyGroupService groupService;
    private CurrencyGroupRepository groupRepository;

    @BeforeEach
    void setUp() {
        groupRepository = new CurrencyGroupRepository();
        groupService = new CurrencyGroupService(groupRepository);
    }

    @Test
    void createGroup_returnsSavedGroupWithId() {
        CurrencyGroup created = groupService.createGroup(CurrencyGroup.builder()
                .name("Travel")
                .description("currencies for the trip")
                .build());

        assertNotNull(created.getId());
        assertEquals("Travel", created.getName());
        assertTrue(created.getPairs().isEmpty());
    }

    @Test
    void getGroup_returnsEmptyWhenNotFound() {
        assertTrue(groupService.getGroup("nonexistent").isEmpty());
    }

    @Test
    void getAllGroups_returnsAllCreated() {
        groupService.createGroup(CurrencyGroup.builder().name("Travel").build());
        groupService.createGroup(CurrencyGroup.builder().name("Work").build());

        List<CurrencyGroup> all = groupService.getAllGroups();

        assertEquals(2, all.size());
    }

    @Test
    void getGroupsByName_matchesCaseInsensitively() {
        groupService.createGroup(CurrencyGroup.builder().name("Travel").build());

        List<CurrencyGroup> matches = groupService.getGroupsByName("travel");

        assertEquals(1, matches.size());
    }

    @Test
    void getGroupsByPair_returnsGroupsContainingPair() {
        CurrencyGroup group = groupService.createGroup(CurrencyGroup.builder()
                .name("Travel")
                .pairs(List.of(CurrencyGroupPair.builder().fromCurrency("USD").toCurrency("INR").build()))
                .build());

        List<CurrencyGroup> matches = groupService.getGroupsByPair("usd", "inr");

        assertEquals(1, matches.size());
        assertEquals(group.getId(), matches.get(0).getId());
    }

    @Test
    void updateGroup_keepsUnsetFields() {
        CurrencyGroup saved = groupService.createGroup(CurrencyGroup.builder()
                .name("Old Name")
                .description("old desc")
                .build());

        CurrencyGroup updated = groupService.updateGroup(saved.getId(),
                CurrencyGroup.builder().name("New Name").build());

        assertEquals("New Name", updated.getName());
        assertEquals("old desc", updated.getDescription());
    }

    @Test
    void updateGroup_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> groupService.updateGroup("bad-id", CurrencyGroup.builder().build()));
    }

    @Test
    void deleteGroup_removesAndReturnsFalseForMissing() {
        CurrencyGroup saved = groupService.createGroup(CurrencyGroup.builder().name("Temp").build());

        assertTrue(groupService.deleteGroup(saved.getId()));
        assertTrue(groupService.getGroup(saved.getId()).isEmpty());
        assertFalse(groupService.deleteGroup("bad-id"));
    }

    @Test
    void addPair_addsPairToGroup() {
        CurrencyGroup saved = groupService.createGroup(CurrencyGroup.builder().name("Travel").build());

        CurrencyGroup updated = groupService.addPair(saved.getId(),
                CurrencyGroupPair.builder().fromCurrency("USD").toCurrency("EUR").build());

        assertEquals(1, updated.getPairs().size());
        assertEquals("USD", updated.getPairs().get(0).getFromCurrency());
    }

    @Test
    void addPair_rejectsDuplicatePair() {
        CurrencyGroup saved = groupService.createGroup(CurrencyGroup.builder().name("Travel").build());
        groupService.addPair(saved.getId(),
                CurrencyGroupPair.builder().fromCurrency("USD").toCurrency("EUR").build());

        assertThrows(IllegalArgumentException.class, () -> groupService.addPair(saved.getId(),
                CurrencyGroupPair.builder().fromCurrency("usd").toCurrency("EUR").build()));
    }

    @Test
    void removePair_removesPairFromGroup() {
        CurrencyGroup saved = groupService.createGroup(CurrencyGroup.builder().name("Travel").build());
        groupService.addPair(saved.getId(),
                CurrencyGroupPair.builder().fromCurrency("USD").toCurrency("EUR").build());

        CurrencyGroup updated = groupService.removePair(saved.getId(), "USD", "EUR");

        assertTrue(updated.getPairs().isEmpty());
    }

    @Test
    void removePair_throwsWhenPairNotInGroup() {
        CurrencyGroup saved = groupService.createGroup(CurrencyGroup.builder().name("Travel").build());

        assertThrows(IllegalArgumentException.class,
                () -> groupService.removePair(saved.getId(), "USD", "EUR"));
    }

    @Test
    void getGroupCount_returnsCorrectCount() {
        assertEquals(0, groupService.getGroupCount());

        groupService.createGroup(CurrencyGroup.builder().name("Travel").build());

        assertEquals(1, groupService.getGroupCount());
    }
}
