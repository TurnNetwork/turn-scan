package com.turn.browser.cache;

import cn.hutool.core.collection.CollUtil;
import com.turn.browser.bean.NodeItem;
import com.turn.browser.dao.entity.Node;
import com.turn.browser.exception.NoSuchBeanException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NodeCache {

    private static final Map<String, NodeItem> cache = new HashMap<>();

    /**
     * Clear node cache
     *
     * @param:
     * @return: void
     */
    public void cleanNodeCache() {
        cache.clear();
    }

    /**
     * Get the node based on the node ID
     *
     * @param nodeId
     * @return
     * @throws NoSuchBeanException
     */
    public NodeItem getNode(String nodeId) throws NoSuchBeanException {
        NodeItem node = cache.get(nodeId);
        if (node == null) throw new NoSuchBeanException("The node of node (id=" + nodeId + ") does not exist");
        return node;
    }

    /**
     * Add node
     *
     * @param node
     */
    public void addNode(NodeItem node) {
        cache.put(node.getNodeId(), node);
    }

    /**
     * Initialize node cache
     *
     * @param nodeList
     * @return void
     */
    public void init(List<Node> nodeList) {
        log.info("Initializing node cache");
        if (nodeList.isEmpty()) return;
        nodeList.forEach(s -> {
            NodeItem node = NodeItem.builder()
                    .nodeId(s.getNodeId())
                    .nodeName(s.getNodeName())
                    .stakingBlockNum(BigInteger.valueOf(s.getStakingBlockNum()))
                    .nodeSettleStatisInfo(s.getNodeSettleStatisInfo())
                    .build();
            addNode(node);
        });
    }

    /**
     * Convert to list---node periodic block information statistics
     *
     * @param
     * @return java.util.List<com.turn.browser.dao.entity.Node>
     */
    public List<Node> toNodeSettleStatisInfoList() {
        List<Node> list = CollUtil.newArrayList();
        cache.forEach((k, v) -> {
            Node node = new Node();
            node.setNodeId(k);
            node.setStakingBlockNum(v.getStakingBlockNum().longValue());
            node.setNodeSettleStatisInfo(v.getNodeSettleStatisInfo());
            list.add(node);
        });
        return list;
    }

}
