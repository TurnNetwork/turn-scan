package com.turn.browser.controller.token;

import com.alibaba.fastjson.JSONObject;
import com.turn.browser.ApiTestBase;
import com.turn.browser.request.token.QueryTokenDetailReq;
import com.turn.browser.request.token.QueryTokenIdDetailReq;
import com.turn.browser.request.token.QueryTokenIdListReq;
import com.turn.browser.request.token.QueryTokenListReq;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class TokenControllerTest extends ApiTestBase {
    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build(); //初始化MockMvc对象
    }

    @Test
    public void tokenList() throws Exception {
        QueryTokenListReq req = new QueryTokenListReq();
        req.setPageNo(1);
        req.setPageSize(10);
        req.setType("erc20");
        this.mockMvc.perform(MockMvcRequestBuilders.post("/token/list")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content((JSONObject.toJSONString(req)).getBytes()))
                .andExpect(status().isOk()).andDo(print());
    }

    @Test
    public void tokenDetail() throws Exception {
        QueryTokenDetailReq req = new QueryTokenDetailReq();
        req.setAddress("atp1x903k8rxp7kpd7h7v5mg0dr3lms00tamvll2n2");
        this.mockMvc.perform(MockMvcRequestBuilders.post("/token/detail")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content((JSONObject.toJSONString(req)).getBytes()))
                .andExpect(status().isOk()).andDo(print());
    }


}