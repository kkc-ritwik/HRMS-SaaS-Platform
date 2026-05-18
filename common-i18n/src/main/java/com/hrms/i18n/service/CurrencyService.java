package com.hrms.i18n.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Currency conversion + formatting. Prefers live rates from ExchangeRateFetcher when
 * available; falls back to static rates from HRMS_CURRENCY_RATES env var.
 */
@Service
@RequiredArgsConstructor
public class CurrencyService {

    @Value("${hrms.currency.base:INR}")
    private String baseCurrency;
    @Value("${hrms.currency.rates:}")
    private String ratesCsv;

    private final Map<String, BigDecimal> rates = new ConcurrentHashMap<>();
    private final ObjectProvider<ExchangeRateFetcher> liveFetcherProvider;

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (amount == null || fromCurrency.equalsIgnoreCase(toCurrency)) return amount;
        BigDecimal fromRate = rateFor(fromCurrency);
        BigDecimal toRate = rateFor(toCurrency);
        return amount.divide(fromRate, 10, RoundingMode.HALF_UP).multiply(toRate)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Returns the rate of {@code currency} relative to baseCurrency. Live > env > 1.0. */
    private BigDecimal rateFor(String currency) {
        String c = currency.toUpperCase();
        if (c.equals(baseCurrency.toUpperCase())) return BigDecimal.ONE;
        ExchangeRateFetcher live = liveFetcherProvider.getIfAvailable();
        if (live != null && live.liveRates.containsKey(c)) return live.liveRates.get(c);
        loadStaticRates();
        return rates.getOrDefault(c, BigDecimal.ONE);
    }

    public String format(BigDecimal amount, String currency, Locale locale) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(locale == null ? Locale.getDefault() : locale);
        nf.setCurrency(Currency.getInstance(currency));
        return nf.format(amount);
    }

    private void loadStaticRates() {
        if (!rates.isEmpty()) return;
        rates.put(baseCurrency.toUpperCase(), BigDecimal.ONE);
        if (ratesCsv == null || ratesCsv.isBlank()) return;
        for (String pair : ratesCsv.split(",")) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                try { rates.put(kv[0].trim().toUpperCase(), new BigDecimal(kv[1].trim())); }
                catch (NumberFormatException ignored) {}
            }
        }
    }
}
