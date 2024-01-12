package com.turn.browser.controller;

import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RetEnum;
import com.turn.browser.request.address.QueryDetailRequest;
import com.turn.browser.request.address.QueryRPPlanDetailRequest;
import com.turn.browser.response.BaseResp;
import com.turn.browser.response.address.QueryDetailResp;
import com.turn.browser.response.address.QueryRPPlanDetailResp;
import com.turn.browser.service.AddressService;
import com.turn.browser.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * The specific implementation of the address Controller provides the use of the address details page
 */
@Slf4j
@RestController
public class AddressController {

    @Resource
    private AddressService addressService;

    @Resource
    private I18nUtil i18n;

    /**
     * Check address details
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.BaseResp < com.turn.browser.response.address.QueryDetailResp>>
     */
    @PostMapping("address/details")
    public Mono<BaseResp<QueryDetailResp>> details(@Valid @RequestBody QueryDetailRequest req) {
        return Mono.create(sink -> {
            QueryDetailResp resp = addressService.getDetails(req);
            sink.success(BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS), resp));
        });
    }

    /**
     * Address lock details
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.BaseResp < com.turn.browser.response.address.QueryRPPlanDetailResp>>
     */
    @PostMapping("address/rpplanDetail")
    public Mono<BaseResp<QueryRPPlanDetailResp>> rpplanDetail(@Valid @RequestBody QueryRPPlanDetailRequest req) {
        return Mono.create(sink -> {
            QueryRPPlanDetailResp resp = addressService.rpplanDetail(req);
            sink.success(BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS), resp));
        });
    }

}
