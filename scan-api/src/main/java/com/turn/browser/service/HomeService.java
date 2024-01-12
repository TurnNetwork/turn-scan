package com.turn.browser.service;

import com.turn.browser.bean.CustomStaking;
import com.turn.browser.bean.StakingBO;
import com.turn.browser.bean.SubChainTx;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.CustomNodeMapper;
import com.turn.browser.dao.entity.Address;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.dao.entity.Node;
import com.turn.browser.dao.entity.NodeExample;
import com.turn.browser.dao.mapper.AddressMapper;
import com.turn.browser.dao.mapper.NodeMapper;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.request.home.QueryNavigationRequest;
import com.turn.browser.response.home.*;
import com.turn.browser.service.elasticsearch.EsBlockRepository;
import com.turn.browser.service.elasticsearch.EsSubChainTxRepository;
import com.turn.browser.service.elasticsearch.EsTransactionRepository;
import com.turn.browser.service.elasticsearch.bean.ESResult;
import com.turn.browser.service.elasticsearch.query.ESQueryBuilderConstructor;
import com.turn.browser.service.elasticsearch.query.ESQueryBuilders;
import com.turn.browser.utils.CommonUtil;
import com.turn.browser.utils.HexUtil;
import com.turn.browser.utils.NetworkParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Specific implementation method of homepage interface logic
 */
@Slf4j
@Service
public class HomeService {

    @Resource
    private EsBlockRepository ESBlockRepository;

    @Resource
    private EsTransactionRepository ESTransactionRepository;

    @Resource
    private StatisticCacheService statisticCacheService;

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private AddressMapper addressMapper;

    @Resource
    private BlockChainConfig blockChainConfig;

    @Resource
    private CommonService commonService;

    @Resource
    private CustomNodeMapper customNodeMapper;

    @Resource
    private EsSubChainTxRepository esSubChainTxRepository;

    private final static String BLOCK_ERR_TIPS = "Get block error。";

    /**
     * Record refresh value
     */
    private static Integer consensusNum = 0;

    /**
     * Record the latest block height
     */
    private static Long newBlockNum = 0L;

    private static final String STAKING_TYPE = "staking";

    private static final String BLOCK_TYPE = "block";

    private static final String ADDRESS_TYPE = "address";

    private static final String CONTACT_TYPE = "contract";

    private static final String TRANSACTION_TYPE = "transaction";

    public QueryNavigationResp queryNavigation(QueryNavigationRequest req) {
        /* Both internal and external accounts of Ethereum are 20 bytes, starting with 0x, string length 40, plus 0x, [external account - wallet address, internal account - contract address]
         *Ethereum block hash and transaction hash both start with 0x and have a length of 33
         * 1. Determine whether it is block high
         * 2. Determine whether it is an address
         * 3. If it is not the above two situations, it is the transaction hash or block hash. You need to query both.
         */
        req.setParameter(req.getParameter().trim());
        String keyword = req.getParameter();
        /* Determine whether it is a pure number */
        boolean isNumber = keyword.matches("[0-9]+");
        QueryNavigationResp result = new QueryNavigationResp();
        QueryNavigationStructResp queryNavigationStructResp = new QueryNavigationStructResp();
        if (isNumber) {
            Long number;
            try {
                /* If the conversion fails and exceeds the numerical range of long, it is considered an invalid block number.  */
                number = Long.valueOf(keyword);
            } catch (Exception e) {
                return result;
            }
            /* If block information exists, the block number is returned. */
            Block block = null;
            try {
                block = ESBlockRepository.get(String.valueOf(number), Block.class);
            } catch (IOException e) {
                log.error(BLOCK_ERR_TIPS, e);
            }
            if (block != null) {
                result.setType(BLOCK_TYPE);
                queryNavigationStructResp.setNumber(number);
            }
        } else {
            if (keyword.length() == 128) {
                /* Determine as node ID */
                Node node = nodeMapper.selectByPrimaryKey(HexUtil.prefix(keyword.toLowerCase()));
                if (node != null) {
                    result.setType(STAKING_TYPE);
                    queryNavigationStructResp.setNodeId(HexUtil.prefix(node.getNodeId()));
                }
            }
            if (keyword.startsWith("0x")) {
                if (keyword.length() == 42) {
                    /* Determined to be a contract or account address */
                    Address address = addressMapper.selectByPrimaryKey(keyword);
                    if (address != null && address.getType() != 1) {
                        result.setType(CONTACT_TYPE);
                    } else {
                        result.setType(ADDRESS_TYPE);
                    }
                    queryNavigationStructResp.setAddress(keyword);
                }
                if (keyword.length() == 130) {
                    /* Determine as node ID */
                    Node node = nodeMapper.selectByPrimaryKey(keyword);
                    if (node != null) {
                        result.setType(STAKING_TYPE);
                        queryNavigationStructResp.setNodeId(node.getNodeId());
                    }
                }
                if (keyword.length() == 66) {
                    /*
                     * Logical analysis of transaction hash or block hash 1. Query completed transactions first 2. Query completed transactions if there is no record, then query the block
                     * 4. If there is no record of the above, an empty result will be returned.
                     */
                    keyword = keyword.toLowerCase();
                    Transaction items = null;
                    try {
                        items = ESTransactionRepository.get(keyword, Transaction.class);
                    } catch (IOException e) {
                        log.error(BLOCK_ERR_TIPS, e);
                    }
                    if (items != null) {
                        result.setType(TRANSACTION_TYPE);
                        queryNavigationStructResp.setTxHash(keyword);
                    } else {
                        log.debug("The transaction record with Hash [{}] cannot be queried in the transaction table. Try to query the block information with Hash [{}]...", keyword, keyword);

                        ESQueryBuilderConstructor blockConstructor = new ESQueryBuilderConstructor();
                        blockConstructor.must(new ESQueryBuilders().term("hash", keyword));
                        ESResult<Block> blockList = new ESResult<>();
                        try {
                            blockList = ESBlockRepository.search(blockConstructor, Block.class, 1, 1);
                        } catch (IOException e) {
                            log.error(BLOCK_ERR_TIPS, e);
                        }
                        if (blockList.getTotal() > 0l) {
                            /* If block information is found, construct the result and return  */
                            result.setType(BLOCK_TYPE);
                            queryNavigationStructResp.setNumber(blockList.getRsData().get(0).getNum());
                        }
                    }
                }
            } else {
                /* If it does not start with 0x, the node information will be queried by default. */
                NodeExample nodeExample = new NodeExample();
                NodeExample.Criteria criteria = nodeExample.createCriteria();
                criteria.andNodeNameEqualTo(keyword);
                List<Node> nodes = nodeMapper.selectByExample(nodeExample);
                if (!nodes.isEmpty()) {
                    result.setType(STAKING_TYPE);
                    queryNavigationStructResp.setNodeId(nodes.get(0).getNodeId());
                }
            }
        }
        result.setStruct(queryNavigationStructResp);
        return result;
    }

