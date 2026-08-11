package com.example.exchangerate.controllers;

import com.example.exchangerate.currencygroup.CurrencyGroup;
import com.example.exchangerate.currencygroup.CurrencyGroupPair;
import com.example.exchangerate.currencygroup.CurrencyGroupRepository;
import com.example.exchangerate.currencygroup.CurrencyGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyGroupControllerTest {

    private CurrencyGroupController controller;

    @BeforeEach
    void setUp() {
        CurrencyGroupRepository repository = new CurrencyGroupRepository();
        CurrencyGroupService service = new CurrencyGroupService(repository);
        controller = new CurrencyGroupController(service);
    }

    @Test
    void createGroup_returnsCreatedGroup() {
        CurrencyGroup result = controller.createGroup(CurrencyGroup.builder()
                .name("Travel")
                .build());

        assertNotNull(result.getId());
        assertEquals("Travel", result.getName());
    }

    @Test
    void createGroup_throwsWhenNameMissing() {
        CurrencyGroup group = CurrencyGroup.builder()
                .name(" ")
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.createGroup(group));
    }

    @Test
    void getAllGroups_returnsAll() {
        controller.createGroup(CurrencyGroup.builder().name("Travel").build());
        controller.createGroup(CurrencyGroup.builder().name("Work").build());

        List<CurrencyGroup> all = controller.getAllGroups();

        assertEquals(2, all.size());
    }

    @Test
    void getGroup_returnsById() {
        CurrencyGroup created = controller.createGroup(CurrencyGroup.builder().name("Travel").build());

        CurrencyGroup result = controller.getGroup(created.getId());

        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getGroup_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getGroup("bad-id"));
    }

    @Test
    void getGroupsByName_filtersByName() {
        controller.createGroup(CurrencyGroup.builder().name("Travel").build());
        controller.createGroup(CurrencyGroup.builder().name("Work").build());

        List<CurrencyGroup> matches = controller.getGroupsByName("Travel");

        assertEquals(1, matches.size());
    }

    @Test
    void getGroupsByPair_filtersByPair() {
        CurrencyGroup group = controller.createGroup(CurrencyGroup.builder()
                .name("Travel")
                .pairs(List.of(CurrencyGroupPair.builder().fromCurrency("USD").toCurrency("INR").build()))
                .build());

        List<CurrencyGroup> matches = controller.getGroupsByPair("USD", "INR");

        assertEquals(1, matches.size());
        assertEquals(group.getId(), matches.get(0).getId());
    }

    @Test
    void deleteGroup_returnsSuccess() {
        CurrencyGroup created = controller.createGroup(CurrencyGroup.builder().name("Temp").build());

        Map<String, String> result = controller.deleteGroup(created.getId());

        assertEquals("deleted", result.get("status"));
    }

    @Test
    void updateGroup_updatesName() {
        CurrencyGroup created = controller.createGroup(CurrencyGroup.builder().name("Old").build());

        CurrencyGroup updated = controller.updateGroup(created.getId(),
                CurrencyGroup.builder().name("New").build());

        assertEquals("New", updated.getName());
    }

    @Test
    void addPair_addsPairToGroup() {
        CurrencyGroup created = controller.createGroup(CurrencyGroup.builder().name("Travel").build());

        CurrencyGroup updated = controller.addPair(created.getId(),
                CurrencyGroupPair.builder().fromCurrency("USD").toCurrency("EUR").build());

        assertEquals(1, updated.getPairs().size());
    }

    @Test
    void removePair_removesPairFromGroup() {
        CurrencyGroup created = controller.createGroup(CurrencyGroup.builder().name("Travel").build());
        controller.addPair(created.getId(),
                CurrencyGroupPair.builder().fromCurrency("USD").toCurrency("EUR").build());

        CurrencyGroup updated = controller.removePair(created.getId(), "USD", "EUR");

        assertTrue(updated.getPairs().isEmpty());
    }

    @Test
    void getGroupCount_returnsCount() {
        controller.createGroup(CurrencyGroup.builder().name("Travel").build());

        Map<String, Object> result = controller.getGroupCount();

        assertEquals(1L, result.get("count"));
    }
}
