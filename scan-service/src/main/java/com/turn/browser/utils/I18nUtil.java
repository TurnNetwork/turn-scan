package com.turn.browser.utils;

import com.turn.browser.enums.I18nEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * International tools
 */
@Slf4j
@Component
public class I18nUtil {
    @Autowired
    private MessageSource messageSource;
    public String i(I18nEnum key, Object... param){
        /** Get the default locale */
        Locale locale = LocaleContextHolder.getLocale();
        /** Load the Chinese and English corresponding keys */
        return messageSource.getMessage(key.name().toLowerCase(),param, locale);
    }

    public String i(I18nEnum key, String localStr, Object... param){
        /** Get locale */
        Locale locale = Locale.forLanguageTag(localStr);
        /** Load the Chinese and English corresponding keys */
        return messageSource.getMessage(key.name().toLowerCase(),param, locale);
    }

    public String getMessageForStr(String key, String localStr, Object... param){
        I18nEnum keyI18 = I18nEnum.valueOf(key);
        /** Get locale */
        Locale locale = Locale.forLanguageTag(localStr);
        /** Load the Chinese and English corresponding keys */
        return messageSource.getMessage(keyI18.name().toLowerCase(),param, locale);
    }
}
