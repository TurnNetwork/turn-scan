package com.turn.browser.verify;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.turn.browser.utils.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Parameter parsing default implementation
 */
public class ApiParamParser implements ParamParser {

    private static final Logger logger = LoggerFactory.getLogger(ApiParamParser.class);

    private static final String CONTENT_TYPE_MULTIPART = MediaType.MULTIPART_FORM_DATA_VALUE;
    private static final String CONTENT_TYPE_JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String CONTENT_TYPE_TEXT = MediaType.TEXT_PLAIN_VALUE;

    public static final String UPLOAD_FORM_DATA_NAME = "body_data";

    private static String REQUEST_DATA_NAME = "data";


    @Override
    public ApiParam parse(HttpServletRequest request) {
        String requestJson = null;
        try {
            requestJson = this.getJson(request);
        } catch (Exception e) {
            logger.error("parse error", e);
        }

        if (StringUtils.isEmpty(requestJson)) {
            throw Errors.ERROR_PARAM.getException();
        }

        ApiParam param = this.jsonToApiParam(requestJson);
        this.bindRestParam(param, request);
        return param;
    }

    public String getJson(HttpServletRequest request) throws Exception {
        String requestJson = null;

        if (RequestUtil.isGetRequest(request)) {
            Map<String, Object> params = RequestUtil.convertRequestParamsToMap(request);
            requestJson = JSON.toJSONString(params);
        } else {
            String contectType = request.getContentType();

            if (contectType == null) {
                contectType = "";
            }

            contectType = contectType.toLowerCase();

            // json or plain text form
            if (contectType.contains(CONTENT_TYPE_JSON) || contectType.contains(CONTENT_TYPE_TEXT)) {
                requestJson = RequestUtil.getText(request);
            }else {
                Map<String, Object> params = RequestUtil.convertRequestParamsToMap(request);
                requestJson = JSON.toJSONString(params);
            }
        }

        return requestJson;
    }

    protected ApiParam jsonToApiParam(String json) {
        if (StringUtils.isEmpty(json)) {
            throw Errors.ERROR_PARAM.getException();
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(json);
        } catch (Exception e) {
            throw Errors.ERROR_JSON_DATA.getException(e.getMessage());
        }

        return new ApiParam(jsonObject);
    }

    protected void bindRestParam(ApiParam param, HttpServletRequest request) {
        String version = (String)request.getAttribute(Consts.REST_PARAM_VERSION);
        if(version != null) {
            param.setVersion(version);
        }
    }
}

