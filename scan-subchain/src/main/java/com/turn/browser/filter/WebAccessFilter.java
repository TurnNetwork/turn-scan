package com.turn.browser.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
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
     * Print request parameters are temporarily not compatible with file upload and multi-parameter printing.
     *
     */
    private void requestParamLog(RequestWrapper requestWrapper) {
        if (!ServletUtil.isMultipart(requestWrapper)) {
            // If it is not a file upload, print the request parameters.
            log.info("[Start of request interface] Path URL: {}, Request method: {}, Content-Type: {}, Request parameters: {}",
                    requestWrapper.getRequestURL(),
                    requestWrapper.getMethod(),
                    StrUtil.blankToDefault(requestWrapper.getContentType(), "None"),
                    StrUtil.blankToDefault(requestWrapper.getParamBody(), "None"));
        } else {
            log.info("[Start of request interface] Path URL: {}, Request method: {}, Content-Type: {}, Request parameters: File upload",
                    requestWrapper.getRequestURL(),
                    requestWrapper.getMethod(),
                    StrUtil.blankToDefault(requestWrapper.getContentType(), "None"));
        }
    }

}
