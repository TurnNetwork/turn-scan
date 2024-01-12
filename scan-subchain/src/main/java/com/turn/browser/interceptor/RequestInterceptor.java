package com.turn.browser.interceptor;

import com.turn.browser.verify.ApiParam;
import com.turn.browser.verify.ApiParamParser;
import com.turn.browser.verify.DefaultMd5Verifier;
import com.turn.browser.verify.Verifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class RequestInterceptor  implements HandlerInterceptor {

    @Value("${subchain.secret}")
    private String secret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        Verifier verifier = new DefaultMd5Verifier();
        ApiParamParser apiParamParser = new ApiParamParser();
        ApiParam apiParam = apiParamParser.parse(request);
        return verifier.verify(apiParam, secret);
    }
}