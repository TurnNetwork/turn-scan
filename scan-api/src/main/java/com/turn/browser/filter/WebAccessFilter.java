package com.turn.browser.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONUtil;
import com.turn.browser.bean.CommonConstant;
import com.turn.browser.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Interface access filter
 *
 */
@Slf4j
@WebFilter(filterName = "webAccessFilter", urlPatterns = "/*")
@Order(0)
@Component
public class WebAccessFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = ((HttpServletResponse) servletResponse);
        long start = System.currentTimeMillis();
        RequestWrapper requestWrapper = new RequestWrapper(request);
        String traceId = CommonUtil.createTraceId();
        CommonUtil.putTraceId(traceId);
        requestParamLog(requestWrapper);
        requestWrapper.setAttribute(CommonConstant.TRACE_ID, traceId);
        chain.doFilter(requestWrapper, response);
        response.setHeader(CommonConstant.TRACE_ID, traceId);
        log.info("[End of request interface] http status: {}, time consumption: {}ms", CommonUtil.ofNullable(() -> response.getStatus()).orElse(-1), System.currentTimeMillis() - start);
    }

    @Override
    public void destroy() {
        CommonUtil.removeTraceId();
    }

    /**
     * Get the trace-id of the upstream service
     * Trace-id general constraint sources: request headers, attributes, request parameters (the first layer structure of json)
     *
     * @param requestWrapper
     * @return java.lang.String
     */
    private String getReqTraceId(RequestWrapper requestWrapper) {
        String traceId = "";
        try {
            traceId = CommonUtil.ofNullable(() -> requestWrapper.getHeader(CommonConstant.TRACE_ID)).orElse("");
            if (StrUtil.isBlank(traceId)) {
                traceId = CommonUtil.ofNullable(() -> requestWrapper.getAttribute(CommonConstant.TRACE_ID).toString()).orElse("");
                if (StrUtil.isBlank(traceId)) {
                    String param = StrUtil.blankToDefault(requestWrapper.getParamBody(), "");
                    if (StrUtil.isNotBlank(param) && JSONUtil.isJson(param)) {
                        if (JSONUtil.parseObj(param).containsKey(CommonConstant.REQ_TRACE_ID)) {
                            traceId = JSONUtil.parseObj(param).getStr(CommonConstant.REQ_TRACE_ID);
                        } else {
                            traceId = CommonUtil.createTraceId();
                        }
                    } else {
                        traceId = CommonUtil.createTraceId();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception in obtaining the trace-id of the upstream service, a new trace-id will be created", e);
        } finally {
            if (StrUtil.isBlank(traceId)) {
                traceId = CommonUtil.createTraceId();
            }
        }
        return traceId;
    }

    /**
     * Print request parameters are temporarily not compatible with file upload and multi-parameter printing.
     *
     * @param requestWrapper
     * @return void
     */
    private void requestParamLog(RequestWrapper requestWrapper) {
        if (!ServletUtil.isMultipart(requestWrapper)) {
            // 不是文件上传，则打印请求参数
            log.info("[Start of request interface] Path URL: {}, request method: {}, Content-Type: {}, request parameters:{}",
                     requestWrapper.getRequestURL(),
                     requestWrapper.getMethod(),
                     StrUtil.blankToDefault(requestWrapper.getContentType(), "none"),
                     StrUtil.blankToDefault(requestWrapper.getParamBody(), "none"));
        } else {
            log.info("[Start of request interface] Path URL: {}, request method: {}, Content-Type: {}, request parameters: file upload",
                     requestWrapper.getRequestURL(),
                     requestWrapper.getMethod(),
                     StrUtil.blankToDefault(requestWrapper.getContentType(), "none"));
        }
    }

}
