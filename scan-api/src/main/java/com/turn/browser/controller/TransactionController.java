package com.turn.browser.controller;

import com.turn.browser.config.CommonMethod;
import com.turn.browser.config.DownFileCommon;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RetEnum;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.request.PageReq;
import com.turn.browser.request.newtransaction.TransactionDetailsReq;
import com.turn.browser.request.newtransaction.TransactionListByAddressRequest;
import com.turn.browser.request.newtransaction.TransactionListByBlockRequest;
import com.turn.browser.request.staking.QueryClaimByStakingReq;
import com.turn.browser.response.BaseResp;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.account.AccountDownload;
import com.turn.browser.response.staking.QueryClaimByStakingResp;
import com.turn.browser.response.transaction.QueryClaimByAddressResp;
import com.turn.browser.response.transaction.TransactionDetailsResp;
import com.turn.browser.response.transaction.TransactionListResp;
import com.turn.browser.service.TransactionService;
import com.turn.browser.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Transaction module Contract. Define usage
 */
@Slf4j
@RestController
public class TransactionController {

    @Resource
    private I18nUtil i18n;

    @Resource
    private TransactionService transactionService;

    @Resource
    private DownFileCommon downFileCommon;

    @Resource
    private CommonMethod commonMethod;

    /**
     * transaction list
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.transaction.TransactionListResp>>
     */
    @PostMapping("transaction/transactionList")
    public Mono<RespPage<TransactionListResp>> transactionList(@Valid @RequestBody PageReq req) {
        return Mono.just(transactionService.getTransactionList(req));
    }

    /**
     * Block transaction list
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.transaction.TransactionListResp>>
     */
    @PostMapping("transaction/transactionListByBlock")
    public Mono<RespPage<TransactionListResp>> transactionListByBlock(@Valid @RequestBody TransactionListByBlockRequest req) {
        return Mono.just(transactionService.getTransactionListByBlock(req));
    }

    /**
     * Address's transaction list
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.transaction.TransactionListResp>>
     */
    @PostMapping("transaction/transactionListByAddress")
    public Mono<RespPage<TransactionListResp>> transactionListByAddress(@Valid @RequestBody TransactionListByAddressRequest req) {
        return Mono.just(transactionService.getTransactionListByAddress(req));
    }

    /**
     * Export address transaction list
     *
     * @param address
     * @param date
     * @param local
     * @param timeZone
     * @param token
     * @param response
     * @return void
     */
    @GetMapping("transaction/addressTransactionDownload")
    public void addressTransactionDownload(
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "date", required = true) Long date,
            @RequestParam(value = "local", required = true) String local,
            @RequestParam(value = "timeZone", required = true) String timeZone,
            @RequestParam(value = "token", required = false) String token,
            HttpServletResponse response
    ) {
        /**
         * Authentication
         */
        this.commonMethod.recaptchaAuth(token);
        /**
         * Add a prefix to the address
         */
        address = address.toLowerCase();
        AccountDownload accountDownload =
                transactionService.transactionListByAddressDownload(address, date, local, timeZone);
        try {
            downFileCommon.download(response, accountDownload.getFilename(), accountDownload.getLength(),
                    accountDownload.getData());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(i18n.i(I18nEnum.DOWNLOAD_EXCEPTION));
        }
    }

    /**
     * Transaction Details
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.BaseResp < com.turn.browser.response.transaction.TransactionDetailsResp>>
     */
    @PostMapping("transaction/transactionDetails")
    public Mono<BaseResp<TransactionDetailsResp>> transactionDetails(@Valid @RequestBody TransactionDetailsReq req) {
        return Mono.create(sink -> {
            TransactionDetailsResp resp = transactionService.transactionDetails(req);
            sink.success(BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS), resp));
        });
    }

    /**
     * List of addresses to receive rewards
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.transaction.QueryClaimByAddressResp>>
     */
    @PostMapping("transaction/queryClaimByAddress")
    public Mono<RespPage<QueryClaimByAddressResp>> queryClaimByAddress(@Valid @RequestBody TransactionListByAddressRequest req) {
        return Mono.just(transactionService.queryClaimByAddress(req));
    }

    /**
     * Node-related rewards list
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.staking.QueryClaimByStakingResp>>
     */
    @PostMapping("transaction/queryClaimByStaking")
    public Mono<RespPage<QueryClaimByStakingResp>> queryClaimByStaking(@Valid @RequestBody QueryClaimByStakingReq req) {
        return Mono.just(transactionService.queryClaimByStaking(req));
    }

}
