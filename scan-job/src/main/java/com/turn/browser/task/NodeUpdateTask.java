package com.turn.browser.task;

import com.alibaba.fastjson.JSONObject;
import com.turn.browser.bean.NodeVersion;
import com.turn.browser.bean.keybase.KeyBaseUserInfo;
import com.turn.browser.client.TurnClient;
import com.turn.browser.client.SpecialApi;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.StakeBusinessMapper;
import com.turn.browser.dao.entity.Node;
import com.turn.browser.dao.mapper.NodeMapper;
import com.turn.browser.exception.HttpRequestException;
import com.turn.browser.utils.AppStatusUtil;
import com.turn.browser.utils.HttpUtil;
import com.turn.browser.utils.KeyBaseAnalysis;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * Node table supplement
 */
@Component
@Slf4j
public class NodeUpdateTask {

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private StakeBusinessMapper stakeBusinessMapper;

    @Resource
    private TurnClient turnClient;

    @Resource
    private SpecialApi specialApi;

    /**
     * Node table supplement
     * Executed every 5 seconds
     *
     * @param :
     * @return: void
     */
    @XxlJob("nodeUpdateJobHandler")
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void nodeUpdate() throws Exception {
        // Only perform tasks when the program is running normally
        if (!AppStatusUtil.isRunning()) return;
        try {
            //Query the pledge information to be supplemented
            List<Node> nodeList = nodeMapper.selectByExample(null);
            Map<String, Optional<KeyBaseUserInfo>> cache = new HashMap<>();
            List<Node> updateNodeList = new ArrayList<>();
            //Query the node version number list
            List<NodeVersion> versionList = specialApi.getNodeVersionList(turnClient.getWeb3jWrapper().getWeb3j());
            Map<String, NodeVersion> versionMap = new HashMap<>();
            versionList.forEach(v -> versionMap.put(v.getNodeId(), v));
            //Request URL prefix
            String prefix = chainConfig.getKeyBase().concat(chainConfig.getKeyBaseApi());
            nodeList.forEach(node -> {
                // Update keybase related information
                if (node.getExternalId().trim().length() == 16) {
                    Optional<KeyBaseUserInfo> optional = cache.computeIfAbsent(node.getExternalId(), key -> {
                        String url = prefix.concat(key);
                        try {
                            KeyBaseUserInfo keyBaseUser = HttpUtil.get(url, KeyBaseUserInfo.class);
                            return Optional.of(keyBaseUser);
                        } catch (HttpRequestException e) {
                            log.error("get keybase error:key={}", key, e);
                            return Optional.ofNullable(null);
                        }
                    });
                    optional.ifPresent(keyBaseUser -> {
                        boolean hasChange = false;
                        try {
                            String userName = KeyBaseAnalysis.getKeyBaseUseName(keyBaseUser);
                            String icon = KeyBaseAnalysis.getKeyBaseIcon(keyBaseUser);

                            if (StringUtils.isNotBlank(icon) && !icon.equals(node.getNodeIcon())) {
                                node.setNodeIcon(icon);
                                hasChange = true;
                            }
                            if (StringUtils.isNotBlank(userName) && !userName.equals(node.getExternalName())) {
                                node.setExternalName(userName);
                                hasChange = true;
                            }
                        } catch (Exception e) {
                            log.error("get keybase error:keyBaseUser={}", JSONObject.toJSONString(keyBaseUser), e);
                        }
                        if (hasChange) {
                            updateNodeList.add(node);
                        }
                    });
                }
                // Update node version number related information
                NodeVersion version = versionMap.get(node.getNodeId());
                if (version != null && (!version.getBigVersion().equals(node.getBigVersion()) || !version.getProgramVersion().equals(node.getProgramVersion()))) {
                    node.setBigVersion(version.getBigVersion());
                    node.setProgramVersion(version.getProgramVersion());
                    updateNodeList.add(node);
                }
            });
            if (!updateNodeList.isEmpty()) {
                stakeBusinessMapper.updateNodeForTask(updateNodeList);
            }
            XxlJobHelper.handleSuccess("Node table supplemented successfully");
        } catch (Exception e) {
            log.error("Node table supplement exception", e);
            throw e;
        }
    }

}
