package com.turn.browser.verify;

import javax.servlet.http.HttpServletRequest;

/**
 * Responsible for parsing parameters
 *
 */
public interface ParamParser {

    /**
     * Extract parameters from request
     * @param request
     * @return Return ApiParam
     * @throws Exception
     */
    ApiParam parse(HttpServletRequest request);
}

