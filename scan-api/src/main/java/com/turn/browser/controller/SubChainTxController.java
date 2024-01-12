package com.turn.browser.controller;


import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RetEnum;
import com.turn.browser.request.newtransaction.TransactionDetailsReq;
import com.turn.browser.request.subchain.SubChainRecordListReq;
import com.turn.browser.request.subchain.SubChainTransactionDetailsReq;
import com.turn.browser.response.BaseResp;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.subchain.SubChainTxDetailsResp;
import com.turn.browser.response.subchain.SubChainTxRecordListResp;
import com.turn.browser.response.transaction.TransactionDetailsResp;
import com.turn.browser.service.SubChainTxService;
import com.turn.browser.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 微节点控制器
 */
@Slf4j
@RestController
public class SubChainTxController {

    @Resource
    private I18nUtil i18n;

    @Resource
    private SubChainTxService subChainTxService;

    /**
     * 子链交易列表
     * @param req
     * @return
     */
    @PostMapping("subchain/subChainTxRecordList")
    public Mono<RespPage<SubChainTxRecordListResp>> subChainTxRecordList(@Valid @RequestBody SubChainRecordListReq req) {
        return Mono.just(subChainTxService.subChainTxRecordList(req));
    }

    /**
     * 交易详情
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.BaseResp < com.turn.browser.response.transaction.TransactionDetailsResp>>
     * @date 2021/5/25
     */
    @PostMapping("subchain/subChainTxDetails")
    public Mono<BaseResp<SubChainTxDetailsResp>> subChainTxDetails(@Valid @RequestBody SubChainTransactionDetailsReq req) {
        return Mono.create(sink -> {
            SubChainTxDetailsResp resp = subChainTxService.subChainTxDetails(req);
            sink.success(BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS), resp));
        });
    }

}
