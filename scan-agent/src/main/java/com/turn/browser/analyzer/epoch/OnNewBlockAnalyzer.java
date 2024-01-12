package com.turn.browser.analyzer.epoch;

import com.turn.browser.bean.CommonConstant;
import com.turn.browser.service.statistic.StatisticService;
import com.turn.browser.utils.ChainVersionUtil;
import com.turn.browser.v0160.service.DelegateBalanceAdjustmentService;
import com.bubble.contracts.dpos.dto.resp.GovernParam;
import com.bubble.contracts.dpos.dto.resp.TallyResult;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.CustomProposal;
import com.turn.browser.cache.NetworkStatCache;
import com.turn.browser.cache.NodeCache;
import com.turn.browser.cache.ProposalCache;
import com.turn.browser.client.TurnClient;
import com.turn.browser.client.SpecialApi;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.v0150.V0150Config;
import com.turn.browser.dao.entity.Config;
import com.turn.browser.dao.entity.Proposal;
import com.turn.browser.dao.entity.ProposalExample;
import com.turn.browser.dao.custommapper.NewBlockMapper;
import com.turn.browser.dao.mapper.ProposalMapper;
import com.turn.browser.dao.param.epoch.NewBlock;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.exception.NoSuchBeanException;
import com.turn.browser.service.govern.ParameterService;
import com.turn.browser.service.proposal.ProposalService;
import com.turn.browser.v0150.bean.AdjustParam;
import com.turn.browser.v0150.service.StakingDelegateBalanceAdjustmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Slf4j
@Service
public class OnNewBlockAnalyzer {

    @Resource
    private NodeCache nodeCache;

    @Resource
    private NewBlockMapper newBlockMapper;

    @Resource
    private NetworkStatCache networkStatCache;

    @Resource
    private ProposalCache proposalCache;

    @Resource
    private ProposalService proposalService;

    @Resource
    private ProposalMapper proposalMapper;

    @Resource
    private ParameterService parameterService;

    @Resource
    private TurnClient turnClient;

    @Resource
    private V0150Config v0150Config;

    @Resource
    private StakingDelegateBalanceAdjustmentService stakingDelegateBalanceAdjustmentService;

    @Resource
    private SpecialApi specialApi;

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private StatisticService statisticService;

    @Resource
    private DelegateBalanceAdjustmentService delegateBalanceAdjustmentService;

