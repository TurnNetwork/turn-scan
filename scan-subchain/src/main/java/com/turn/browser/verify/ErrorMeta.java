package com.turn.browser.verify;

import java.util.Locale;

public class ErrorMeta {

    private String code;
    private String msg;

    public ErrorMeta(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public String getCode() {
        return code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @param params i18n properties file parameters. The order corresponds to the placeholders {0},{1} in the file
     * @return return exception
     */
    public ApiException getException(Object... params) {
        return this.getException(params);
    }

    /**
     * Return exception with data attached
     * @param data data
     * @param params i18n properties file parameters. The order corresponds to the placeholders {0},{1} in the file
     * @return return exception
     */
    public ApiException getExceptionData(Object data, Object... params) {
        ApiException ex = this.getException(params);
        ex.setData(data);
        return ex;
    }

    public ApiException getException(Locale locale, Object... params) {
        Error<String> error = ErrorFactory.getError(this, params);
        return new ApiException(error);
    }
}
