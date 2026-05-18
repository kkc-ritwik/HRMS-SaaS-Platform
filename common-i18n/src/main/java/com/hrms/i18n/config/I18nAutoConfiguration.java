package com.hrms.i18n.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
@ComponentScan(basePackages = "com.hrms.i18n")
@EnableScheduling
public class I18nAutoConfiguration {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource s = new ReloadableResourceBundleMessageSource();
        s.setBasenames("classpath:messages/messages", "classpath:messages/payroll", "classpath:messages/email");
        s.setDefaultEncoding("UTF-8");
        s.setFallbackToSystemLocale(false);
        s.setCacheSeconds(60);
        return s;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver r = new AcceptHeaderLocaleResolver();
        r.setDefaultLocale(Locale.ENGLISH);
        r.setSupportedLocales(List.of(
                Locale.ENGLISH, new Locale("hi"), new Locale("es"), Locale.FRENCH,
                Locale.GERMAN, new Locale("pt"), new Locale("ar"), Locale.JAPANESE,
                Locale.CHINESE, new Locale("ru"), new Locale("ta"), new Locale("te"),
                new Locale("mr"), new Locale("bn"), new Locale("gu")));
        return r;
    }
}
