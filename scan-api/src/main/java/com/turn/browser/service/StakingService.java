package com.turn.browser.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.turn.browser.bean.*;
import com.turn.browser.bean.CustomDelegation.YesNoEnum;
import com.turn.browser.bean.CustomStaking.StatusEnum;
import com.turn.browser.client.TurnClient;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.constant.Browser;
import com.turn.browser.dao.custommapper.CustomDelegationMapper;
import com.turn.browser.dao.custommapper.CustomNodeMapper;
import com.turn.browser.dao.custommapper.CustomVoteMapper;
import com.turn.browser.dao.entity.*;
import com.turn.browser.dao.mapper.AddressMapper;
import com.turn.browser.dao.mapper.NodeMapper;
import com.turn.browser.dao.mapper.ProposalMapper;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.enums.AddressTypeEnum;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RetEnum;
import com.turn.browser.enums.StakingStatusEnum;
import com.turn.browser.request.staking.*;
import com.turn.browser.response.BaseResp;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.staking.*;
import com.turn.browser.service.elasticsearch.EsNodeOptRepository;
import com.turn.browser.service.elasticsearch.bean.ESResult;
import com.turn.browser.service.elasticsearch.query.ESQueryBuilderConstructor;
import com.turn.browser.service.elasticsearch.query.ESQueryBuilders;
import com.turn.browser.utils.*;
import com.bubble.contracts.dpos.dto.resp.Reward;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Validator module methods
 */
@Service
public class StakingService {

    private final Logger logger = LoggerFactory.getLogger(StakingService.class);

    @Resource
    private StatisticCacheService statisticCacheService;

    @Resource
    private CustomVoteMapper customVoteMapper;

    @Resource
    private CustomDelegationMapper customDelegationMapper;

    @Resource
    private CustomNodeMapper customNodeMapper;

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private EsNodeOptRepository ESNodeOptRepository;

    @Resource
    private I18nUtil i18n;

    @Resource
    private BlockChainConfig blockChainConfig;

    @Resource
    private TurnClient turnClient;

    @Resource
    private CommonService commonService;

    @Resource
    private AddressMapper addressMapper;

    @Resource
    private ProposalMapper proposalMapper;

    public StakingStatisticNewResp stakingStatisticNew() {
        /** Get statistics */
        NetworkStat networkStatRedis = statisticCacheService.getNetworkStatCache();
        StakingStatisticNewResp stakingStatisticNewResp = new StakingStatisticNewResp();
        if (networkStatRedis != null) {
            BeanUtils.copyProperties(networkStatRedis, stakingStatisticNewResp);
            stakingStatisticNewResp.setCurrentNumber(networkStatRedis.getCurNumber());
            stakingStatisticNewResp.setNextSetting(networkStatRedis.getNextSettle());
            // Accepted commissions = Total number of real-time pledge commissions - Total number of real-time pledges
            stakingStatisticNewResp.setDelegationValue(networkStatRedis.getStakingDelegationValue().subtract(networkStatRedis.getStakingValue()));
            stakingStatisticNewResp.setStakingReward(networkStatRedis.getStakingReward());
            stakingStatisticNewResp.setIssueValue(networkStatRedis.getIssueValue());
            StakingBO bo = commonService.getTotalStakingValueAndStakingDenominator(networkStatRedis);
            stakingStatisticNewResp.setStakingDenominator(bo.getStakingDenominator());
            stakingStatisticNewResp.setStakingDelegationValue(bo.getTotalStakingValue());
        }
        return stakingStatisticNewResp;
    }

