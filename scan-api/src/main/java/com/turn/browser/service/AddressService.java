package com.turn.browser.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.turn.browser.bean.CustomAddressDetail;
import com.turn.browser.bean.DlLock;
import com.turn.browser.bean.LockDelegate;
import com.turn.browser.bean.RestrictingBalance;
import com.turn.browser.client.TurnClient;
import com.turn.browser.client.SpecialApi;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.CustomAddressMapper;
import com.turn.browser.dao.custommapper.CustomRpPlanMapper;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.dao.entity.RpPlan;
import com.turn.browser.dao.entity.RpPlanExample;
import com.turn.browser.dao.mapper.RpPlanMapper;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.TokenTypeEnum;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.request.address.QueryDetailRequest;
import com.turn.browser.request.address.QueryRPPlanDetailRequest;
import com.turn.browser.response.address.DetailsRPPlanResp;
import com.turn.browser.response.address.QueryDetailResp;
import com.turn.browser.response.address.QueryRPPlanDetailResp;
import com.turn.browser.service.elasticsearch.EsBlockRepository;
import com.turn.browser.utils.ConvertUtil;
import com.turn.browser.utils.I18nUtil;
import com.bubble.contracts.dpos.RestrictingPlanContract;
import com.bubble.contracts.dpos.dto.CallResponse;
import com.bubble.contracts.dpos.dto.resp.RestrictingItem;
import com.bubble.contracts.dpos.dto.resp.Reward;
import com.bubble.protocol.core.DefaultBlockParameterName;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Address specific logic implementation method
 */
@Service
public class AddressService {

    private final Logger logger = LoggerFactory.getLogger(AddressService.class);

    @Resource
    private CustomAddressMapper customAddressMapper;

    @Resource
    private RpPlanMapper rpPlanMapper;

    @Resource
    private CustomRpPlanMapper customRpPlanMapper;

    @Resource
    private TurnClient turnClient;

    @Resource
    private I18nUtil i18n;

    @Resource
    private BlockChainConfig blockChainConfig;

    @Resource
    private EsBlockRepository esBlockRepository;

    @Resource
    private SpecialApi specialApi;

    @Resource
    private StatisticCacheService statisticCacheService;

