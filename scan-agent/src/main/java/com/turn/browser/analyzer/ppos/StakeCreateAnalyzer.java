package com.turn.browser.analyzer.ppos;

import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.ComplementNodeOpt;
import com.turn.browser.dao.custommapper.StakeBusinessMapper;
import com.turn.browser.dao.param.ppos.StakeCreate;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.enums.ModifiableGovernParamEnum;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.param.StakeCreateParam;
import com.turn.browser.service.govern.ParameterService;
import com.turn.browser.service.ppos.StakeEpochService;
import com.turn.browser.utils.ChainVersionUtil;
import com.turn.browser.utils.DateUtil;
import com.turn.browser.utils.HexUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Date;


/**
 * @description: Create a validator (staking) business parameter converter
 **/
@Slf4j
@Service
public class StakeCreateAnalyzer
        extends PPOSAnalyzer<NodeOpt> {

    @Resource
    private StakeBusinessMapper stakeBusinessMapper;

    @Resource
    private ParameterService parameterService;

    @Resource
    private StakeEpochService stakeEpochService;

    /**
     * Initiate staking (create a validator)
     *
     * @param event
     * @param tx
     * @return com.turn.browser.elasticsearch.dto.NodeOpt
     */
    @Override
    public NodeOpt analyze(CollectionEvent event, Transaction tx) {
        // Failed transactions do not analyze business data
        if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus())
            return null;

        long startTime = System.currentTimeMillis();

        StakeCreateParam txParam = tx.getTxParam(StakeCreateParam.class);
        BigInteger bigVersion = ChainVersionUtil.toBigVersion(txParam.getProgramVersion());
        BigInteger stakingBlockNum = BigInteger.valueOf(tx.getNum());

        String configVal = parameterService.getValueInBlockChainConfig(ModifiableGovernParamEnum.UN_STAKE_FREEZE_DURATION.getName());
        if (StringUtils.isBlank(configVal)) {
            throw new BusinessException("Parameter table parameter is missing：" + ModifiableGovernParamEnum.UN_STAKE_FREEZE_DURATION.getName());
        }
        Date txTime = DateUtil.covertTime(tx.getTime());
        // Update the number of settlement cycles required for unstaking to be credited to the account
        BigInteger unStakeFreezeDuration = stakeEpochService.getUnStakeFreeDuration();
        // Theoretical exit block number
        BigInteger unStakeEndBlock = stakeEpochService.getUnStakeEndBlock(txParam.getNodeId(), event.getEpochMessage().getSettleEpochRound(), false);
        StakeCreate businessParam = StakeCreate.builder()
                                               .nodeId(txParam.getNodeId())
                                               .stakingHes(txParam.getAmount())
                                               .nodeName(txParam.getNodeName())
                                               .externalId(txParam.getExternalId())
                                               .benefitAddr(txParam.getBenefitAddress())
                                               .programVersion(txParam.getProgramVersion().toString())
                                               .bigVersion(bigVersion.toString())
                                               .webSite(txParam.getWebsite())
                                               .details(txParam.getDetails())
                                               .isInit(isInit(txParam.getBenefitAddress()))
                                               .stakingBlockNum(stakingBlockNum)
                                               .stakingTxIndex(tx.getIndex())
                                               .stakingAddr(tx.getFrom())
                                               .joinTime(txTime)
                                               .txHash(tx.getHash())
                                               .delegateRewardPer(txParam.getDelegateRewardPer())
                                               .unStakeFreezeDuration(unStakeFreezeDuration.intValue())
                                               .unStakeEndBlock(unStakeEndBlock)
                                               .settleEpoch(event.getEpochMessage().getSettleEpochRound().intValue())
                                               .build();

        stakeBusinessMapper.create(businessParam);

        updateNodeCache(HexUtil.prefix(txParam.getNodeId()), txParam.getNodeName(), stakingBlockNum);

        NodeOpt nodeOpt = ComplementNodeOpt.newInstance();
        nodeOpt.setNodeId(txParam.getNodeId());
        nodeOpt.setType(Integer.valueOf(NodeOpt.TypeEnum.CREATE.getCode()));
        nodeOpt.setTxHash(tx.getHash());
        nodeOpt.setBNum(tx.getNum());
        nodeOpt.setTime(txTime);

        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);

        return nodeOpt;
    }

}
