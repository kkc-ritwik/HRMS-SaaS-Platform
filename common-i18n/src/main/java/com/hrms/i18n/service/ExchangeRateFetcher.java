package com.hrms.i18n.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Periodically fetches live FX rates from a free public source (ECB / exchangerate.host) and
 * pushes them into CurrencyService's in-memory map. Gracefully degrades to the static rates
 * configured via env if the provider is unreachable.
 *
 * For paid providers (OpenExchangeRates, Fixer, CurrencyLayer) set
 * hrms.currency.fetch-url and the JSON-path conventions can be adapted in parseRates().
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateFetcher {

    @Value("${hrms.currency.fetch-enabled:true}") private boolean enabled;
    @Value("${hrms.currency.base:INR}") private String base;
    @Value("${hrms.currency.fetch-url:https://api.exchangerate.host/latest?base={base}}")
    private String urlTemplate;

    /** Public in-memory rate cache — same object referenced by CurrencyService. */
    public final Map<String, BigDecimal> liveRates = new ConcurrentHashMap<>();

    private final RestTemplate rest = new RestTemplate();

    @Scheduled(cron = "${hrms.currency.fetch-cron:0 0 */6 * * *}")   // every 6h
    public void refresh() {
        if (!enabled) return;
        try {
            String url = urlTemplate.replace("{base}", base);
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = rest.getForObject(url, Map.class);
            if (resp == null) return;
            Object rates = resp.get("rates");
            if (rates instanceof Map<?,?> m) {
                m.forEach((k, v) -> {
                    try { liveRates.put(k.toString().toUpperCase(), new BigDecimal(v.toString())); }
                    catch (NumberFormatException ignored) {}
                });
                log.info("Refreshed {} FX rates (base={})", liveRates.size(), base);
            }
        } catch (Exception e) {
            log.warn("FX refresh failed (will use env-configured fallbacks): {}", e.getMessage());
        }
    }
}
