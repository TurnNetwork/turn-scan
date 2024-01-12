package com.turn.browser.verify;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * The parameters passed by the client are placed here.
 *
 */
public class ApiParam extends JSONObject implements Param {
    private static final long serialVersionUID = 6718200590738465201L;

    public static volatile String SIGN_NAME = "sign";

    public ApiParam(Map<String, Object> map) {
        super(map);
    }

    private boolean ignoreSign;
    private boolean ignoreValidate;

    private String restName;
    private String restVersion;

    /**
     * Get sign and delete it from param
     * @return Return sign content
     */
    public String fatchSignAndRemove() {
        String sign = this.fatchSign();
        this.remove(SIGN_NAME);
        return sign;
    }

    /**
     * Parameters, after urlencode
     */
    @Override
    public String fatchData() {
        return getString(ParamNames.DATA_NAME);
    }

    /**
     * Whether to ignore verification signature
     * @return returns true, ignore signature
     */
    public boolean fatchIgnoreSign() {
        return ignoreSign;
    }

    public void setIgnoreSign(boolean ignoreSign) {
        this.ignoreSign = ignoreSign;
    }

    public boolean fatchIgnoreValidate() {
        return ignoreValidate;
    }

    public void setIgnoreValidate(boolean ignoreValidate) {
        this.ignoreValidate = ignoreValidate;
    }


    /**
     * Timestamp in the format of yyyy-MM-dd HH:mm:ss, for example: 2015-01-01 12:00:00
     */
    @Override
    public String fatchTimestamp() {
        return getString(ParamNames.TIMESTAMP_NAME);
    }

    public void setTimestamp(String timestamp) {
        put(ParamNames.TIMESTAMP_NAME, timestamp);
    }

    /**
     * signature string
     */
    @Override
    public String fatchSign() {
        return getString(ParamNames.SIGN_NAME);
    }

    public void setSign(String sign) {
        put(ParamNames.SIGN_NAME, sign);
    }

    @Override
    public String fatchFormat() {
        String format = getString(ParamNames.FORMAT_NAME);
        if (format == null || "".equals(format)) {
            return Consts.FORMAT_JSON;
        }
        return format;
    }

    /**
     * version
     */
    @Override
    public String fatchVersion() {
        String version = getString(ParamNames.VERSION_NAME);
        if(version == null) {
            version = this.restVersion;
        }
        return version;
    }

    public void setVersion(String version) {
        this.restVersion = version;
    }

    public void setFormat(String format) {
        put(ParamNames.FORMAT_NAME, format);
    }

    @Override
    public ApiParam clone() {
        ApiParam param = new ApiParam(this);
        param.ignoreSign = this.ignoreSign;
        param.ignoreValidate = this.ignoreValidate;
        return param;
    }


}