    public void analyze(CollectionEvent event, Block block) throws NoSuchBeanException {

        long startTime = System.currentTimeMillis();

        networkStatCache.getNetworkStat().setCurNumber(event.getBlock().getNum());
        networkStatCache.getNetworkStat().setCurBlockHash(event.getBlock().getHash());
        NewBlock newBlock = NewBlock.builder()
                                    .nodeId(block.getNodeId())
                                    .stakingBlockNum(nodeCache.getNode(block.getNodeId()).getStakingBlockNum())
                                    .blockRewardValue(event.getEpochMessage().getBlockReward())
                                    .feeRewardValue(new BigDecimal(block.getTxFee()))
                                    .predictStakingReward(event.getEpochMessage().getStakeReward())
                                    .build();

        newBlockMapper.newBlock(newBlock);
        log.info("The handling fee of node [{}] with block height [{}] is [{}] and the block reward is [{}]",
                 event.getBlock().getNum(),
                 newBlock.getNodeId(),
                 newBlock.getFeeRewardValue(),
                 newBlock.getBlockRewardValue());

        // Check whether any parameter proposal takes effect in the current block
        Set<String> proposalTxHashSet = proposalCache.get(block.getNum());
        if (proposalTxHashSet != null) {
            ProposalExample proposalExample = new ProposalExample();
            proposalExample.createCriteria().andHashIn(new ArrayList<>(proposalTxHashSet));
            List<Proposal> proposalList = proposalMapper.selectByExample(proposalExample);
            Map<String, Proposal> proposalMap = new HashMap<>();
            proposalList.forEach(p -> proposalMap.put(p.getHash(), p));
            List<Config> configList = new ArrayList<>();
            for (String hash : proposalTxHashSet) {
                try {
                    TallyResult tr = proposalService.getTallyResult(hash);
                    if (tr == null) {
                        continue;
                    }
                    if (tr.getStatus() == CustomProposal.StatusEnum.PASS.getCode() || tr.getStatus() == CustomProposal.StatusEnum.FINISH.getCode()) {
                        // Proposal passed (parameter proposal, status=2)||Proposal takes effect (upgrade proposal, status=5):
                        // Overwrite the parameters in the proposal table to the corresponding parameters in the Config table
                        Proposal proposal = proposalMap.get(hash);
                        if (proposal.getType() == CustomProposal.TypeEnum.PARAMETER.getCode()) {
                            // If it is a parameter proposal
                            // Overwrite the parameters in the proposal table to the corresponding parameters in the Config table
                            Config config = new Config();
                            config.setModule(proposal.getModule());
                            config.setName(proposal.getName());
                            config.setStaleValue(proposal.getStaleValue());
                            config.setValue(proposal.getNewValue());
                            configList.add(config);
                        }
                        if (proposal.getType() == CustomProposal.TypeEnum.UPGRADE.getCode()) {
                            // If it is an upgrade proposal
                            // Then query the governance parameter details and overwrite the new parameter values to the corresponding parameters in the Config table
                            List<GovernParam> governParamList = turnClient.getGovernParamValue("");
                            governParamList.forEach(gp -> {
                                Config config = new Config();
                                config.setModule(gp.getParamItem().getModule());
                                config.setName(gp.getParamItem().getName());
                                config.setStaleValue(gp.getParamValue().getStaleValue());
                                config.setValue(gp.getParamValue().getValue());
                                configList.add(config);
                            });
                            BigInteger proposalVersion = new BigInteger(proposal.getNewVersion());
                            String proposalPipid = proposal.getPipId();
                            BigInteger configVersion = v0150Config.getAdjustmentActiveVersion();
                            String configPipid = v0150Config.getAdjustmentPipId();
                            if (proposalVersion.compareTo(configVersion) >= 0 && proposalPipid.equals(configPipid)) {
                                // If the upgrade proposal version number and proposal ID are the same as those specified in the configuration file, the account adjustment logic will be executed.
                                List<AdjustParam> adjustParams = specialApi.getStakingDelegateAdjustDataList(turnClient.getWeb3jWrapper()
                                                                                                                         .getWeb3j(), BigInteger.valueOf(block.getNum()));
                                adjustParams.forEach(param -> {
                                    param.setBlockTime(block.getTime());
                                    param.setSettleBlockCount(chainConfig.getSettlePeriodBlockCount());
                                });
                                stakingDelegateBalanceAdjustmentService.adjust(adjustParams);
                            }
                            //Alaya main network is compatible with the bottom layer's account adjustment function upgraded to 0.16.0, corresponding to bottom layer issue1583
                            BigInteger v0160Version = ChainVersionUtil.toBigIntegerVersion(CommonConstant.V0160_VERSION);
                            if (proposalVersion.compareTo(v0160Version) == 0 && CommonConstant.ALAYA_CHAIN_ID == chainConfig.getChainId()) {
                                delegateBalanceAdjustmentService.adjust();
                            }
                            // Upgrade proposal 1.3.0+ will configure the unDelegateFreezeDuration governance parameter
                            parameterService.configUnDelegateFreezeDuration(proposalVersion);
                        }
                    }
                } catch (Exception e) {
                    log.error("get error", e);
                    throw new BusinessException(e.getMessage());
                }
            }
            if (!configList.isEmpty()) {
                // Update the configuration table config and blockChainConfig in memory
                parameterService.rotateConfig(configList);
            }
        }

        statisticService.nodeSettleStatisBlockNum(event);

        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
    }

}
