package com.example.exchangerate.services;

import com.example.exchangerate.models.CurrencyInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CurrencyService {

    private final Map<String, CurrencyInfo> currencies = new ConcurrentHashMap<>();

    public CurrencyService() {
        register("USD", "US Dollar", "$", 2, 840);
        register("EUR", "Euro", "\u20AC", 2, 978);
        register("GBP", "British Pound", "\u00A3", 2, 826);
        register("INR", "Indian Rupee", "\u20B9", 2, 356);
        register("JPY", "Japanese Yen", "\u00A5", 0, 392);
        register("CNY", "Chinese Yuan", "\u00A5", 2, 156);
        register("AUD", "Australian Dollar", "A$", 2, 36);
        register("CAD", "Canadian Dollar", "C$", 2, 124);
        register("CHF", "Swiss Franc", "CHF", 2, 756);
        register("SGD", "Singapore Dollar", "S$", 2, 702);
        register("HKD", "Hong Kong Dollar", "HK$", 2, 344);
        register("NZD", "New Zealand Dollar", "NZ$", 2, 554);
        register("KRW", "South Korean Won", "\u20A9", 0, 410);
        register("SEK", "Swedish Krona", "kr", 2, 752);
        register("NOK", "Norwegian Krone", "kr", 2, 578);
        register("MXN", "Mexican Peso", "Mex$", 2, 484);
        register("BRL", "Brazilian Real", "R$", 2, 986);
        register("ZAR", "South African Rand", "R", 2, 710);
        register("TRY", "Turkish Lira", "\u20BA", 2, 949);
        register("AED", "UAE Dirham", "Dh", 2, 784);
        log.info("Registered {} supported currencies", currencies.size());
    }

    private void register(String code, String name, String symbol, int decimalPlaces, int numericCode) {
        currencies.put(code, CurrencyInfo.builder()
                .code(code)
                .name(name)
                .symbol(symbol)
                .decimalPlaces(decimalPlaces)
                .numericCode(numericCode)
                .build());
    }

    public Collection<CurrencyInfo> getSupportedCurrencies() {
        return Collections.unmodifiableCollection(currencies.values());
    }

    public CurrencyInfo getCurrency(String code) {
        return code != null ? currencies.get(code.toUpperCase()) : null;
    }

    public boolean isSupported(String code) {
        return code != null && currencies.containsKey(code.toUpperCase());
    }
}
