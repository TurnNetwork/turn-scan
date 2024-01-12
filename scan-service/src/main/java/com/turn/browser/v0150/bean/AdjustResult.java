package com.turn.browser.v0150.bean;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Adjustment results
 */
@Data
public class AdjustResult {
    // whether succeed
    private boolean success=true;
    // Error message <adjustment parameters, error message (string)>
    private Map<AdjustParam, String> errors = new HashMap<>();

    /**
     * Check if there are any errors
     */
    public boolean validate(){
        for (Map.Entry<AdjustParam, String> entry : errors.entrySet()) {
            String v = entry.getValue();
            if (v!=null) {
                success = false;
                break;
            }
        }
        return success;
    }
}