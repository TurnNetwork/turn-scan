package com.turn.browser.controller;

import com.turn.browser.request.address.QueryAddrGameListReq;
import com.turn.browser.response.BaseResp;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.address.AddrGameDetailResp;
import com.turn.browser.service.AddrGameService;
import com.turn.browser.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;


@Slf4j
@RestController
public class AddrGameController {

    @Resource
    private AddrGameService addrGameService;

    /**
     * Query the details of the game list that the address participates in
     */
    @PostMapping("addrGame/list")
    public Mono<BaseResp<List<AddrGameDetailResp>>> list(@Valid @RequestBody QueryAddrGameListReq req) {
        return Mono.just(addrGameService.getList(req));
    }

}