    public BlockStatisticNewResp blockStatisticNew() {
        /*------------ Assemble chart data ------------*/
        List<Block> items = statisticCacheService.getBlockCache(0, 32);
        BlockStatisticNewResp blockStatisticNewResp = new BlockStatisticNewResp();
        if (items.isEmpty()) {
            return blockStatisticNewResp;
        }
        /* Query 32 items, the block generation time needs to be deducted, so the size is subtracted by 2 */
        Long[] x = new Long[items.size() - 2];
        Double[] ya = new Double[items.size() - 2];
        Long[] yb = new Long[items.size() - 2];
        for (int i = 0; i < items.size() - 1; i++) {
            Block currentBlock = items.get(i);
            if (i < items.size() - 2) {
                /* The last deduction does not require corresponding settings */
                x[i] = currentBlock.getNum();
                /* If the number of block transactions is equal to null, the transaction is considered empty. */
                if (currentBlock.getTxQty() == null) {
                    yb[i] = 0L;
                } else {
                    yb[i] = Long.valueOf(currentBlock.getTxQty());
                }
            }
            /* The first block does not need to calculate the block time */
            if (i == 0) continue;
            Block previousBlock = items.get(i - 1);
            BigDecimal sec = BigDecimal.valueOf(previousBlock.getTime().getTime() - currentBlock.getTime().getTime()).divide(BigDecimal.valueOf(1000), 4, RoundingMode.FLOOR);
            ya[i - 1] = sec.doubleValue();
        }
        blockStatisticNewResp.setX(x);
        blockStatisticNewResp.setYa(ya);
        blockStatisticNewResp.setYb(yb);
        return blockStatisticNewResp;
    }

