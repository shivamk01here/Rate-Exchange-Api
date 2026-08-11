package com.example.exchangerate.controllers;

import com.example.exchangerate.currencygroup.CurrencyGroup;
import com.example.exchangerate.currencygroup.CurrencyGroupPair;
import com.example.exchangerate.currencygroup.CurrencyGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/currency-groups")
@RequiredArgsConstructor
public class CurrencyGroupController {

    private final CurrencyGroupService groupService;

    @PostMapping
    public CurrencyGroup createGroup(@Valid @RequestBody CurrencyGroup group) {
        if (group.getName() == null || group.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        log.info("Creating group: name={}", group.getName());
        return groupService.createGroup(group);
    }

    @GetMapping
    public List<CurrencyGroup> getAllGroups() {
        return groupService.getAllGroups();
    }

    @GetMapping("/{id}")
    public CurrencyGroup getGroup(@PathVariable String id) {
        return groupService.getGroup(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found: " + id));
    }

    @GetMapping("/by-name")
    public List<CurrencyGroup> getGroupsByName(@RequestParam String name) {
        return groupService.getGroupsByName(name);
    }

    @GetMapping("/by-pair")
    public List<CurrencyGroup> getGroupsByPair(@RequestParam String from, @RequestParam String to) {
        return groupService.getGroupsByPair(from, to);
    }

    @PutMapping("/{id}")
    public CurrencyGroup updateGroup(@PathVariable String id, @Valid @RequestBody CurrencyGroup group) {
        try {
            return groupService.updateGroup(id, group);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteGroup(@PathVariable String id) {
        boolean deleted = groupService.deleteGroup(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @PostMapping("/{id}/pairs")
    public CurrencyGroup addPair(@PathVariable String id, @Valid @RequestBody CurrencyGroupPair pair) {
        try {
            return groupService.addPair(id, pair);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}/pairs/{from}/{to}")
    public CurrencyGroup removePair(@PathVariable String id, @PathVariable String from, @PathVariable String to) {
        try {
            return groupService.removePair(id, from, to);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/count")
    public Map<String, Object> getGroupCount() {
        return Map.of("count", groupService.getGroupCount());
    }
}
