package com.turn.browser.controller;

import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RetEnum;
import com.turn.browser.request.bubble.BubbleListReq;
import com.turn.browser.request.micronode.AliveMicroNodeListReq;
import com.turn.browser.request.micronode.MicroNodeDetailsReq;
import com.turn.browser.request.micronode.MicroNodeOptRecordListReq;
import com.turn.browser.response.BaseResp;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.bubble.BubbleListResp;
import com.turn.browser.response.microNode.AliveMicroNodeListResp;
import com.turn.browser.response.microNode.MicroNodeDetailsResp;
import com.turn.browser.response.microNode.MicroNodeOptRecordListResp;
import com.turn.browser.response.microNode.MicroNodeStatisticResp;
import com.turn.browser.service.BubbleService;
import com.turn.browser.service.MicroNodeService;
import com.turn.browser.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * bubble controller
 */
@Slf4j
@RestController
public class BubbleController {

    @Resource
    private BubbleService bubbleService;


    @PostMapping("bubble/list")
    public Mono<RespPage<BubbleListResp>> bubbleList(@Valid @RequestBody BubbleListReq req) {
        return Mono.just(bubbleService.bubbleList(req));
    }

}
