package com.turn.browser.controller;

import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RetEnum;
import com.turn.browser.request.staking.*;
import com.turn.browser.response.BaseResp;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.staking.*;
import com.turn.browser.service.StakingService;
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
 * Validator module Contract. Define usage
 */
@Slf4j
@RestController
public class StakingController {

    @Resource
    private I18nUtil i18n;

    @Resource
    private StakingService stakingService;

    /**
     * Aggregate data
     *
     * @param
     * @return reactor.core.publisher.Mono<com.turn.browser.response.BaseResp < com.turn.browser.response.staking.StakingStatisticNewResp>>
     */
    @SubscribeMapping("topic/staking/statistic/new")
    @PostMapping("staking/statistic")
    public Mono<BaseResp<StakingStatisticNewResp>> stakingStatisticNew() {
        return Mono.create(sink -> {
            StakingStatisticNewResp resp = stakingService.stakingStatisticNew();
            sink.success(BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS), resp));
        });
    }

    /**
     * Real-time validator list
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.staking.AliveStakingListResp>>
     */
    @PostMapping("staking/aliveStakingList")
    public Mono<RespPage<AliveStakingListResp>> aliveStakingList(@Valid @RequestBody AliveStakingListReq req) {
        return Mono.just(stakingService.aliveStakingList(req));
    }

    /**
     * Historical validator list
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.staking.HistoryStakingListResp>>
     */
    @PostMapping("staking/historyStakingList")
    public Mono<RespPage<HistoryStakingListResp>> historyStakingList(@Valid @RequestBody HistoryStakingListReq req) {
        return Mono.just(stakingService.historyStakingList(req));
    }

    /**
     * Validator details
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.BaseResp < com.turn.browser.response.staking.StakingDetailsResp>>
     */
    @PostMapping("staking/stakingDetails")
    public Mono<BaseResp<StakingDetailsResp>> stakingDetails(@Valid @RequestBody StakingDetailsReq req) {
        return Mono.just(stakingService.stakingDetails(req));
    }

    /**
     * Node operation record
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.staking.StakingOptRecordListResp>>
     */
    @PostMapping("staking/stakingOptRecordList")
    public Mono<RespPage<StakingOptRecordListResp>> stakingOptRecordList(@Valid @RequestBody StakingOptRecordListReq req) {
        return Mono.just(stakingService.stakingOptRecordList(req));
    }

    /**
     * Validator’s delegation list
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.staking.DelegationListByStakingResp>>
     */
    @PostMapping("staking/delegationListByStaking")
    public Mono<RespPage<DelegationListByStakingResp>> delegationListByStaking(@Valid @RequestBody DelegationListByStakingReq req) {
        return Mono.just(stakingService.delegationListByStaking(req));
    }

    /**
     * Address-related commission list
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.staking.DelegationListByAddressResp>>
     */
    @PostMapping("staking/delegationListByAddress")
    public Mono<RespPage<DelegationListByAddressResp>> delegationListByAddress(@Valid @RequestBody DelegationListByAddressReq req) {
        return Mono.just(stakingService.delegationListByAddress(req));
    }

    /**
     * Locked validator list
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.staking.LockedStakingListResp>>
     */
    @PostMapping("staking/lockedStakingList")
    public Mono<RespPage<LockedStakingListResp>> lockedStakingList(@Valid @RequestBody LockedStakingListReq req) {
        return Mono.just(stakingService.lockedStakingList(req));
    }

}
