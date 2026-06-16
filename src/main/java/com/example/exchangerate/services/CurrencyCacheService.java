package com.example.exchangerate.services;

import com.example.exchangerate.models.CurrencyInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CurrencyCacheService {

    private static final long CACHE_TTL_SECONDS = 300;

    private final CurrencyService currencyService;

    private Collection<CurrencyInfo> cachedList;
    private Instant listExpiresAt;

    private final ConcurrentHashMap<String, CachedCurrency> currencyCache = new ConcurrentHashMap<>();

    public CurrencyCacheService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public Collection<CurrencyInfo> getSupportedCurrencies() {
        if (cachedList == null || Instant.now().isAfter(listExpiresAt)) {
            cachedList = currencyService.getSupportedCurrencies();
            listExpiresAt = Instant.now().plusSeconds(CACHE_TTL_SECONDS);
            log.debug("Refreshed currency list cache");
        }
        return cachedList;
    }

    public CurrencyInfo getCurrency(String code) {
        String key = code != null ? code.toUpperCase() : "";
        CachedCurrency cached = currencyCache.get(key);
        if (cached != null && !Instant.now().isAfter(cached.expiresAt)) {
            return cached.currency;
        }
        CurrencyInfo currency = currencyService.getCurrency(code);
        if (currency != null) {
            currencyCache.put(key, new CachedCurrency(currency, Instant.now().plusSeconds(CACHE_TTL_SECONDS)));
        }
        return currency;
    }

    public boolean isSupported(String code) {
        return getCurrency(code) != null;
    }

    public void clearCache() {
        cachedList = null;
        listExpiresAt = null;
        currencyCache.clear();
        log.info("Currency cache cleared");
    }

    private record CachedCurrency(CurrencyInfo currency, Instant expiresAt) {}
}
