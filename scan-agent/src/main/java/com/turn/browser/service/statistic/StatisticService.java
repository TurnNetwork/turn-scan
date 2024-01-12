package com.turn.browser.service.statistic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.turn.browser.analyzer.statistic.StatisticsAddressAnalyzer;
import com.turn.browser.analyzer.statistic.StatisticsNetworkAnalyzer;
import com.turn.browser.bean.*;
import com.turn.browser.cache.AddressCache;
import com.turn.browser.cache.NodeCache;
import com.turn.browser.dao.custommapper.CustomNodeMapper;
import com.turn.browser.dao.custommapper.CustomStakingMapper;
import com.turn.browser.dao.entity.Address;
import com.turn.browser.dao.entity.Node;
import com.turn.browser.dao.mapper.NodeMapper;
import com.turn.browser.elasticsearch.dto.Block;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Statistical warehousing parameter service
 *
 * @author chendai
 */
@Slf4j
@Service
public class StatisticService {

    @Resource
    private AddressCache addressCache;

    @Resource
    private StatisticsNetworkAnalyzer statisticsNetworkAnalyzer;

    @Resource
    private StatisticsAddressAnalyzer statisticsAddressAnalyzer;

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private CustomNodeMapper customNodeMapper;

    @Resource
    private CustomStakingMapper customStakingMapper;

    @Resource
    protected NodeCache nodeCache;

    /**
     * Analyze blocks and construct business warehousing parameter information
     *
     * @return
     */
    public void analyze(CollectionEvent event) throws Exception {
        long startTime = System.currentTimeMillis();
        Block block = event.getBlock();
        EpochMessage epochMessage = event.getEpochMessage();
        // Address statistics
        Collection<Address> addressList = this.addressCache.getAll();
        if (block.getNum() == 0) {
            if (CollUtil.isNotEmpty(addressList)) {
                // Initialize internal addresses, such as internal contracts, etc.
                this.statisticsAddressAnalyzer.analyze(event, block, epochMessage);
            }
            return;
        }
        // The program logic has run to this point. All ppos-related business logic has been analyzed and the address storage operation is carried out.
        if (CollUtil.isNotEmpty(addressList)) {
            this.statisticsAddressAnalyzer.analyze(event, block, epochMessage);
        }
        this.statisticsNetworkAnalyzer.analyze(event, block, epochMessage);
        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
    }

    /**
     * Block production statistics of the node settlement cycle---the number of times a node is elected as a block node
     *
     * @param event
     * @return void
     */
    public void nodeSettleStatisElected(CollectionEvent event) {
        try {
            // Initialize the number of elected nodes for all nodes
            List<Node> nodeList = nodeCache.toNodeSettleStatisInfoList();
            List<Node> updateNodeList = CollUtil.newArrayList();
            if (CollUtil.isNotEmpty(nodeList)) {
                nodeList.forEach(node -> {
                    String info = node.getNodeSettleStatisInfo();
                    NodeSettleStatis nodeSettleStatis;
                    if (StrUtil.isEmpty(info)) {
                        nodeSettleStatis = new NodeSettleStatis();
                        nodeSettleStatis.setNodeId(node.getNodeId());
                        nodeSettleStatis.setBlockNum(0L);
                        NodeSettleStatisBase nodeSettleStatisBase = new NodeSettleStatisBase();
                        nodeSettleStatisBase.setSettleEpochRound(event.getEpochMessage().getSettleEpochRound());
                        nodeSettleStatisBase.setBlockNumGrandTotal(BigInteger.ZERO);
                        if (inCurValidator(event.getEpochMessage().getPreValidatorList(), node.getNodeId())) {
                            nodeSettleStatisBase.setBlockNumElected(BigInteger.ONE);
                        } else {
                            nodeSettleStatisBase.setBlockNumElected(BigInteger.ZERO);
                        }
                        nodeSettleStatis.getNodeSettleStatisQueue().add(nodeSettleStatisBase);
                    } else {
                        nodeSettleStatis = NodeSettleStatis.jsonToBean(info);
                        if (event.getEpochMessage().getCurrentBlockNumber().compareTo(BigInteger.valueOf(nodeSettleStatis.getBlockNum())) > 0) {
                            addNodeSettleStatisElected(event.getEpochMessage().getPreValidatorList(),
                                                       node.getNodeId(),
                                                       event.getEpochMessage().getSettleEpochRound(),
                                                       nodeSettleStatis);
                        }
                    }
                    Node updateNode = new Node();
                    updateNode.setNodeId(node.getNodeId());
                    updateNode.setStakingBlockNum(node.getStakingBlockNum());
                    updateNode.setNodeSettleStatisInfo(JSONUtil.toJsonStr(nodeSettleStatis));
                    updateNodeList.add(updateNode);
                    updateNodeCacheSettleStatis(node.getNodeId(), JSONUtil.toJsonStr(nodeSettleStatis));
                });
                if (CollUtil.isNotEmpty(updateNodeList)) {

                    int res = customNodeMapper.updateNodeSettleStatis(updateNodeList);
                    if (res > 0) {
                        log.info("The node list ({}) selected the block node in the consensus round [{}] and the block height [{}], and the data was updated successfully.",
                                 updateNodeList.stream().map(Node::getNodeId).collect(Collectors.toList()),
                                 event.getEpochMessage().getConsensusEpochRound(),
                                 event.getEpochMessage().getCurrentBlockNumber());
                    } else {
                        log.error("The node list ({}) selected the block node in the consensus round [{}] block height [{}] and failed to update the data.",
                                  updateNodeList.stream().map(Node::getNodeId).collect(Collectors.toList()),
                                  event.getEpochMessage().getConsensusEpochRound(),
                                  event.getEpochMessage().getCurrentBlockNumber());
                    }

                    customStakingMapper.updateNodeSettleStatis(updateNodeList);

                }
            }
        } catch (Exception e) {
            log.error(StrUtil.format("The node is abnormally updated when the block is selected in the consensus round [{}] block height [{}]",
                                     event.getEpochMessage().getConsensusEpochRound(),
                                     event.getEpochMessage().getCurrentBlockNumber()), e);
        }
    }

