package com.turn.browser.controller.token;//package com.turn.browser.controller;

import com.turn.browser.config.CommonMethod;
import com.turn.browser.config.DownFileCommon;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.request.token.QueryHolderTokenListReq;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.account.AccountDownload;
import com.turn.browser.response.token.QueryHolderTokenListResp;
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
@RequestMapping("token/holder-token")
public class HolderTokenController {

    @Resource
    private I18nUtil i18n;

    @Resource
    private ErcTxService ercTxService;

    @Resource
    private DownFileCommon downFileCommon;

    @Resource
    private CommonMethod commonMethod;

    /**
     * Token list of holder
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.token.QueryHolderTokenListResp>>
     */
    @PostMapping("list")
    public Mono<RespPage<QueryHolderTokenListResp>> list(@Valid @RequestBody QueryHolderTokenListReq req) {
        return Mono.just(ercTxService.holderTokenList(req));
    }

    /**
     * Export the holder's token
     *
     * @param address  Contract address
     * @param local    Region: en or zh-cn
     * @param timeZone
     * @param token
     * @param type     Contract type (value erc20 or erc721)
     * @param response
     * @return void
     */
    @GetMapping("export")
    public void export(@RequestParam(value = "address", required = true) String address,
                       @RequestParam(value = "local", required = true) String local,
                       @RequestParam(value = "timeZone", required = true) String timeZone,
                       @RequestParam(value = "token", required = false) String token,
                       @RequestParam(value = "type", required = false) String type,
                       HttpServletResponse response) {
        try {
            /**
             * Authentication
             */
            commonMethod.recaptchaAuth(token);
            AccountDownload accountDownload = ercTxService.exportHolderTokenList(address, local, timeZone, type);
            downFileCommon.download(response, accountDownload.getFilename(), accountDownload.getLength(),
                    accountDownload.getData());
        } catch (Exception e) {
            log.error("download error", e);
            throw new BusinessException(i18n.i(I18nEnum.DOWNLOAD_EXCEPTION));
        }
    }

}
