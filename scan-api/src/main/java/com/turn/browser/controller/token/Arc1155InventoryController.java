package com.turn.browser.controller.token;

import com.turn.browser.config.CommonMethod;
import com.turn.browser.config.DownFileCommon;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RetEnum;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.request.token.QueryTokenIdDetailReq;
import com.turn.browser.request.token.QueryTokenIdListReq;
import com.turn.browser.response.BaseResp;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.account.AccountDownload;
import com.turn.browser.response.token.QueryTokenIdDetailResp;
import com.turn.browser.response.token.QueryTokenIdListResp;
import com.turn.browser.service.Token1155Service;
import com.turn.browser.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("token/arc1155-inventory")
public class Arc1155InventoryController {

    @Resource
    private I18nUtil i18n;

    @Resource
    private DownFileCommon downFileCommon;

    @Resource
    private CommonMethod commonMethod;

    @Resource
    private Token1155Service token1155Service;

    /**
     * ARC1155 Stock List
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.token.QueryTokenIdListResp>>
     */
    @PostMapping("list")
    public Mono<RespPage<QueryTokenIdListResp>> list(@Valid @RequestBody QueryTokenIdListReq req) {
        return Mono.just(token1155Service.queryTokenIdList(req));
    }

    /**
     * ARC1155 stock details
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.BaseResp < com.turn.browser.response.token.QueryTokenIdDetailResp>>
     */
    @PostMapping("detail")
    public Mono<BaseResp<QueryTokenIdDetailResp>> detail(@Valid @RequestBody QueryTokenIdDetailReq req) {
        return Mono.create(sink -> {
            QueryTokenIdDetailResp resp = token1155Service.queryTokenIdDetail(req);
            sink.success(BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS), resp));
        });
    }

    /**
     * ARC1155 inventory list export
     *
     * @param address
     * @param contract
     * @param tokenId
     * @param local
     * @param timeZone
     * @param token
     * @param response
     * @return void
     * @date 2021/5/25
     */
    @GetMapping("export")
    public void export(@RequestParam(value = "address", required = false) String address, @RequestParam(value = "contract", required = false) String contract, @RequestParam(value = "tokenId", required = false) String tokenId, @RequestParam(value = "local", required = true) String local, @RequestParam(value = "timeZone", required = true) String timeZone, @RequestParam(value = "token", required = false) String token, HttpServletResponse response) {
        try {
            /**
             * Authentication
             */
            commonMethod.recaptchaAuth(token);
            AccountDownload accountDownload = token1155Service.exportTokenId(address, contract, tokenId, local, timeZone);
            downFileCommon.download(response, accountDownload.getFilename(), accountDownload.getLength(), accountDownload.getData());
        } catch (Exception e) {
            log.error("download error", e);
            throw new BusinessException(this.i18n.i(I18nEnum.DOWNLOAD_EXCEPTION));
        }
    }

}
