package com.turn.browser.controller;

import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RetEnum;
import com.turn.browser.request.SubChainTxListReq;
import com.turn.browser.request.SubChainTxReq;
import com.turn.browser.response.BaseResp;
import com.turn.browser.service.SubChainTransactionService;
import com.turn.browser.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
public class SubChainTransactionController {


    @Resource
    private SubChainTransactionService subChainTransactionService;

    @Resource
    private I18nUtil i18n;

    @PostMapping("/submitSubChainTransaction")
    public BaseResp<Boolean> submitSubChainTransaction(@RequestBody SubChainTxListReq req){
        return BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS), subChainTransactionService.submitSubChainTransaction(req));

    }
}
