package com.example.exchangerate.controllers;

import com.example.exchangerate.currencynickname.CurrencyNickname;
import com.example.exchangerate.currencynickname.CurrencyNicknameService;
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
@RequestMapping("/api/currency-nicknames")
@RequiredArgsConstructor
public class CurrencyNicknameController {

    private final CurrencyNicknameService nicknameService;

    @PostMapping
    public CurrencyNickname createNickname(@Valid @RequestBody CurrencyNickname nickname) {
        if (nickname.getNickname() == null || nickname.getNickname().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nickname is required");
        }
        log.info("Creating nickname: {}={}", nickname.getCurrencyCode(), nickname.getNickname());
        return nicknameService.createNickname(nickname);
    }

    @GetMapping
    public List<CurrencyNickname> getAllNicknames() {
        return nicknameService.getAllNicknames();
    }

    @GetMapping("/{id}")
    public CurrencyNickname getNickname(@PathVariable String id) {
        return nicknameService.getNickname(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nickname not found: " + id));
    }

    @GetMapping("/by-code")
    public List<CurrencyNickname> getNicknamesByCode(@RequestParam String code) {
        return nicknameService.getNicknamesByCode(code);
    }

    @GetMapping("/by-nickname")
    public List<CurrencyNickname> getNicknamesByName(@RequestParam String nickname) {
        return nicknameService.getNicknamesByNickname(nickname);
    }

    @PutMapping("/{id}")
    public CurrencyNickname updateNickname(@PathVariable String id, @Valid @RequestBody CurrencyNickname nickname) {
        try {
            return nicknameService.updateNickname(id, nickname);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteNickname(@PathVariable String id) {
        boolean deleted = nicknameService.deleteNickname(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nickname not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @GetMapping("/count")
    public Map<String, Object> getNicknameCount() {
        return Map.of("count", nicknameService.getNicknameCount());
    }
}
