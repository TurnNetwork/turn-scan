package com.turn.browser.verify;

public interface Verifier {

    /**
     * Verification
     * @param apiParam interface parameters
     * @param secret secret key
     * @return returns the verification result, true: successful
     */
    boolean verify(ApiParam apiParam, String secret);
}

