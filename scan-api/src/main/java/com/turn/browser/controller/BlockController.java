package com.turn.browser.controller;

import com.turn.browser.config.CommonMethod;
import com.turn.browser.config.DownFileCommon;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RetEnum;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.request.PageReq;
import com.turn.browser.request.newblock.BlockDetailNavigateReq;
import com.turn.browser.request.newblock.BlockDetailsReq;
import com.turn.browser.request.newblock.BlockDownload;
import com.turn.browser.request.newblock.BlockListByNodeIdReq;
import com.turn.browser.response.BaseResp;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.block.BlockDetailResp;
import com.turn.browser.response.block.BlockListResp;
import com.turn.browser.service.BlockService;
import com.turn.browser.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Specific implementation method of block module
 */
@Slf4j
@RestController
public class BlockController {

    private final Logger logger = LoggerFactory.getLogger(BlockController.class);

    @Resource
    private BlockService blockService;

    @Resource
    private I18nUtil i18n;

    @Resource
    private DownFileCommon downFileCommon;

    @Resource
    private CommonMethod commonMethod;

    /**
     * block list
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.block.BlockListResp>>
     */
    @PostMapping("block/blockList")
    public Mono<RespPage<BlockListResp>> blockList(@Valid @RequestBody PageReq req) {
        return Mono.just(blockService.blockList(req));
    }

    /**
     * Node’s block list
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.RespPage < com.turn.browser.response.block.BlockListResp>>
     */
    @PostMapping("block/blockListByNodeId")
    public Mono<RespPage<BlockListResp>> blockListByNodeId(@Valid @RequestBody BlockListByNodeIdReq req) {
        return Mono.just(blockService.blockListByNodeId(req));
    }

    /**
     * Export node’s block list
     *
     * @param nodeId
     * @param date
     * @param local
     * @param timeZone
     * @param token
     * @param response
     * @return void
     */
    @GetMapping("block/blockListByNodeIdDownload")
    public void blockListByNodeIdDownload(
            @RequestParam(value = "nodeId", required = false) String nodeId,
            @RequestParam(value = "date", required = true) Long date,
            @RequestParam(value = "local", required = true) String local,
            @RequestParam(value = "timeZone", required = true) String timeZone,
            @RequestParam(value = "token", required = false) String token,
            HttpServletResponse response
    ) {
        /**
         * Authentication
         */
        commonMethod.recaptchaAuth(token);
        BlockDownload blockDownload = blockService.blockListByNodeIdDownload(nodeId, date, local, timeZone);
        try {
            downFileCommon.download(response, blockDownload.getFilename(), blockDownload.getLength(), blockDownload.getData());
        } catch (Exception e) {
            logger.error("download error", e);
            throw new BusinessException(i18n.i(I18nEnum.DOWNLOAD_EXCEPTION));
        }
    }

    /**
     * Block details
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.BaseResp < com.turn.browser.response.block.BlockDetailResp>>
     */
    @PostMapping("block/blockDetails")
    public Mono<BaseResp<BlockDetailResp>> blockDetails(@Valid @RequestBody BlockDetailsReq req) {
        return Mono.create(sink -> {
            BlockDetailResp resp = blockService.blockDetails(req);
            sink.success(BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS), resp));
        });
    }

    /**
     * Jump forward and backward to browse block details
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.turn.browser.response.BaseResp < com.turn.browser.response.block.BlockDetailResp>>
     */
    @PostMapping("block/blockDetailNavigate")
    public Mono<BaseResp<BlockDetailResp>> blockDetailNavigate(@Valid @RequestBody BlockDetailNavigateReq req) {
        return Mono.create(sink -> {
            BlockDetailResp resp = blockService.blockDetailNavigate(req);
            sink.success(BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS), resp));
        });
    }

}
