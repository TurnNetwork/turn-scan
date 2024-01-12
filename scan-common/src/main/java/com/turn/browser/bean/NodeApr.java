package com.turn.browser.bean;

import cn.hutool.core.collection.BoundedPriorityQueue;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Data
@Slf4j
public class NodeApr {

    private String nodeId;

    /**
     * Create a bounded priority queue to store the node’s entrusted annualized return rate for 9 settlement cycles
     */
    BoundedPriorityQueue<NodeAprBase> nodeAprQueue = new BoundedPriorityQueue<>(CommonConstant.BLOCK_APR_EPOCH_NUM + 1,
                                                                                (v1, v2) -> v2.getEpochNum()
                                                                                              .compareTo(v1.getEpochNum()));

    /**
     * Construct
     *
     * @param settingEpoch: settlement cycle
     * @param nodeId: node id
     * @param annualizedRate: Commission annualized rate
     * @param json:           json
     * @return:
     */
    public static NodeApr build(Integer settingEpoch, String nodeId, BigDecimal annualizedRate, String json) {
        NodeApr nodeApr = new NodeApr();
        nodeApr.setNodeId(nodeId);
        NodeAprBase nodeAprBase = new NodeAprBase();
        nodeAprBase.setEpochNum(settingEpoch);
        nodeAprBase.setDeleAnnualizedRate(annualizedRate.toPlainString());
        if (StrUtil.isNotEmpty(json) && JSONUtil.isJson(json)) {
            JSONObject jsonObject = JSONUtil.parseObj(json);
            JSONArray jsonArray = jsonObject.getJSONArray("nodeAprQueue");
            List<NodeAprBase> list = jsonArray.toList(NodeAprBase.class);
            nodeApr.getNodeAprQueue().addAll(list);
        }
        nodeApr.getNodeAprQueue().offer(nodeAprBase);
        return nodeApr;
    }

    /**
     * Get the commission annualized rate of the previous day
     *
     * @param :
     * @return:
     */
    public static String getPreDeleAnnualizedRate(String json) {
        try {
            if (StrUtil.isNotEmpty(json) && JSONUtil.isJson(json)) {
                JSONObject jsonObject = JSONUtil.parseObj(json);
                JSONArray jsonArray = jsonObject.getJSONArray("nodeAprQueue");
                List<NodeAprBase> list = jsonArray.toList(NodeAprBase.class);
                return list.get(0).getDeleAnnualizedRate();
            } else {
                return "0";
            }
        } catch (Exception e) {
            log.error("Get the commission annualized rate anomaly of the previous day", e);
            return "0";
        }
    }

}
