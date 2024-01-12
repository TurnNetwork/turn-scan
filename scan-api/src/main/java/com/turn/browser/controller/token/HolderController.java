package com.turn.browser.controller.token;

import com.turn.browser.config.CommonMethod;
import com.turn.browser.config.DownFileCommon;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.request.token.QueryTokenHolderListReq;
import com.turn.browser.response.BaseResp;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.account.AccountDownload;
import com.turn.browser.response.token.QueryTokenHolderListResp;
import com.turn.browser.service.ErcTxService;
import com.turn.browser.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("token/holder")
public class HolderController {

    @Resource
    private I18nUtil i18n;

    @Resource
    private ErcTxService ercTxService;

    @Resource
    private DownFileCommon downFileCommon;

    @Resource
    private CommonMethod commonMethod;

    /**
     * List of token holders
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.token.QueryTokenHolderListResp>>
     */
    @PostMapping("list")
    public Mono<RespPage<QueryTokenHolderListResp>> list(@Valid @RequestBody QueryTokenHolderListReq req) {
        return Mono.just(ercTxService.tokenHolderList(req));
    }

    /**
     * Token holder list export
     *
     * @param contract:
     * @param local:
     * @param timeZone:
     * @param token:
     * @param ercType:
     * @param response:
     * @return: void
     */
    @GetMapping("export")
    public void export(@RequestParam(value = "contract", required = true) String contract, @RequestParam(value = "local", required = true) String local, @RequestParam(value = "timeZone", required = true) String timeZone, @RequestParam(value = "token", required = false) String token, @RequestParam(value = "ercType", required = true) String ercType, HttpServletResponse response) {
        try {
            /**
             * Authentication
             */
            commonMethod.recaptchaAuth(token);
            AccountDownload accountDownload = ercTxService.exportTokenHolderList(contract, local, timeZone, ercType);
            downFileCommon.download(response, accountDownload.getFilename(), accountDownload.getLength(), accountDownload.getData());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(i18n.i(I18nEnum.DOWNLOAD_EXCEPTION));
        }
    }

}