    public RespPage<AliveStakingListResp> aliveStakingList(AliveStakingListReq req) {
        PageHelper.startPage(req.getPageNo(), req.getPageSize());
        Integer status = null;
        Integer isSettle = null;

        // Whether it is necessary to treat the exiting node as active. By default, it is not required. Only when querying all or active nodes, the exiting state needs to be treated as active.
        boolean exitingAsActive = false;
        /**
         *  Convert the parameters passed from the front end to query conditions
         */
        switch (StakingStatusEnum.valueOf(req.getQueryStatus().toUpperCase())) {
            case ALL:
                /** Query candidates */
                status = StakingStatusEnum.CANDIDATE.getCode();
                exitingAsActive = true;
                break;
            case ACTIVE:
                /** Active representatives will also be validators of the settlement cycle even if they follow up. */
                status = StakingStatusEnum.CANDIDATE.getCode();
                isSettle = CustomStaking.YesNoEnum.YES.getCode();
                exitingAsActive = true;
                break;
            case CANDIDATE:
                /** Query candidates */
                status = StakingStatusEnum.CANDIDATE.getCode();
                isSettle = CustomStaking.YesNoEnum.NO.getCode();
                break;
            default:
                break;
        }
        RespPage<AliveStakingListResp> respPage = new RespPage<>();
        List<AliveStakingListResp> lists = new LinkedList<>();
        /** Query list based on conditions and status */
        NodeExample nodeExample = new NodeExample();
        nodeExample.setOrderByClause(" big_version desc, total_value desc,staking_block_num asc, staking_tx_index asc");
        NodeExample.Criteria criteria1 = nodeExample.createCriteria();
        criteria1.andStatusEqualTo(status);
        if (StringUtils.isNotBlank(req.getKey())) {
            criteria1.andNodeNameLike("%" + req.getKey() + "%");
        }
        if (isSettle != null) {
            criteria1.andIsSettleEqualTo(isSettle);
        }

        if (exitingAsActive) {
            /**
             * If the node status is Exiting and it is the settlement period, it is considered to be active.
             */
            NodeExample.Criteria criteria2 = nodeExample.createCriteria();
            criteria2.andStatusEqualTo(CustomStaking.StatusEnum.EXITING.getCode());
            if (StringUtils.isNotBlank(req.getKey())) {
                criteria2.andNodeNameLike("%" + req.getKey() + "%");
            }
            criteria2.andIsSettleEqualTo(CustomStaking.YesNoEnum.YES.getCode());
            nodeExample.or(criteria2);
        }

        Page<Node> stakingPage = customNodeMapper.selectListByExample(nodeExample);
        List<Node> stakings = stakingPage.getResult();
        /** Query the block producing node */
        NetworkStat networkStatRedis = statisticCacheService.getNetworkStatCache();
        int i = (req.getPageNo() - 1) * req.getPageSize();
        for (Node staking : stakings) {
            AliveStakingListResp aliveStakingListResp = new AliveStakingListResp();
            BeanUtils.copyProperties(staking, aliveStakingListResp);
            aliveStakingListResp.setBlockQty(staking.getStatBlockQty());
            aliveStakingListResp.setDelegateQty(staking.getStatValidAddrs());
            aliveStakingListResp.setExpectedIncome(staking.getAnnualizedRate().toString());
            /** The total amount of entrusted transactions = the total amount of entrusted transactions (amount during the hesitation period) + the total amount of entrusted transactions (amount during the lock-in period) */
            String sumAmount = staking.getStatDelegateValue().toString();
            aliveStakingListResp.setDelegateValue(sumAmount);
            aliveStakingListResp.setIsInit(staking.getIsInit() == 1);
            aliveStakingListResp.setStakingIcon(staking.getNodeIcon());
            if (staking.getIsRecommend() != null) {
                aliveStakingListResp.setIsRecommend(CustomStaking.YesNoEnum.YES.getCode() == staking.getIsRecommend());
            }

            aliveStakingListResp.setRanking(i + 1);
            aliveStakingListResp.setSlashLowQty(staking.getStatSlashLowQty());
            aliveStakingListResp.setSlashMultiQty(staking.getStatSlashMultiQty());
            /** If it is the corresponding block-producing node, it is set to be producing blocks, otherwise it is active or exited. */
            if (staking.getNodeId().equals(networkStatRedis.getNodeId())) {
                aliveStakingListResp.setStatus(StakingStatusEnum.BLOCK.getCode());
            } else {
                aliveStakingListResp.setStatus(StakingStatusEnum.getCodeByStatus(staking.getStatus(),
                                                                                 staking.getIsConsensus(),
                                                                                 staking.getIsSettle()));
            }
            /** Total number of pledges = valid pledges + delegation */
            aliveStakingListResp.setTotalValue(staking.getTotalValue().toString());
            aliveStakingListResp.setDeleAnnualizedRate(staking.getDeleAnnualizedRate().toString());

            try {
                String nodeSettleStatisInfo = staking.getNodeSettleStatisInfo();
                NodeSettleStatis nodeSettleStatis = NodeSettleStatis.jsonToBean(nodeSettleStatisInfo);
                BigInteger settleEpochRound = EpochUtil.getEpoch(BigInteger.valueOf(networkStatRedis.getCurNumber()),
                                                                 blockChainConfig.getSettlePeriodBlockCount());
                aliveStakingListResp.setGenBlocksRate(nodeSettleStatis.computeGenBlocksRate(settleEpochRound));
                aliveStakingListResp.setUpTime(computeUpTime(staking, nodeSettleStatis, settleEpochRound));
            } catch (Exception e) {
                logger.error("Obtain the abnormal block production rate of the node in 24 hours", e);
            }
            aliveStakingListResp.setDelegatedRewardRatio(new BigDecimal(staking.getRewardPer()).divide(Browser.PERCENTAGE).toString() + "%");
            if (staking.getProgramVersion() != 0) {
                aliveStakingListResp.setVersion(ChainVersionUtil.toStringVersion(BigInteger.valueOf(staking.getProgramVersion())));
            } else {
                aliveStakingListResp.setVersion(ChainVersionUtil.toStringVersion(BigInteger.valueOf(staking.getBigVersion())));
            }
            aliveStakingListResp.setPreDeleAnnualizedRate(NodeApr.getPreDeleAnnualizedRate(staking.getNodeApr()));
            lists.add(aliveStakingListResp);
            i++;
        }
        Page<?> page = new Page<>(req.getPageNo(), req.getPageSize());
        page.setTotal(stakingPage.getTotal());
        respPage.init(page, lists);
        return respPage;
    }