    /**
     * Update statistics in cache
     *
     * @param nodeId
     * @param json
     * @return void
     */
    private void updateNodeCacheSettleStatis(String nodeId, String json) {
        try {
            NodeItem nodeItem = nodeCache.getNode(nodeId);
            nodeItem.setNodeSettleStatisInfo(json);
        } catch (Exception e) {
            log.error("Exception in updating node cache statistics", e);
        }
    }

    /**
     * Add node block number statistics
     *
     * @param curValidatorList Current consensus cycle validator list
     * @param nodeId node id
     * @param curIssueEpochRound Current settlement cycle number
     * @param nodeSettleStatis Block production statistics of the node settlement cycle
     * @return void
     */
    private void addNodeSettleStatisElected(List<com.bubble.contracts.dpos.dto.resp.Node> curValidatorList,
                                            String nodeId,
                                            BigInteger curIssueEpochRound,
                                            NodeSettleStatis nodeSettleStatis) {
        if (nodeSettleStatis.getNodeSettleStatisQueue().size() > 0) {
            List<NodeSettleStatisBase> list = nodeSettleStatis.getNodeSettleStatisQueue().toList();
            // The highest number of settlement cycle rounds recorded, the queue has been sorted
            BigInteger recordSettleEpochRound = list.get(0).getSettleEpochRound();
            if (recordSettleEpochRound.compareTo(curIssueEpochRound) == 0) {
                if (inCurValidator(curValidatorList, nodeId)) {
                    BigInteger newBlockNumElected = list.get(0).getBlockNumElected().add(BigInteger.ONE);
                    list.get(0).setBlockNumElected(newBlockNumElected);
                }
                nodeSettleStatis.getNodeSettleStatisQueue().clear();
                nodeSettleStatis.getNodeSettleStatisQueue().addAll(list);
            } else {
                // Record the number of rounds in the next settlement cycle
                NodeSettleStatisBase nodeSettleStatisBase = new NodeSettleStatisBase();
                nodeSettleStatisBase.setSettleEpochRound(curIssueEpochRound);
                nodeSettleStatisBase.setBlockNumGrandTotal(BigInteger.ZERO);
                if (inCurValidator(curValidatorList, nodeId)) {
                    nodeSettleStatisBase.setBlockNumElected(BigInteger.ONE);
                } else {
                    nodeSettleStatisBase.setBlockNumElected(BigInteger.ZERO);
                }
                nodeSettleStatis.getNodeSettleStatisQueue().offer(nodeSettleStatisBase);
            }
        } else {
            log.error("Node [{}] statistics [{}] are abnormal, please verify", nodeId, JSONUtil.toJsonStr(nodeSettleStatis));
        }
    }