    /**
     * Check address details
     *
     * @param req
     * @return com.turn.browser.response.address.QueryDetailResp
     */
    public QueryDetailResp getDetails(QueryDetailRequest req) {
        QueryDetailResp resp = new QueryDetailResp();
        // If you query the 0 address, return directly
        if (StrUtil.isNotBlank(req.getAddress()) && com.turn.browser.utils.AddressUtil.isAddrZero(req.getAddress())) {
            return resp;
        }
        /** Query address information based on primary key */
        CustomAddressDetail item = customAddressMapper.findAddressDetail(req.getAddress());
        if (item != null) {
            if (TokenTypeEnum.ERC20.getType().equalsIgnoreCase(item.getTokenType())) {
                resp.setHasErc20(true);
            } else if (TokenTypeEnum.ERC721.getType().equalsIgnoreCase(item.getTokenType())) {
                resp.setHasErc721(true);
            } else if (TokenTypeEnum.ERC1155.getType().equalsIgnoreCase(item.getTokenType())) {
                resp.setHasErc1155(true);
            }
            BeanUtils.copyProperties(item, resp);
            resp.setDelegateUnlock(item.getDelegateHes());
            /** Pre-set whether to display lock-up */
            resp.setIsRestricting(0);
            resp.setStakingValue(item.getStakingValue().add(item.getDelegateValue()));
            resp.setIsDestroy(StringUtils.isBlank(item.getContractDestroyHash()) ? 0 : 1);
            resp.setContractCreateHash(item.getContractCreatehash());
            resp.setDestroyHash(item.getContractDestroyHash());
            resp.setContractName(ConvertUtil.captureName(item.getContractName()));
        }
        /** Special account balance direct inquiry chain  */
        try {
            this.getAddressInfo(req, resp);
        } catch (Exception e) {
            logger.error("getBalance error", e);
            turnClient.updateCurrentWeb3jWrapper();
            try {
                this.getAddressInfo(req, resp);
            } catch (Exception e1) {
                logger.error("getBalance error again", e);
            }
        }
        RpPlanExample rpPlanExample = new RpPlanExample();
        RpPlanExample.Criteria criteria = rpPlanExample.createCriteria();
        criteria.andAddressEqualTo(req.getAddress());
        List<RpPlan> rpPlans = rpPlanMapper.selectByExample(rpPlanExample);
        /** Once you have lock-in data, you can return 1 */
        if (rpPlans != null && !rpPlans.isEmpty()) {
            resp.setIsRestricting(1);
        }
        List<LockDelegate> lockDelegateList = new ArrayList<>();
        try {
            List<RestrictingBalance> restrictingBalances = specialApi.getRestrictingBalance(turnClient.getWeb3jWrapper().getWeb3j(), req.getAddress());
            // Unfrozen entrust amount/entrust to be withdrawn
            BigDecimal unLockBalance = BigDecimal.ZERO;
            // Unfrozen entrust amount/entrust to be redeemed
            BigDecimal lockBalance = BigDecimal.ZERO;
            if (CollUtil.isNotEmpty(restrictingBalances)) {
                unLockBalance = new BigDecimal(restrictingBalances.get(0).getDlFreeBalance().add(restrictingBalances.get(0).getDlRestrictingBalance()));
                unLockBalance = ConvertUtil.convertByFactor(unLockBalance, 18);
                if (CollUtil.isNotEmpty(restrictingBalances.get(0).getDlLocks())) {
                    NetworkStat networkStat = statisticCacheService.getNetworkStatCache();
                    Block block = null;
                    try {
                        block = esBlockRepository.get(String.valueOf(networkStat.getCurNumber()), Block.class);
                    } catch (Exception e) {
                        logger.error("Get block error。", e);
                    }
                    for (DlLock dlLock : restrictingBalances.get(0).getDlLocks()) {
                        BigDecimal accLockBalance = new BigDecimal(dlLock.getFreeBalance().add(dlLock.getLockBalance()));
                        accLockBalance = ConvertUtil.convertByFactor(accLockBalance, 18);
                        lockBalance = lockBalance.add(accLockBalance);
                        LockDelegate lockDelegate = new LockDelegate();
                        lockDelegate.setBlockNum(dlLock.getEpoch().multiply(blockChainConfig.getSettlePeriodBlockCount()));
                        // Estimated time: expected block height minus current block height multiplied by block time plus block time
                        BigDecimal diff = new BigDecimal(lockDelegate.getBlockNum().subtract(BigInteger.valueOf(networkStat.getCurNumber())));
                        if (block != null) {
                            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                                lockDelegate.setDate(new BigDecimal(networkStat.getAvgPackTime()).multiply(diff).add(BigDecimal.valueOf(block.getTime().getTime())).longValue());
                            } else {
                                lockDelegate.setDate(block.getTime().getTime());
                            }
                        }
                        lockDelegate.setLock(accLockBalance.toPlainString());
                        lockDelegateList.add(lockDelegate);
                    }
                }
                resp.setLockBalance(lockBalance.toPlainString());
                resp.setUnLockBalance(unLockBalance.toPlainString());
            }
        } catch (Exception e) {
            logger.error("Getting frozen delegate exception", e);
        }
        resp.setLockDelegateList(lockDelegateList);
        return resp;
    }

    /**
     * Address lock details
     *
     * @param req
     * @return com.turn.browser.response.address.QueryRPPlanDetailResp
     */
    public QueryRPPlanDetailResp rpplanDetail(QueryRPPlanDetailRequest req) {
        QueryRPPlanDetailResp queryRPPlanDetailResp = new QueryRPPlanDetailResp();
        try {
            // 锁仓可用余额查询特殊节点接口
            List<RestrictingBalance> restrictingBalances = specialApi.getRestrictingBalance(turnClient.getWeb3jWrapper().getWeb3j(), req.getAddress());
            if (restrictingBalances != null && !restrictingBalances.isEmpty()) {
                /**
                 * The available balance is balance minus the pledge amount
                 */
                queryRPPlanDetailResp.setRestrictingBalance(new BigDecimal(restrictingBalances.get(0).getLockBalance().subtract(restrictingBalances.get(0).getPledgeBalance())));
            }
            // Check the pledged amount and the amount to be released to lock the contract
            RestrictingPlanContract restrictingPlanContract = turnClient.getRestrictingPlanContract();
            CallResponse<RestrictingItem> baseResponse = restrictingPlanContract.getRestrictingInfo(req.getAddress()).send();
            if (baseResponse.isStatusOk()) {
                queryRPPlanDetailResp.setStakingValue(new BigDecimal(baseResponse.getData().getPledge()));
                queryRPPlanDetailResp.setUnderReleaseValue(new BigDecimal(baseResponse.getData().getDebt()));
            }
        } catch (Exception e) {
            logger.error("rpplanDetail error", e);
            throw new BusinessException(i18n.i(I18nEnum.SYSTEM_EXCEPTION));
        }
        /**
         * Query the corresponding lock-up plan by page
         */
        RpPlanExample rpPlanExample = new RpPlanExample();
        RpPlanExample.Criteria criteria = rpPlanExample.createCriteria();
        criteria.andAddressEqualTo(req.getAddress());
        List<DetailsRPPlanResp> detailsRPPlanResps = new ArrayList<>();
        PageHelper.startPage(req.getPageNo(), req.getPageSize());
        Page<RpPlan> rpPlans = rpPlanMapper.selectByExample(rpPlanExample);
        for (RpPlan rPlan : rpPlans) {
            DetailsRPPlanResp detailsRPPlanResp = new DetailsRPPlanResp();
            BeanUtils.copyProperties(rPlan, detailsRPPlanResp);
            /**
             * The lock-up period corresponds to the number of settlement periods * epoch + number. If it is not an integer multiple, it will be: settlement period * (epoch-1) + excess number
             */
            BigInteger number;
            long remainder = rPlan.getNumber() % blockChainConfig.getSettlePeriodBlockCount().longValue();
            if (remainder == 0L) {
                number = blockChainConfig.getSettlePeriodBlockCount().multiply(rPlan.getEpoch()).add(BigInteger.valueOf(rPlan.getNumber()));
            } else {
                number = blockChainConfig.getSettlePeriodBlockCount()
                                         .multiply(rPlan.getEpoch().subtract(BigInteger.ONE))
                                         .add(BigInteger.valueOf(rPlan.getNumber()))
                                         .add(blockChainConfig.getSettlePeriodBlockCount().subtract(BigInteger.valueOf(remainder)));
            }

            detailsRPPlanResp.setBlockNumber(number.toString());
            /** Estimated time: expected block height minus current block height multiplied by block time plus block time */
            Block block = null;
            try {
                block = esBlockRepository.get(String.valueOf(rPlan.getNumber()), Block.class);
            } catch (IOException e) {
                logger.error("Get block error。", e);
            }
            BigDecimal diff = new BigDecimal(number.subtract(BigInteger.valueOf(rPlan.getNumber())));
            if (block != null) {
                if (diff.compareTo(BigDecimal.ZERO) > 0) {
                    NetworkStat networkStat = statisticCacheService.getNetworkStatCache();
                    detailsRPPlanResp.setEstimateTime(new BigDecimal(networkStat.getAvgPackTime()).multiply(diff).add(BigDecimal.valueOf(block.getTime().getTime())).longValue());
                } else {
                    detailsRPPlanResp.setEstimateTime(block.getTime().getTime());
                }
            }
            detailsRPPlanResps.add(detailsRPPlanResp);
        }
        queryRPPlanDetailResp.setRpPlans(detailsRPPlanResps);
        /**
         * Get calculated total
         */
        BigDecimal bigDecimal = customRpPlanMapper.selectSumByAddress(req.getAddress());
        if (bigDecimal != null) {
            queryRPPlanDetailResp.setTotalValue(bigDecimal);
        }
        /**
         * Get the total number of lists
         */
        queryRPPlanDetailResp.setTotal(rpPlans.getTotal());
        return queryRPPlanDetailResp;
    }

    /**
     * Query the balance of special accounts from the chain
     *
     * @param req
     * @param resp
     * @return com.turn.browser.response.address.QueryDetailResp
     */
    private QueryDetailResp getAddressInfo(QueryDetailRequest req, QueryDetailResp resp) throws Exception {
        List<RestrictingBalance> restrictingBalances = specialApi.getRestrictingBalance(turnClient.getWeb3jWrapper().getWeb3j(), req.getAddress());
        if (restrictingBalances != null && !restrictingBalances.isEmpty()) {
            resp.setBalance(new BigDecimal(restrictingBalances.get(0).getFreeBalance()));
            resp.setRestrictingBalance(new BigDecimal(restrictingBalances.get(0).getLockBalance().subtract(restrictingBalances.get(0).getPledgeBalance())));
        }
        /** Special account balance direct inquiry chain  */
        if (resp.getBalance().compareTo(BigDecimal.valueOf(10000000000L)) > 0) {
            BigInteger balance = turnClient.getWeb3jWrapper().getWeb3j().bubbleGetBalance(req.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance();
            resp.setBalance(new BigDecimal(balance));
        }
        /**
         * Query all transaction amounts and summarize them
         */
        List<String> nodes = new ArrayList<>();
        List<Reward> rewards = turnClient.getRewardContract().getDelegateReward(req.getAddress(), nodes).send().getData();
        /**
         * Return directly when the reward is empty
         */
        if (rewards == null) {
            return resp;
        }
        BigDecimal allRewards = BigDecimal.ZERO;
        for (Reward reward : rewards) {
            allRewards = allRewards.add(new BigDecimal(reward.getReward()));
        }
        resp.setDelegateClaim(allRewards);
        return resp;
    }

}
