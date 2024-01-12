package com.turn.browser.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turn.browser.config.BrowserCache;
import com.turn.browser.config.MessageDto;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RetEnum;
import com.turn.browser.request.staking.AliveStakingListReq;
import com.turn.browser.response.BaseResp;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.home.BlockStatisticNewResp;
import com.turn.browser.response.home.ChainStatisticNewResp;
import com.turn.browser.response.home.StakingListNewResp;
import com.turn.browser.response.microNode.MicroNodeStatisticResp;
import com.turn.browser.response.staking.AliveStakingListResp;
import com.turn.browser.response.staking.StakingStatisticNewResp;
import com.turn.browser.service.*;
import com.turn.browser.service.govern.ParameterService;
import com.turn.browser.utils.AppStatusUtil;
import com.turn.browser.utils.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map.Entry;

/**
 * push task
 */
@Component
public class StompPushTask {

    private static Logger logger = LoggerFactory.getLogger(StompPushTask.class);

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    @Resource
    private I18nUtil i18n;

    @Resource
    private HomeService homeService;

    @Resource
    private StakingService stakingService;

    @Resource
    private ParameterService parameterService;

    @Resource
    private StatisticCacheService statisticCacheService;

    @Resource
    private MicroNodeService microNodeService;

    private boolean checkData() {
        NetworkStat networkStatRedis = this.statisticCacheService.getNetworkStatCache();
        if (networkStatRedis == null || networkStatRedis.getId() == null) {
            return false;
        }
        return true;
    }

    /**
     * Push statistics related information
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void pushChainStatisticNew() {
        // Only perform tasks when the program is running normally
        if (!AppStatusUtil.isRunning()) {
            return;
        }
        if (this.checkData()) {
            ChainStatisticNewResp chainStatisticNewResp = this.homeService.chainStatisticNew();
            BaseResp<ChainStatisticNewResp> resp = BaseResp.build(RetEnum.RET_SUCCESS.getCode(), this.i18n.i(I18nEnum.SUCCESS), chainStatisticNewResp);
            this.messagingTemplate.convertAndSend("/topic/chain/statistic/new", resp);
        }
    }

    /**
     * Push information related to block generation trends
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void pushBlockStatisticNew() {
        if (this.checkData()) {
            BlockStatisticNewResp blockStatisticNewResp = this.homeService.blockStatisticNew();
            BaseResp<BlockStatisticNewResp> resp = BaseResp.build(RetEnum.RET_SUCCESS.getCode(), this.i18n.i(I18nEnum.SUCCESS), blockStatisticNewResp);
            this.messagingTemplate.convertAndSend("/topic/block/statistic/new", resp);
        }
    }

    /**
     * Push verifier related information on the home page
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void pushStakingListNew() {
        if (this.checkData()) {
            StakingListNewResp stakingListNewResp = this.homeService.stakingListNew();
            BaseResp<StakingListNewResp> resp = BaseResp.build(RetEnum.RET_SUCCESS.getCode(), this.i18n.i(I18nEnum.SUCCESS), stakingListNewResp);
            this.messagingTemplate.convertAndSend("/topic/staking/list/new", resp);
        }
    }

    /**
     * Push the verifier to summarize relevant information
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void pushStakingStatisticNew() {
        if (this.checkData()) {
            StakingStatisticNewResp stakingStatisticNewResp = this.stakingService.stakingStatisticNew();
            BaseResp<StakingStatisticNewResp> resp = BaseResp.build(RetEnum.RET_SUCCESS.getCode(), this.i18n.i(I18nEnum.SUCCESS), stakingStatisticNewResp);
            this.messagingTemplate.convertAndSend("/topic/staking/statistic/new", resp);
        }
    }

    /**
     * Push micro-node summary related information
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void pushMicroNodeStatisticNew() {
        if (this.checkData()) {
            MicroNodeStatisticResp microNodeStatisticResp = this.microNodeService.stakingStatistic();
            BaseResp<MicroNodeStatisticResp> resp = BaseResp.build(RetEnum.RET_SUCCESS.getCode(), this.i18n.i(I18nEnum.SUCCESS), microNodeStatisticResp);
            this.messagingTemplate.convertAndSend("/topic/micronode/statistic/new", resp);
        }
    }

    /**
     * Push validator list related information
     *
     * @throws JsonProcessingException
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void pushStakingChangeNew() throws JsonProcessingException {
        if (this.checkData()) {
            for (Entry<String, List<String>> m : BrowserCache.getKeys().entrySet()) {
                MessageDto messageDto = new MessageDto();
                messageDto = messageDto.analysisKey(m.getKey());
                AliveStakingListReq req = new AliveStakingListReq();
                BeanUtils.copyProperties(messageDto, req);
                RespPage<AliveStakingListResp> alives = this.stakingService.aliveStakingList(req);
                for (String userNo : m.getValue()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        BrowserCache.sendMessage(userNo, mapper.writeValueAsString(alives));
                    } catch (Exception e) {
                        BrowserCache.getWebSocketSet().remove(userNo);
                        m.getValue().remove(userNo);
                        /**
                         * Only when there is no user list do you need to remove the entire key
                         */
                        if (m.getValue().isEmpty()) {
                            BrowserCache.getKeys().remove(m.getKey());
                            break;
                        }
                        logger.error("Connection exception, clear connection", e);
                    }
                }
            }
        }
    }

    /**
     * Get configuration regularly
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void updateConfig() {
        if (this.checkData()) {
            this.parameterService.overrideBlockChainConfig();
        }
    }

}
