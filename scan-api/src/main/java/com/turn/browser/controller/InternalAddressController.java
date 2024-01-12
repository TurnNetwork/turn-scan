package com.turn.browser.controller;

import com.turn.browser.request.PageReq;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.address.InternalAddressResp;
import com.turn.browser.service.InternalAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * inner address
 */
@Slf4j
@RestController
public class InternalAddressController {

    @Resource
    private InternalAddressService internalAddressService;

    /**
     * Get the foundation account list (with address balance)
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.address.InternalAddressResp>>
     */
    @PostMapping("internalAddress/foundationInfo")
    public Mono<RespPage<InternalAddressResp>> getFoundationInfo(@Valid @RequestBody PageReq req) {
        return Mono.just(internalAddressService.getFoundationInfo(req));
    }

    /**
     * Get the foundation address
     *
     * @param req:
     * @return: reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.address.InternalAddrResp>>
     */
    @PostMapping("internalAddress/list")
    public Mono<RespPage<String>> getInternalAddressList(@Valid @RequestBody PageReq req) {
        return Mono.just(internalAddressService.getInternalAddressList(req));
    }

}
