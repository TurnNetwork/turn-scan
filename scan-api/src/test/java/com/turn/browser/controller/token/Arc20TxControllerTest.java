package com.turn.browser.controller.token;

import com.alibaba.fastjson.JSONObject;
import com.turn.browser.ApiTestBase;
import com.turn.browser.request.token.QueryTokenTransferRecordListReq;
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
public class Arc20TxControllerTest extends ApiTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build(); //初始化MockMvc对象
    }

    @Test
    public void tokenTxList() throws Exception {
        QueryTokenTransferRecordListReq req = new QueryTokenTransferRecordListReq();
        req.setPageNo(1);
        req.setPageSize(10);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/token/arc20-tx/list")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content((JSONObject.toJSONString(req)).getBytes()))
                .andExpect(status().isOk()).andDo(print());
    }

    @Test
    public void tokenTxExport() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/token/arc20-tx/export")
                        .param("address", "atp1cy2uat0eukfrxv897s5s8lnljfka5ewjj943gf")
                        .param("contract", "")
                        .param("token", "")
                        .param("date", "1571813653000")
                        .param("local", "en")
                        .param("timeZone", "+8")
                        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andDo(result -> {
                    result.getResponse().setCharacterEncoding("UTF-8");
                    MockHttpServletResponse contentRespon = result.getResponse();
                    InputStream in = new ByteArrayInputStream(contentRespon.getContentAsByteArray());
                    FileOutputStream fos = new FileOutputStream(new File("build/bbb.csv"));
                    byte[] byteBuf = new byte[1024];
                    while (in.read(byteBuf) != -1) {
                        fos.write(byteBuf);
                    }
                    fos.close();
                });
    }


}