    // The node’s uptime ratio in the last 24 hours (8 settlement cycles)
    private String computeUpTime(Node node, NodeSettleStatis nodeSettleStatis, BigInteger settleEpochRound) {
        try {
            BigInteger settleNum = BigInteger.valueOf(10750L);
            // Total block of statistical period
            BigInteger totalBlockNum = settleNum.multiply(BigInteger.valueOf(CommonConstant.BLOCK_APR_EPOCH_NUM));
            BigInteger max = settleNum.multiply(settleEpochRound.subtract(BigInteger.ONE));
            // Maximum block in statistical period
            max = max.compareTo(settleNum) < 0 ? settleNum : max;
            BigInteger min = settleNum.multiply(settleEpochRound.subtract(BigInteger.valueOf(CommonConstant.BLOCK_APR_EPOCH_NUM))
                                                                .subtract(BigInteger.ONE));
            // Minimum block of statistical period
            min = min.compareTo(BigInteger.ZERO) <= 0 ? BigInteger.ZERO : min;
            BigInteger subNum = computeSubNum(nodeSettleStatis, settleEpochRound);
            BigInteger stakingSubNum = computeStakingSubNum(max, min, BigInteger.valueOf(node.getStakingBlockNum()));
            BigInteger leaveSubNum = leaveSubNum(node, max, min, settleNum);
            BigInteger subBlockNUm = totalBlockNum.subtract(subNum).subtract(stakingSubNum).subtract(leaveSubNum);
            BigDecimal percent = new BigDecimal(subBlockNUm)
                    .divide(new BigDecimal(totalBlockNum), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(6, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            logger.info("Node [{}] running time ratio [{}] = (total number of blocks [{}] - blocks that should be released [{}] - pledged blocks [{}] - exit blocks [{} ])/total number of blocks[{}]*100%",
                        node.getNodeId(),
                        percent.toPlainString() + "%",
                        totalBlockNum.longValue(),
                        subNum.longValue(),
                        stakingSubNum.longValue(),
                        leaveSubNum.longValue(),
                        totalBlockNum.longValue());
            return percent.toPlainString() + "%";
        } catch (Exception e) {
            logger.error("Abnormal 24-hour uptime of computing nodes", e);
            return "0%";
        }
    }

    /**
     * Count the number of blocks that should have been produced but have not been produced by the node in 8 settlement cycles.
     *
     * @param nodeSettleStatis:
     * @param settleEpochRound:
     */
    private BigInteger computeSubNum(NodeSettleStatis nodeSettleStatis, BigInteger settleEpochRound) {
        BigInteger subNum = BigInteger.ZERO;
        if (nodeSettleStatis.getNodeSettleStatisQueue().size() > 0) {
            // Sorted by settlement cycle number, index[0] is the largest
            List<NodeSettleStatisBase> list = nodeSettleStatis.getNodeSettleStatisQueue().toList();
            // Filter the data, and the difference between the current settlement cycle number and the round number is greater than 0 and less than or equal to 8, that is, the last 8 settlement cycles. Excludes current billing cycle
            List<NodeSettleStatisBase> last = list.stream().filter(v -> {
                BigInteger difference = settleEpochRound.subtract(v.getSettleEpochRound());
                return difference.compareTo(BigInteger.ZERO) > 0 && difference.compareTo(BigInteger.valueOf(CommonConstant.BLOCK_APR_EPOCH_NUM)) <= 0;
            }).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(last)) {
                LongSummaryStatistics blockNumGrandTotal = last.stream()
                                                               .collect(Collectors.summarizingLong(v -> v.getBlockNumGrandTotal().longValue()));
                LongSummaryStatistics blockNumElected = last.stream()
                                                            .collect(Collectors.summarizingLong(v -> v.getBlockNumElected().longValue()));
                // Fewer blocks are produced in the current settlement cycle
                if (BigInteger.valueOf(blockNumElected.getSum())
                              .multiply(BigInteger.TEN)
                              .compareTo(BigInteger.valueOf(blockNumGrandTotal.getSum())) > 0) {
                    BigInteger sub = BigInteger.valueOf(blockNumElected.getSum())
                                               .multiply(BigInteger.TEN)
                                               .subtract(BigInteger.valueOf(blockNumGrandTotal.getSum()));
                    subNum = subNum.add(sub);
                }
            }
        }
        return subNum;
    }

    /**
     * Node staking block height minus starting block
     *
     * @param max:
     * @param min:
     * @param stakingBlockNum:
     * @return:
     * @date: 2022/11/22
     */
    private BigInteger computeStakingSubNum(BigInteger max,
                                            BigInteger min,
                                            BigInteger stakingBlockNum) {
        BigInteger stakingSubNum = BigInteger.ZERO;
        if (stakingBlockNum.compareTo(min) >= 0 && stakingBlockNum.compareTo(max) <= 0) {
            stakingSubNum = stakingBlockNum.subtract(min);
        }
        return stakingSubNum;
    }

    private BigInteger leaveSubNum(Node node,
                                   BigInteger max,
                                   BigInteger min,
                                   BigInteger settleNum) {
        BigInteger leaveSubNum = BigInteger.ZERO;
        BigInteger startNum = min;
        BigInteger endNum = max;
        if (ObjectUtil.isNull(node.getLeaveNum())
                && node.getZeroProduceFreezeEpoch() <= 0
                && node.getZeroProduceFreezeDuration() <= 0) {
            return leaveSubNum;
        }
        // If there is leaveNum, then leaveNum shall prevail.
        if (ObjectUtil.isNotNull(node.getLeaveNum())
                && BigInteger.valueOf(node.getLeaveNum()).compareTo(min) >= 0
                && BigInteger.valueOf(node.getLeaveNum()).compareTo(max) <= 0) {
            startNum = BigInteger.valueOf(node.getLeaveNum());
        } else if (ObjectUtil.isNull(node.getLeaveNum())
                && node.getZeroProduceFreezeEpoch() > 0
                && node.getZeroProduceFreezeDuration() > 0) {
            startNum = BigInteger.valueOf(node.getZeroProduceFreezeEpoch()).multiply(settleNum);
        }
        if (startNum.compareTo(min) < 0) {
            startNum = min;
        }
        if (node.getZeroProduceFreezeEpoch() > 0 && node.getZeroProduceFreezeDuration() > 0) {
            BigInteger zeroProduceFreezeEpoch = BigInteger.valueOf(node.getZeroProduceFreezeEpoch() + node.getZeroProduceFreezeDuration())
                                                          .multiply(settleNum);
            endNum = (zeroProduceFreezeEpoch.compareTo(min) >= 0 && zeroProduceFreezeEpoch.compareTo(max) <= 0) ? zeroProduceFreezeEpoch : max;
        }

        leaveSubNum = endNum.subtract(startNum);
        return leaveSubNum;
    }

    public RespPage<HistoryStakingListResp> historyStakingList(HistoryStakingListReq req) {
        /** Set to query only exiting and exited */
        PageHelper.startPage(req.getPageNo(), req.getPageSize());
        List<Integer> status = new ArrayList<>();
        status.add(CustomStaking.StatusEnum.EXITING.getCode());
        status.add(CustomStaking.StatusEnum.EXITED.getCode());
        RespPage<HistoryStakingListResp> respPage = new RespPage<>();
        List<HistoryStakingListResp> lists = new LinkedList<>();
        /** Query list based on conditions and status */
        NodeExample nodeExample = new NodeExample();
        nodeExample.setOrderByClause(" leave_time desc");
        NodeExample.Criteria criteria = nodeExample.createCriteria();
        criteria.andStatusIn(status);
        /**
         * Prevent directly exited nodes from appearing in the history table
         */
        criteria.andIsSettleEqualTo(CustomStaking.YesNoEnum.NO.getCode());

        if (StringUtils.isNotBlank(req.getKey())) {
            criteria.andNodeNameLike("%" + req.getKey() + "%");
        }
        Page<Node> stakings = customNodeMapper.selectListByExample(nodeExample);

        for (Node stakingNode : stakings.getResult()) {
            HistoryStakingListResp historyStakingListResp = new HistoryStakingListResp();
            BeanUtils.copyProperties(stakingNode, historyStakingListResp);
            if (stakingNode.getLeaveTime() != null) {
                historyStakingListResp.setLeaveTime(stakingNode.getLeaveTime().getTime());
            }
            historyStakingListResp.setNodeName(stakingNode.getNodeName());
            historyStakingListResp.setStakingIcon(stakingNode.getNodeIcon());
            historyStakingListResp.setSlashLowQty(stakingNode.getStatSlashLowQty());
            historyStakingListResp.setSlashMultiQty(stakingNode.getStatSlashMultiQty());
            /**
             * The delegate to be extracted is equal to hes+lock
             */
            historyStakingListResp.setStatDelegateReduction(stakingNode.getStatDelegateValue().add(stakingNode.getStatDelegateReleased()));
            historyStakingListResp.setStatus(StakingStatusEnum.getCodeByStatus(stakingNode.getStatus(),
                                                                               stakingNode.getIsConsensus(),
                                                                               stakingNode.getIsSettle()));
            historyStakingListResp.setBlockQty(stakingNode.getStatBlockQty());

            // The estimated unlocking block height of the exiting node
            Long unlockBlockNum = stakingNode.getUnStakeEndBlock();
            historyStakingListResp.setUnlockBlockNum(unlockBlockNum);

            lists.add(historyStakingListResp);
        }
        Page<?> page = new Page<>(req.getPageNo(), req.getPageSize());
        page.setTotal(stakings.getTotal());
        respPage.init(page, lists);
        return respPage;
    }

    public BaseResp<StakingDetailsResp> stakingDetails(StakingDetailsReq req) {
        /**
         * First check whether the node is active. If it cannot be found, then check whether it has a history summary.
         */
        Node stakingNode = nodeMapper.selectByPrimaryKey(req.getNodeId());
        StakingDetailsResp resp = new StakingDetailsResp();
        // There is only one piece of data
        if (stakingNode != null) {
            BeanUtils.copyProperties(stakingNode, resp);
            resp.setIsInit(stakingNode.getIsInit() == 1);
            resp.setStatus(StakingStatusEnum.getCodeByStatus(stakingNode.getStatus(), stakingNode.getIsConsensus(), stakingNode.getIsSettle()));
            resp.setSlashLowQty(stakingNode.getStatSlashLowQty());
            resp.setSlashMultiQty(stakingNode.getStatSlashMultiQty());
            resp.setBlockQty(stakingNode.getStatBlockQty());
            resp.setExpectBlockQty(stakingNode.getStatExpectBlockQty());
            resp.setVerifierTime(stakingNode.getStatVerifierTime());
            resp.setJoinTime(stakingNode.getJoinTime().getTime());
            resp.setDenefitAddr(stakingNode.getBenefitAddr());
            Address denefitAddr = addressMapper.selectByPrimaryKey(stakingNode.getBenefitAddr());
            resp.setDenefitAddrType(CommonUtil.ofNullable(() -> denefitAddr.getType()).orElse(AddressTypeEnum.ACCOUNT.getCode()));
            Address stakingAddr = addressMapper.selectByPrimaryKey(stakingNode.getStakingAddr());
            resp.setStakingAddrType(CommonUtil.ofNullable(() -> stakingAddr.getType()).orElse(AddressTypeEnum.ACCOUNT.getCode()));
            resp.setStakingIcon(stakingNode.getNodeIcon());
            resp.setDeleAnnualizedRate(stakingNode.getDeleAnnualizedRate().toString());
            resp.setRewardPer(new BigDecimal(stakingNode.getRewardPer()).divide(Browser.PERCENTAGE).toString());
            resp.setNextRewardPer(new BigDecimal(stakingNode.getNextRewardPer()).divide(Browser.PERCENTAGE).toString());
            resp.setTotalDeleReward(stakingNode.getTotalDeleReward().add(stakingNode.getPreTotalDeleReward()));
            try {
                String nodeSettleStatisInfo = stakingNode.getNodeSettleStatisInfo();
                NodeSettleStatis nodeSettleStatis = NodeSettleStatis.jsonToBean(nodeSettleStatisInfo);
                NetworkStat networkStatRedis = statisticCacheService.getNetworkStatCache();
                BigInteger settleEpochRound = EpochUtil.getEpoch(BigInteger.valueOf(networkStatRedis.getCurNumber()),
                                                                 blockChainConfig.getSettlePeriodBlockCount());
                resp.setGenBlocksRate(nodeSettleStatis.computeGenBlocksRate(settleEpochRound));
            } catch (Exception e) {
                logger.error("Obtain the abnormal block production rate of the node in 24 hours", e);
            }
            resp.setVersion(ChainVersionUtil.toStringVersion(BigInteger.valueOf(stakingNode.getProgramVersion())));
            /**
             * The reward to be claimed is equal to the cumulative commission reward plus the previous round reward minus the commission reward that has been claimed
             */
            resp.setDeleRewardRed(stakingNode.getTotalDeleReward()
                                             .add(stakingNode.getPreTotalDeleReward())
                                             .subtract(stakingNode.getHaveDeleReward()));
            /** The annualized rate is calculated only if the node is not a built-in node.  */
            if (CustomStaking.YesNoEnum.YES.getCode() != stakingNode.getIsInit()) {
                resp.setExpectedIncome(String.valueOf(stakingNode.getAnnualizedRate()));
                resp.setRewardValue(stakingNode.getStatFeeRewardValue()
                                               .add(stakingNode.getStatBlockRewardValue())
                                               .add(stakingNode.getStatStakingRewardValue()));
                logger.info("Cumulative system reward [{}] = block reward statistics (handling fee) [{}] + block reward statistics (incentive pool) [{}] + pledge reward statistics (incentive pool) [{}]",
                            resp.getRewardValue(),
                            stakingNode.getStatFeeRewardValue(),
                            stakingNode.getStatBlockRewardValue(),
                            stakingNode.getStatStakingRewardValue());
            } else {
                resp.setRewardValue(stakingNode.getStatFeeRewardValue());
                logger.info("Cumulative system reward [{}] = block reward statistics (handling fee) [{}]", resp.getRewardValue(), stakingNode.getStatFeeRewardValue());
                resp.setExpectedIncome("");
            }
            String webSite = "";
            if (StringUtils.isNotBlank(stakingNode.getWebSite())) {
                /**
                 * If the address does not start with http, fill it in
                 */
                if (stakingNode.getWebSite().startsWith(Browser.HTTP) || stakingNode.getWebSite().startsWith(Browser.HTTPS)) {
                    webSite = stakingNode.getWebSite();
                } else {
                    webSite = Browser.HTTP + stakingNode.getWebSite();
                }
            }
            resp.setWebsite(webSite);
            /** The actual jump address is the name concatenated with the URL */
            if (StringUtils.isNotBlank(stakingNode.getExternalName())) {
                resp.setExternalUrl(blockChainConfig.getKeyBase() + stakingNode.getExternalName());
            } else {
                resp.setExternalUrl(blockChainConfig.getKeyBase());
            }
            if (stakingNode.getLeaveTime() != null) {
                resp.setLeaveTime(stakingNode.getLeaveTime().getTime());
            }
            // Valid Delegate amount
            resp.setDelegateValue(stakingNode.getStatDelegateValue());
            // Valid staking entrust total value
            resp.setTotalValue(stakingNode.getTotalValue());

            /**
             * If the judgment is true, it means checking historical data
             * If there is no value, it indicates that the active account is being queried.
             */
            if (stakingNode.getStatus().intValue() == StatusEnum.CANDIDATE.getCode()) {
                // The nodes in the candidate set the number of valid delegation addresses
                resp.setDelegateQty(stakingNode.getStatValidAddrs());
                /** staking amount = pledge (hesitation period) + pledge (lock-up period)  */
                BigDecimal stakingValue = stakingNode.getStakingHes().add(stakingNode.getStakingLocked());
                resp.setStakingValue(stakingValue);
            } else {
                // Other status nodes set the number of delegation addresses to be extracted.
                resp.setDelegateQty(stakingNode.getStatInvalidAddrs());
                /**
                 * If it is in settlement, set it directly to 0
                 */
                if (stakingNode.getIsSettle().intValue() == YesNoEnum.YES.getCode()) {
                    resp.setTotalValue(BigDecimal.ZERO);
                    resp.setStakingValue(BigDecimal.ZERO);
                } else {
                    if (stakingNode.getStatus().intValue() == StatusEnum.LOCKED.getCode()) {
                        resp.setStakingValue(stakingNode.getStakingLocked());
                    } else {
                        resp.setStakingValue(stakingNode.getStakingReduction());
                    }
                }
                // The delegate amount to be withdrawn(AAA)
                resp.setStatDelegateReduction(resp.getDelegateValue().add(stakingNode.getStatDelegateReleased()));
            }
        }
        return BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS), resp);
    }

    public RespPage<StakingOptRecordListResp> stakingOptRecordList(StakingOptRecordListReq req) {
        RespPage<StakingOptRecordListResp> respPage = new RespPage<>();
        ESQueryBuilderConstructor constructor = new ESQueryBuilderConstructor();
        constructor.must(new ESQueryBuilders().term("nodeId", req.getNodeId()));
        ESResult<NodeOpt> items = new ESResult<>();
        constructor.setDesc("id");
        try {
            items = ESNodeOptRepository.search(constructor, NodeOpt.class, req.getPageNo(), req.getPageSize());
        } catch (Exception e) {
            logger.error("Get node operation error。", e);
            return respPage;
        }
        List<NodeOpt> nodeOpts = items.getRsData();
        List<StakingOptRecordListResp> lists = new LinkedList<>();
        for (NodeOpt nodeOpt : nodeOpts) {
            StakingOptRecordListResp stakingOptRecordListResp = new StakingOptRecordListResp();
            BeanUtils.copyProperties(nodeOpt, stakingOptRecordListResp);
            stakingOptRecordListResp.setType(String.valueOf(nodeOpt.getType()));
            stakingOptRecordListResp.setTimestamp(nodeOpt.getTime().getTime());
            stakingOptRecordListResp.setBlockNumber(nodeOpt.getBNum());
            if (StringUtils.isNotBlank(nodeOpt.getDesc())) {
                String[] desces = nodeOpt.getDesc().split(Browser.OPT_SPILT);
                /** Return based on different types of combinations */
                switch (NodeOpt.TypeEnum.getEnum(String.valueOf(nodeOpt.getType()))) {
                    /**
                     *Modify validator
                     */
                    case MODIFY:
                        if (desces.length > 1) {
                            stakingOptRecordListResp.setBeforeRate(new BigDecimal(desces[0]).divide(Browser.PERCENTAGE).toString());
                            stakingOptRecordListResp.setAfterRate(new BigDecimal(desces[1]).divide(Browser.PERCENTAGE).toString());
                        }
                        break;
                    /** Proposal type */
                    case PROPOSALS:
                        Proposal proposal = proposalMapper.selectByPrimaryKey(nodeOpt.getTxHash());
                        if (ObjectUtil.isNotNull(proposal) && StrUtil.isNotBlank(proposal.getTopic())) {
                            String desc = StrUtil.replace(stakingOptRecordListResp.getDesc(), Browser.INQUIRY, proposal.getTopic());
                            nodeOpt.setDesc(desc);
                            stakingOptRecordListResp.setDesc(desc);
                            desces = nodeOpt.getDesc().split(Browser.OPT_SPILT);
                        }
                        stakingOptRecordListResp.setId(Browser.PIP_NAME + desces[0]);
                        stakingOptRecordListResp.setTitle(Browser.INQUIRY.equals(desces[1]) ? "" : desces[1]);
                        stakingOptRecordListResp.setProposalType(desces[2]);
                        if (desces.length > 3) {
                            stakingOptRecordListResp.setVersion(desces[3]);
                        }
                        break;
                    /** voting type */
                    case VOTE:
                        // The description is updated by a scheduled task, so each query must be re-queried to get the latest value.
                        CustomVoteProposal customVoteProposal = customVoteMapper.selectVotePropal(nodeOpt.getTxHash());
                        if (ObjectUtil.isNotNull(customVoteProposal) && StrUtil.isNotBlank(customVoteProposal.getTopic())) {
                            String desc = StrUtil.replace(stakingOptRecordListResp.getDesc(), Browser.INQUIRY, customVoteProposal.getTopic());
                            nodeOpt.setDesc(desc);
                            stakingOptRecordListResp.setDesc(desc);
                            desces = nodeOpt.getDesc().split(Browser.OPT_SPILT);
                        }
                        stakingOptRecordListResp.setTitle(Browser.INQUIRY.equals(desces[1]) ? "" : desces[1]);
                        stakingOptRecordListResp.setId(Browser.PIP_NAME + desces[0]);
                        stakingOptRecordListResp.setOption(desces[2]);
                        stakingOptRecordListResp.setProposalType(desces[3]);
                        if (desces.length > 4) {
                            stakingOptRecordListResp.setVersion(desces[4]);
                        }
                        break;
                    /** Double signature */
                    case MULTI_SIGN:
                        stakingOptRecordListResp.setPercent(desces[0]);
                        stakingOptRecordListResp.setAmount(new BigDecimal(desces[1]));
                        break;
                    /** Low block rate */
                    case LOW_BLOCK_RATE:
                        stakingOptRecordListResp.setPercent(desces[1]);
                        stakingOptRecordListResp.setAmount(new BigDecimal(desces[2]));
                        stakingOptRecordListResp.setIsFire(Integer.parseInt(desces[3]));
                        break;
                    /**
                     * Parameter proposal
                     */
                    case PARAMETER:
                        stakingOptRecordListResp.setId(Browser.PIP_NAME + desces[0]);
                        stakingOptRecordListResp.setTitle(Browser.INQUIRY.equals(desces[1]) ? "" : desces[1]);
                        stakingOptRecordListResp.setProposalType(desces[2]);
                        stakingOptRecordListResp.setType("4");
                        break;
                    /**
                     * Version Statement
                     */
                    case VERSION:
                        String v = desces[2];
                        if (StringUtils.isNotBlank(v)) {
                            v = ChainVersionUtil.toStringVersion(new BigInteger(v));
                        } else {
                            v = "0";
                        }
                        stakingOptRecordListResp.setVersion(v);
                        stakingOptRecordListResp.setType("12");
                        break;
                    default:
                        break;
                }
            }

            lists.add(stakingOptRecordListResp);
        }
        /** Query the total number of pages */
        Page<?> page = new Page<>(req.getPageNo(), req.getPageSize());
        page.setTotal(items.getTotal());
        respPage.init(page, lists);
        return respPage;
    }

    public RespPage<DelegationListByStakingResp> delegationListByStaking(DelegationListByStakingReq req) {
        Node node = nodeMapper.selectByPrimaryKey(req.getNodeId());
        PageHelper.startPage(req.getPageNo(), req.getPageSize());
        List<DelegationListByStakingResp> lists = new LinkedList<>();
        /** Query and verify delegation information based on node id and block */
        Page<DelegationStaking> delegationStakings = customDelegationMapper.selectStakingByNodeId(req.getNodeId());
        for (DelegationStaking delegationStaking : delegationStakings.getResult()) {
            DelegationListByStakingResp byStakingResp = new DelegationListByStakingResp();
            BeanUtils.copyProperties(delegationStaking, byStakingResp);
            byStakingResp.setDelegateAddr(delegationStaking.getDelegateAddr());
            /**Locked delegation (TURN) is displayed normally if the associated validator status is normal, and zero if otherwise (delegation)  */
            byStakingResp.setDelegateTotalValue(node.getStatDelegateValue());
            /**
             * The commission amount is equal to has plus the actual lock amount
             */
            BigDecimal delValue = delegationStaking.getDelegateHes().add(delegationStaking.getDelegateLocked());
            byStakingResp.setDelegateValue(delValue);
            lists.add(byStakingResp);
        }
        /** Total number of pagination statistics */
        Page<?> page = new Page<>(req.getPageNo(), req.getPageSize());
        page.setTotal(delegationStakings.getTotal());
        RespPage<DelegationListByStakingResp> respPage = new RespPage<>();
        respPage.init(page, lists);
        return respPage;
    }

    public RespPage<DelegationListByAddressResp> delegationListByAddress(DelegationListByAddressReq req) {
        PageHelper.startPage(req.getPageNo(), req.getPageSize());
        List<DelegationListByAddressResp> lists = new LinkedList<>();
        /** Query the commission list by paging according to the address */
        Page<DelegationAddress> delegationAddresses = customDelegationMapper.selectAddressByAddr(req.getAddress());
        /**
         * Initialize the reward node ID list, which is used to subsequently query the corresponding rewards to be received.
         */
        List<String> nodes = new ArrayList<>();
        for (DelegationAddress delegationAddress : delegationAddresses.getResult()) {
            nodes.add(delegationAddress.getNodeId());
        }
        /**
         * Get the rewards to be claimed for each node
         */
        List<Reward> rewards = new ArrayList<>();
        try {
            rewards = turnClient.getRewardContract().getDelegateReward(req.getAddress(), nodes).send().getData();
        } catch (Exception e) {
            logger.error("Error getting reward data：{}", e.getMessage());
            rewards = new ArrayList<>();
        }
        for (DelegationAddress delegationAddress : delegationAddresses.getResult()) {
            DelegationListByAddressResp byAddressResp = new DelegationListByAddressResp();
            BeanUtils.copyProperties(delegationAddress, byAddressResp);
            byAddressResp.setDelegateHas(delegationAddress.getDelegateHes());
            /** Commission amount = hesitation period amount plus lock-in period amount */
            BigDecimal deleValue = delegationAddress.getDelegateHes().add(byAddressResp.getDelegateLocked());
            byAddressResp.setDelegateValue(deleValue);
            byAddressResp.setDelegateUnlock(delegationAddress.getDelegateHes());
            /**
             * Cycling to get rewards
             */
            if (rewards != null) {
                for (Reward reward : rewards) {
                    /**
                     * Set the amount after successful matching
                     */
                    if (delegationAddress.getNodeId().equals(HexUtil.prefix(reward.getNodeId()))) {
                        byAddressResp.setDelegateClaim(new BigDecimal(reward.getReward()));
                    }
                }
            }
            lists.add(byAddressResp);
        }
        /** Total statistics */
        Page<?> page = new Page<>(req.getPageNo(), req.getPageSize());
        page.setTotal(delegationAddresses.getTotal());
        RespPage<DelegationListByAddressResp> respPage = new RespPage<>();
        respPage.init(page, lists);
        return respPage;
    }

    public RespPage<LockedStakingListResp> lockedStakingList(LockedStakingListReq req) {
        PageHelper.startPage(req.getPageNo(), req.getPageSize());
        RespPage<LockedStakingListResp> respPage = new RespPage<>();
        List<LockedStakingListResp> lists = new LinkedList<>();
        NodeExample nodeExample = new NodeExample();
        nodeExample.setOrderByClause(" leave_time desc");
        NodeExample.Criteria criteria = nodeExample.createCriteria();
        criteria.andStatusEqualTo(StatusEnum.LOCKED.getCode());

        if (StringUtils.isNotBlank(req.getKey())) {
            criteria.andNodeNameLike("%" + req.getKey() + "%");
        }
        Page<Node> stakingPage = customNodeMapper.selectListByExample(nodeExample);

        /** Query the block producing node */
        //NetworkStat networkStatRedis = this.statisticCacheService.getNetworkStatCache();
        int i = (req.getPageNo() - 1) * req.getPageSize();
        for (Node node : stakingPage) {
            LockedStakingListResp lockedStakingListResp = new LockedStakingListResp();
            BeanUtils.copyProperties(node, lockedStakingListResp);
            lockedStakingListResp.setBlockQty(node.getStatBlockQty());
            lockedStakingListResp.setDelegateQty(node.getStatValidAddrs());
            lockedStakingListResp.setExpectedIncome(node.getAnnualizedRate().toString());
            /** The total amount of entrusted transactions = the total amount of entrusted transactions (amount during the hesitation period) + the total amount of entrusted transactions (amount during the lock-in period) */
            String sumAmount = node.getStatDelegateValue().toString();
            lockedStakingListResp.setDelegateValue(sumAmount);
            lockedStakingListResp.setIsInit(node.getIsInit() == 1);
            lockedStakingListResp.setStakingIcon(node.getNodeIcon());
            if (node.getIsRecommend() != null) {
                lockedStakingListResp.setIsRecommend(CustomStaking.YesNoEnum.YES.getCode() == node.getIsRecommend());
            }

            lockedStakingListResp.setRanking(i + 1);
            lockedStakingListResp.setSlashLowQty(node.getStatSlashLowQty());
            lockedStakingListResp.setSlashMultiQty(node.getStatSlashMultiQty());
            lockedStakingListResp.setStatus(StakingStatusEnum.LOCKED.getCode());
            Date leaveTime = node.getLeaveTime();
            lockedStakingListResp.setLeaveTime(leaveTime == null ? null : leaveTime.getTime());
            /** Total number of pledges = valid pledges + delegation */
            lockedStakingListResp.setTotalValue(node.getTotalValue().toString());
            lockedStakingListResp.setDeleAnnualizedRate(node.getDeleAnnualizedRate().toString());

            // Estimated unlocked block height occupied by locked nodes = (the settlement period when the node is locked + the number of locked settlement periods) x the number of blocks in each settlement period
            int epoches = node.getZeroProduceFreezeEpoch() + node.getZeroProduceFreezeDuration();
            BigInteger unlockBlockNum = blockChainConfig.getSettlePeriodBlockCount().multiply(BigInteger.valueOf(epoches));
            lockedStakingListResp.setUnlockBlockNum(unlockBlockNum.longValue());

            lists.add(lockedStakingListResp);
            i++;
        }
        Page<?> page = new Page<>(req.getPageNo(), req.getPageSize());
        page.setTotal(stakingPage.getTotal());
        respPage.init(page, lists);
        return respPage;
    }

}
