package com.turn.browser.analyzer.ppos;

import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.ComplementNodeOpt;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.param.VersionDeclareParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @description: Version declaration business parameter converter
 **/
@Slf4j
@Service
public class VersionDeclareAnalyzer
        extends PPOSAnalyzer<NodeOpt> {

    /**
     * Version Statement Analysis
     *
     * @param event
     * @param tx
     * @return com.turn.browser.elasticsearch.dto.NodeOpt
     */
    @Override
    public NodeOpt analyze(CollectionEvent event, Transaction tx) {
        VersionDeclareParam txParam = tx.getTxParam(VersionDeclareParam.class);
        // Supplementary node name
        updateTxInfo(txParam, tx);
        // Failed transactions do not analyze business data
        if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus()) return null;

        long startTime = System.currentTimeMillis();

        String nodeId = txParam.getActiveNode();
        String nodeName = txParam.getNodeName();

        String desc = NodeOpt.TypeEnum.VERSION.getTpl()
                                              .replace("NODE_NAME", nodeName)
                                              .replace("ACTIVE_NODE", nodeId)
                                              .replace("VERSION", String.valueOf(txParam.getVersion()));

        NodeOpt nodeOpt = ComplementNodeOpt.newInstance();
        nodeOpt.setNodeId(nodeId);
        nodeOpt.setType(Integer.valueOf(NodeOpt.TypeEnum.VERSION.getCode()));
        nodeOpt.setDesc(desc);
        nodeOpt.setTxHash(tx.getHash());
        nodeOpt.setBNum(event.getBlock().getNum());
        nodeOpt.setTime(tx.getTime());

        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);

        return nodeOpt;
    }

}
