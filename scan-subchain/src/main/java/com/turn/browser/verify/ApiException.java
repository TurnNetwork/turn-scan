package com.turn.browser.verify;

public class ApiException extends RuntimeException {
    private static final long serialVersionUID = 16789476595630713L;
    private static final String ERROR_CODE = Errors.SYS_ERROR.getCode();

    private String code = ERROR_CODE;
    private Object data;

    public ApiException(String msg) {
        super(msg);
    }

    public ApiException(Exception e) {
        super(e);
    }

    public ApiException(Error<String> error) {
        this(error.getMsg());
        this.code = error.getCode();
    }

    public ApiException(String msg, String code) {
        super(msg);
        this.code = code;
    }

    public ApiException(String msg, String code, Object data) {
        super(msg);
        this.code = code;
        this.data = data;
    }

    public ApiException(Error<String> error, Object data) {
        this(error);
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public RuntimeException getRuntimeException() {
        return new RuntimeException(this.getMessage());
    }

}

