package com.turn.browser.service.epoch;

import com.bubble.contracts.dpos.dto.resp.Node;
import com.turn.browser.bean.EpochMessage;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.entity.BlockNode;
import com.turn.browser.dao.custommapper.CustomBlockNodeMapper;
import com.turn.browser.dao.mapper.NodeMapper;
import com.turn.browser.exception.BlockNumberException;
import com.turn.browser.utils.EpochUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This type of non-thread safety
 * Periodic switching service
 *
 * 1. Calculate cycle switching related values based on block number: Name/Meaning Variable name Current block number currentBlockNumber Current consensus cycle number consensusEpochRound Current settlement cycle number settleEpochRound
 * Current settlement cycle number issueEpochRound
 */
@Slf4j
@Service
public class EpochService {
    @Resource
    private BlockChainConfig chainConfig;
    @Resource
    private EpochRetryService epochRetryService;
    @Resource
    private CustomBlockNodeMapper customBlockNodeMapper;
    @Resource
    private NodeMapper nodeMapper;

    @Getter
    private BigInteger currentBlockNumber;
    @Getter
    private BigInteger consensusEpochRound = BigInteger.ZERO; // Current consensus cycle number
    @Getter
    private BigInteger settleEpochRound = BigInteger.ZERO; // Current settlement cycle number
    @Getter
    private BigInteger issueEpochRound = BigInteger.ZERO; // Current settlement cycle number

    private volatile static int max = -1;

    /**
     * Update service internal state using block number
     * 
     * @param blockNumber
     */
    public EpochMessage getEpochMessage(Long blockNumber) throws BlockNumberException {
        // The first block of each cycle calculates relevant data for the next cycle
        currentBlockNumber = BigInteger.valueOf(blockNumber);
        BigInteger prevBlockNumber = currentBlockNumber.subtract(BigInteger.ONE);

        // In order to prevent errors in the calculation of reward amounts when the three cycles of additional issuance, settlement, and consensus overlap, the execution order is stipulated as: change of additional issuance cycle -> change of settlement cycle -> change of consensus cycle
        issueEpochRound = EpochUtil.getEpoch(currentBlockNumber, chainConfig.getAddIssuePeriodBlockCount());
        if (prevBlockNumber.longValue() % chainConfig.getAddIssuePeriodBlockCount().longValue() == 0) {
            // Changes in issuance cycle
            try {
                epochRetryService.issueChange(currentBlockNumber);
            } catch (Exception e) {
                log.error("Failed to execute additional issuance cycle change:", e);
            }
        }

        settleEpochRound = EpochUtil.getEpoch(currentBlockNumber, chainConfig.getSettlePeriodBlockCount());
        if (prevBlockNumber.longValue() % chainConfig.getSettlePeriodBlockCount().longValue() == 0) {
            // settle cycle changes
            try {
                epochRetryService.settlementChange(currentBlockNumber);
            } catch (Exception e) {
                log.error("settlement cycle change execution failed:", e);
            }
        }

        consensusEpochRound = EpochUtil.getEpoch(currentBlockNumber, chainConfig.getConsensusPeriodBlockCount());
        if (prevBlockNumber.longValue() % chainConfig.getConsensusPeriodBlockCount().longValue() == 0) {
            // Consensus cycle changes
            try {
                epochRetryService.consensusChange(currentBlockNumber);
            } catch (Exception e) {
                log.error("Consensus cycle change execution failed:", e);
            }
            /**
             * Store node data for each consensus round
             */
            List<Node> nodes = epochRetryService.getCurValidators();
            if (!nodes.isEmpty()) {
                if (max == -1) {
                    max = customBlockNodeMapper.selectMaxNum();
                }
                /**
                 * Prevent repeated insertion of 25 consensus nodes
                 */
                if (prevBlockNumber.longValue() / chainConfig.getConsensusPeriodBlockCount().longValue() >= max) {
                    max++;
                    List<BlockNode> blockNodes = new ArrayList<>(32);
                    Date date = new Date();
                    nodes.forEach(node -> {
                        BlockNode blockNode = new BlockNode();
                        blockNode.setNodeId(node.getNodeId());
                        blockNode.setNodeName("");
                        com.turn.browser.dao.entity.Node nodeT = nodeMapper.selectByPrimaryKey(node.getNodeId());
                        if (nodeT == null) {
                            blockNode.setNodeName("");
                        } else {
                            blockNode.setNodeName(nodeT.getNodeName());
                        }
                        blockNode.setStakingConsensusEpoch(max);
                        blockNode.setCreateTime(date);
                        blockNode.setUpdateTime(date);
                        blockNodes.add(blockNode);
                    });
                    customBlockNodeMapper.batchInsert(blockNodes);
                }
            }
        }
        // Use the status information in EpochRetryService and the status information of EpochService to construct cycle information
        return EpochMessage.newInstance()
            .updateWithEpochService(this)
            .updateWithEpochRetryService(epochRetryService);
    }

}