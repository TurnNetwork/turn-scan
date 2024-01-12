package com.turn.browser.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Get the body of the request to prevent body loss
 *
 * @date 2021/4/28
 */
@Slf4j
public class RequestWrapper extends HttpServletRequestWrapper {

    private byte[] body;

    private Map<String, String> paramMap;

    public RequestWrapper(HttpServletRequest request) {
        super(request);
        paramMap = ServletUtil.getParamMap(request);
        body = ServletUtil.getBodyBytes(request);
    }

    /**
     * Get the body of the request to prevent body loss
     *
     * @return
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        ServletInputStream servletInputStream = new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }

            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
        return servletInputStream;
    }


    /**
     * Override getReader method
     *
     * @return
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * Get request parameters
     *
     * @param
     * @return java.lang.String
     */
    public String getParamBody() {
        try {
            if (ServletUtil.isGetMethod(this)) {
                return JSONUtil.toJsonStr(paramMap);
            } else {
                return StrUtil.str(body, Charset.defaultCharset());
            }
        } catch (Exception e) {
            log.error("Exception in getting request parameters", e);
            return "";
        }
    }

}
