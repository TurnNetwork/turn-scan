package com.turn.browser.controller.token;//package com.turn.browser.controller;

import com.turn.browser.config.CommonMethod;
import com.turn.browser.config.DownFileCommon;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.request.token.QueryTokenTransferRecordListReq;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.account.AccountDownload;
import com.turn.browser.response.token.QueryTokenTransferRecordListResp;
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
@RequestMapping("token/arc721-tx")
public class Arc721TxController {

    @Resource
    private I18nUtil i18n;

    @Resource
    private ErcTxService ercTxService;

    @Resource
    private DownFileCommon downFileCommon;

    @Resource
    private CommonMethod commonMethod;

    /**
     * ARC721 transaction list
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.token.QueryTokenTransferRecordListResp>>
     */
    @PostMapping("list")
    public Mono<RespPage<QueryTokenTransferRecordListResp>> list(@Valid @RequestBody QueryTokenTransferRecordListReq req) {
        return Mono.just(ercTxService.token721TransferList(req));
    }

    /**
     * arc721 transaction list export
     *
     * @param address  wallet address
     * @param contract Contract address
     * @param date     start date timestamp
     * @param local    Region: en or zh-cn
     * @param timeZone Time zone: +8
     * @param token
     * @param tokenId  token id
     * @param response
     * @return void
     * @date 2021/1/27
     */
    @GetMapping("export")
    public void export(@RequestParam(value = "address", required = false) String address,
                       @RequestParam(value = "contract", required = false) String contract,
                       @RequestParam(value = "date", required = true) Long date,
                       @RequestParam(value = "local", required = true) String local,
                       @RequestParam(value = "timeZone", required = true) String timeZone,
                       @RequestParam(value = "token", required = false) String token,
                       @RequestParam(value = "tokenId", required = false) String tokenId,
                       HttpServletResponse response) {
        try {
            /**
             * Authentication
             */
            commonMethod.recaptchaAuth(token);
            AccountDownload accountDownload = ercTxService.exportToken721TransferList(address, contract, date, local, timeZone, tokenId);
            downFileCommon.download(response, accountDownload.getFilename(), accountDownload.getLength(),
                    accountDownload.getData());
        } catch (Exception e) {
            log.error("download error", e);
            throw new BusinessException(this.i18n.i(I18nEnum.DOWNLOAD_EXCEPTION));
        }
    }

}