    public ChainStatisticNewResp chainStatisticNew() {
        NetworkStat networkStatRedis = statisticCacheService.getNetworkStatCache();
        ChainStatisticNewResp chainStatisticNewResp = new ChainStatisticNewResp();
        if (networkStatRedis == null) return chainStatisticNewResp;
        /* Query redis statistics and convert the corresponding returned objects */
        BeanUtils.copyProperties(networkStatRedis, chainStatisticNewResp);
        chainStatisticNewResp.setCurrentTps(networkStatRedis.getCurTps());
        chainStatisticNewResp.setCurrentNumber(networkStatRedis.getCurNumber());
        Long bNumber = networkStatRedis.getCurNumber();
        /* Query the latest eight block information cached */
        List<Block> items = statisticCacheService.getBlockCache(0, 8);
        if (!items.isEmpty()) {
            /*
             * If the statistical block is smaller than the block transaction, re-query the new block.
             */
            Long dValue = items.get(0).getNum() - bNumber;
            if (dValue > 0) {
                items = statisticCacheService.getBlockCacheByStartEnd(dValue, dValue + 8);
            }
            if (dValue < 0) {
                chainStatisticNewResp.setCurrentNumber(items.get(0).getNum());
            }
        }

        int nodeNum = customNodeMapper.selectCountByActive();
        chainStatisticNewResp.setNodeNum(nodeNum);
        List<BlockListNewResp> lists = new LinkedList<>();
        for (int i = 0; i < items.size(); i++) {
            BlockListNewResp blockListNewResp = new BlockListNewResp();
            BeanUtils.copyProperties(items.get(i), blockListNewResp);
            blockListNewResp.setNodeId(items.get(i).getNodeId());
            blockListNewResp.setNumber(items.get(i).getNum());
            blockListNewResp.setStatTxQty(items.get(i).getTxQty());
            blockListNewResp.setServerTime(new Date().getTime());
            blockListNewResp.setTimestamp(items.get(i).getTime().getTime());
            blockListNewResp.setIsRefresh(true);
            blockListNewResp.setNodeName(commonService.getNodeName(items.get(i).getNodeId(), null));
            /*
             * The first block needs to be recorded in the cache and then compared
             * Set to false if the block is not growing
             */
            if (i == 0) {
                log.debug("newBlockNum:{},item number:{},isFresh:{}", newBlockNum, items.get(i).getNum(), blockListNewResp.getIsRefresh());
                if (items.get(i).getNum().longValue() != newBlockNum.longValue()) {
                    newBlockNum = items.get(i).getNum();
                } else {
                    blockListNewResp.setIsRefresh(false);
                }
            }
            lists.add(blockListNewResp);
        }
        chainStatisticNewResp.setBlockList(lists);
        BigDecimal issueValue = networkStatRedis.getIssueValue();
        chainStatisticNewResp.setIssueValue(issueValue.abs());
        BigDecimal circulationValue = CommonUtil.ofNullable(() -> networkStatRedis.getTurnValue()).orElse(BigDecimal.ZERO);
        chainStatisticNewResp.setTurnValue(circulationValue);
        StakingBO bo = commonService.getTotalStakingValueAndStakingDenominator(networkStatRedis);
        chainStatisticNewResp.setStakingDenominator(bo.getStakingDenominator());
        chainStatisticNewResp.setStakingDelegationValue(bo.getTotalStakingValue());

        ESQueryBuilderConstructor constructor = new ESQueryBuilderConstructor();
        ESResult<?> count;

        try {
            count = esSubChainTxRepository.Count(constructor);
            chainStatisticNewResp.setSubChainTxQty(count.getTotal());
        } catch (Exception e) {
            chainStatisticNewResp.setSubChainTxQty(0l);
        }
        return chainStatisticNewResp;
    }


    public StakingListNewResp stakingListNew() {
        StakingListNewResp stakingListNewResp = new StakingListNewResp();
        stakingListNewResp.setIsRefresh(false);
        NetworkStat networkStatRedis = statisticCacheService.getNetworkStatCache();
        /* Divide the current block by the consensus block to calculate which round */
        BigDecimal num = new BigDecimal(networkStatRedis.getCurNumber()).divide(new BigDecimal(blockChainConfig.getConsensusPeriodBlockCount()), 0, RoundingMode.UP);
        if (num.intValue() > consensusNum) {
            /* If the number of existing consensus rounds is greater than the storage round, the entire amount will be refreshed.  */
            stakingListNewResp.setIsRefresh(true);
            consensusNum = num.intValue();
        }
        /* Only query active nodes and return them in reverse order */
        NodeExample nodeExample = new NodeExample();
        NodeExample.Criteria criteria = nodeExample.createCriteria();
        criteria.andStatusEqualTo(CustomStaking.StatusEnum.CANDIDATE.getCode()).andIsConsensusEqualTo(CustomStaking.YesNoEnum.YES.getCode());
        nodeExample.setOrderByClause(" big_version desc,total_value desc,staking_block_num asc ,staking_tx_index asc");
        List<Node> nodes = nodeMapper.selectByExample(nodeExample);

        List<StakingListResp> lists = new LinkedList<>();
        for (int i = 0; i < nodes.size(); i++) {
            StakingListResp stakingListResp = new StakingListResp();
            BeanUtils.copyProperties(nodes.get(i), stakingListResp);
            stakingListResp.setIsInit(nodes.get(i).getIsInit() == 1);
            stakingListResp.setStakingIcon(nodes.get(i).getNodeIcon());
            /* The annualized rate is calculated only if the node is not a built-in node.  */
            if (CustomStaking.YesNoEnum.YES.getCode() != nodes.get(i).getIsInit()) {
                stakingListResp.setExpectedIncome(nodes.get(i).getAnnualizedRate().toString() + "%");
            } else {
                stakingListResp.setExpectedIncome("");
            }
            /* Total number of pledges = valid pledges + delegation */
            BigDecimal totalValue = nodes.get(i).getStakingHes().add(nodes.get(i).getStakingLocked()).add(nodes.get(i).getStatDelegateValue());
            stakingListResp.setTotalValue(totalValue);
            stakingListResp.setRanking(i + 1);
            lists.add(stakingListResp);
        }
        stakingListNewResp.setDataList(lists);
        return stakingListNewResp;
    }

}