    /**
     * Block production statistics of the node settlement cycle---the cumulative number of blocks produced
     *
     * @param event
     * @return void
     */
    public void nodeSettleStatisBlockNum(CollectionEvent event) {
        try {
            NodeItem nodeItem = nodeCache.getNode(event.getBlock().getNodeId());
            if (ObjectUtil.isNull(nodeItem)) {
                return;
            }
            String info = nodeItem.getNodeSettleStatisInfo();
            NodeSettleStatis nodeSettleStatis;
            if (StrUtil.isEmpty(info)) {
                nodeSettleStatis = new NodeSettleStatis();
                nodeSettleStatis.setNodeId(nodeItem.getNodeId());
                nodeSettleStatis.setBlockNum(event.getEpochMessage().getCurrentBlockNumber().longValue());
                NodeSettleStatisBase nodeSettleStatisBase = new NodeSettleStatisBase();
                nodeSettleStatisBase.setSettleEpochRound(event.getEpochMessage().getSettleEpochRound());
                nodeSettleStatisBase.setBlockNumGrandTotal(BigInteger.ONE);
                nodeSettleStatisBase.setBlockNumElected(BigInteger.ZERO);
                nodeSettleStatis.getNodeSettleStatisQueue().add(nodeSettleStatisBase);
            } else {
                nodeSettleStatis = NodeSettleStatis.jsonToBean(info);
                if (event.getEpochMessage().getCurrentBlockNumber().compareTo(BigInteger.valueOf(nodeSettleStatis.getBlockNum())) > 0) {
                    addNodeSettleStatisBlockNum(event.getEpochMessage().getCurrentBlockNumber().longValue(),
                                                event.getBlock().getNodeId(),
                                                event.getEpochMessage().getSettleEpochRound(),
                                                nodeSettleStatis);
                }
            }
            updateNodeCacheSettleStatis(nodeItem.getNodeId(), JSONUtil.toJsonStr(nodeSettleStatis));
            updateNodeSettleStatis(nodeItem.getNodeId(), JSONUtil.toJsonStr(nodeSettleStatis));
        } catch (Exception e) {
            log.error(StrUtil.format("The block production statistics of node [{}] settlement period are abnormal.", event.getBlock().getNodeId()), e);
        }
    }

    /**
     * Add node block number statistics
     *
     * @param blockNum block height
     * @param nodeId node id
     * @param curIssueEpochRound Current settlement cycle number
     * @param nodeSettleStatis Block production statistics of the node settlement cycle
     * @return void
     */
    private void addNodeSettleStatisBlockNum(Long blockNum, String nodeId, BigInteger curIssueEpochRound, NodeSettleStatis nodeSettleStatis) {
        nodeSettleStatis.setBlockNum(blockNum);
        if (nodeSettleStatis.getNodeSettleStatisQueue().size() > 0) {
            List<NodeSettleStatisBase> list = nodeSettleStatis.getNodeSettleStatisQueue().toList();
            // The highest number of settlement cycle rounds recorded, the queue has been sorted
            BigInteger recordSettleEpochRound = list.get(0).getSettleEpochRound();
            if (recordSettleEpochRound.compareTo(curIssueEpochRound) == 0) {
                BigInteger newBlockNumGrandTotal = list.get(0).getBlockNumGrandTotal().add(BigInteger.ONE);
                list.get(0).setBlockNumGrandTotal(newBlockNumGrandTotal);
                nodeSettleStatis.getNodeSettleStatisQueue().clear();
                nodeSettleStatis.getNodeSettleStatisQueue().addAll(list);
            } else {
                // Record the number of rounds in the next settlement cycle
                NodeSettleStatisBase nodeSettleStatisBase = new NodeSettleStatisBase();
                nodeSettleStatisBase.setSettleEpochRound(curIssueEpochRound);
                nodeSettleStatisBase.setBlockNumGrandTotal(BigInteger.ONE);
                nodeSettleStatisBase.setBlockNumElected(BigInteger.ZERO);
                nodeSettleStatis.getNodeSettleStatisQueue().offer(nodeSettleStatisBase);
            }
        } else {
            log.error("Node [{}] statistics [{}] are abnormal, please verify", nodeId, JSONUtil.toJsonStr(nodeSettleStatis));
        }
    }

    /**
     * Update node block number statistics
     *
     * @param nodeId
     * @param json
     */
    private void updateNodeSettleStatis(String nodeId, String json) {
        Node updateNode = new Node();
        updateNode.setNodeId(nodeId);
        updateNode.setNodeSettleStatisInfo(json);
        int res = nodeMapper.updateByPrimaryKeySelective(updateNode);
        if (res > 0) {
            log.debug("The node’s block production statistics information [{}] in the latest [{}] settlement period has been updated successfully.", CommonConstant.BLOCK_RATE_SETTLE_EPOCH_NUM, json);
        } else {
            log.error("The node's block production statistics [{}] failed to be updated in the latest [{}] settlement cycles.", CommonConstant.BLOCK_RATE_SETTLE_EPOCH_NUM, json);
        }
    }

    /**
     * Determine whether the current node is in the validator list of the current consensus cycle
     *
     * @param curValidatorList Current consensus cycle validator list
     * @param nodeId
     * @return boolean
     */
    private boolean inCurValidator(List<com.bubble.contracts.dpos.dto.resp.Node> curValidatorList, String nodeId) {
        if (CollUtil.isNotEmpty(curValidatorList)) {
            List<com.bubble.contracts.dpos.dto.resp.Node> curValidator = curValidatorList.stream()
                                                                                         .filter(v -> v.getNodeId().equalsIgnoreCase(nodeId))
                                                                                         .collect(Collectors.toList());
            if (curValidator.size() > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
