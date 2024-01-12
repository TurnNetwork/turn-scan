package com.turn.browser.controller;

import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RetEnum;
import com.turn.browser.request.micronode.AliveMicroNodeListReq;
import com.turn.browser.request.micronode.MicroNodeDetailsReq;
import com.turn.browser.request.micronode.MicroNodeOptRecordListReq;
import com.turn.browser.response.BaseResp;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.microNode.AliveMicroNodeListResp;
import com.turn.browser.response.microNode.MicroNodeDetailsResp;
import com.turn.browser.response.microNode.MicroNodeOptRecordListResp;
import com.turn.browser.response.microNode.MicroNodeStatisticResp;
import com.turn.browser.service.MicroNodeService;
import com.turn.browser.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * micronode controller
 */
@Slf4j
@RestController
public class MicroNodeController {

    @Resource
    private I18nUtil i18n;

    @Resource
    private MicroNodeService microNodeService;

    /**
     * Aggregate data
     *
     * @param
     * @return reactor.core.publisher.Mono<com.turn.browser.response.BaseResp < com.turn.browser.response.staking.StakingStatisticNewResp>>
     */
    @SubscribeMapping("topic/micronode/statistic/new")
    @PostMapping("micronode/statistic")
    public Mono<BaseResp<MicroNodeStatisticResp>> microNodeStatisticNew() {
        return Mono.create(sink -> {
            MicroNodeStatisticResp resp = microNodeService.stakingStatistic();
            sink.success(BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS), resp));
        });
    }

    /**
     * Real-time micronode list
     */
    @PostMapping("micronode/aliveNodeList")
    public Mono<RespPage<AliveMicroNodeListResp>> aliveNodeList(@Valid @RequestBody AliveMicroNodeListReq req) {
        return Mono.just(microNodeService.aliveMicroNodeList(req));
    }

    /**
     * Micronode details
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.BaseResp < com.turn.browser.response.staking.MicroNodeDetailsResp>>
     */
    @PostMapping("micronode/microNodeDetails")
    public Mono<BaseResp<MicroNodeDetailsResp>> microNodeDetails(@Valid @RequestBody MicroNodeDetailsReq req) {
        return Mono.just(microNodeService.microNodeDetails(req));
    }

    /**
     * Micronode operation records
     * @param req
     * @return
     */
    @PostMapping("micronode/microNodeOptRecordList")
    public Mono<RespPage<MicroNodeOptRecordListResp>> microNodeOptRecordList(@Valid @RequestBody MicroNodeOptRecordListReq req) {
        return Mono.just(microNodeService.microNodeOptRecordList(req));
    }

}
