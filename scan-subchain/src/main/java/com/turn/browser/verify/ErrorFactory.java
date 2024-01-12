package com.turn.browser.verify;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Responsible for building error messages
 *
 */
public class ErrorFactory {

    protected static Logger logger = LoggerFactory.getLogger(ErrorFactory.class);

    private static final String I18N_OPEN_ERROR = "i18n/open/error";

    private static Set<String> noModuleCache = new HashSet<>();

    /** Internationalization information of error message */
    private static MessageSourceAccessor errorMessageSourceAccessor;

    /**
     * Set international resource information
     */
    public static void initMessageSource(List<String> isvModules) {
        HashSet<String> baseNamesSet = new HashSet<String>();
        baseNamesSet.add(I18N_OPEN_ERROR);

        if(!isvModules.isEmpty()) {
            baseNamesSet.addAll(isvModules);
        }

        String[] totalBaseNames = baseNamesSet.toArray(new String[0]);

        if (logger.isInfoEnabled()) {
            logger.info("Loading error code international resources：{}", StringUtils.arrayToCommaDelimitedString(totalBaseNames));
        }
        ResourceBundleMessageSource bundleMessageSource = new ResourceBundleMessageSource();
        bundleMessageSource.setBasenames(totalBaseNames);
        MessageSourceAccessor messageSourceAccessor = new MessageSourceAccessor(bundleMessageSource);
        setErrorMessageSourceAccessor(messageSourceAccessor);
    }

    /**
     * Build internationalized error messages through ErrorMeta, params
     * @param errorMeta error message
     * @param params parameters
     * @return If no internationalization message is configured, the information in errorMeta will be returned directly.
     */
    public static Error<String> getError(ErrorMeta errorMeta, Object... params) {
        final String code = errorMeta.getCode();
        String errorMessage = errorMeta.getMsg();
        final String errorMsg = errorMessage;
        return new Error<String>() {
            @Override
            public String getMsg() {
                return errorMsg;
            }

            @Override
            public String getCode() {
                return code;
            }
        };
    }


    public static void setErrorMessageSourceAccessor(MessageSourceAccessor errorMessageSourceAccessor) {
        ErrorFactory.errorMessageSourceAccessor = errorMessageSourceAccessor;
    }

    /**
     * Return localization information
     * @param module error module
     * @param locale localization
     * @param params parameters
     * @return return information
     */
    public static String getErrorMessage(String module, Locale locale, Object... params) {
        if (noModuleCache.contains(module)) {
            return null;
        }
        try {
            return errorMessageSourceAccessor.getMessage(module, params, locale);
        } catch (Exception e) {
            noModuleCache.add(module);
            return null;
        }
    }

}

