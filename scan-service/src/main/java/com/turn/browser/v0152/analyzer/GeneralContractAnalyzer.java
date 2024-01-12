package com.turn.browser.v0152.analyzer;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.turn.browser.bean.CollectionTransaction;
import com.turn.browser.bean.ComplementInfo;
import com.turn.browser.bean.CustomStaking;
import com.turn.browser.client.TurnClient;
import com.turn.browser.dao.entity.MicroNode;
import com.turn.browser.dao.entity.MicroNodeExample;
import com.turn.browser.dao.entity.MicroNodeOptBak;
import com.turn.browser.dao.mapper.AddrGameMapper;
import com.turn.browser.dao.mapper.MicroNodeMapper;
import com.turn.browser.dao.mapper.MicroNodeOptBakMapper;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.enums.MicroNodeStatusEnum;
import com.turn.browser.enums.TransactionStatusEnum;
import com.turn.browser.param.CreateBubbleParam;
import com.turn.browser.param.CreateStakeParam;
import com.turn.browser.param.EditCandidateParam;
import com.turn.browser.param.ReleaseBubbleParam;
import com.turn.browser.service.elasticsearch.EsMicroNodeOptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class GeneralContractAnalyzer {

    @Resource
    private MicroNodeMapper microNodeMapper;

    @Resource
    private MicroNodeOptBakMapper microNodeOptBakMapper;

    @Resource
    private EsMicroNodeOptService esMicroNodeOptService;

    @Resource
    private TurnClient turnClient;

    public void resolveTx(CollectionTransaction result, ComplementInfo ci, int status) {
        if(TransactionStatusEnum.FAIL.getCode() == status){
            return;
        }
        Transaction.TypeEnum typeEnum = Transaction.TypeEnum.getEnum(ci.getType());
        switch (typeEnum){
            case CREATE_STAKING: microNodeHandler(result, ci, MicroNodeStatusEnum.CANDIDATE);break;
            case EDIT_CANDIDATE: microNodeHandler(result, ci, null);break;
            case WITHDREW_STAKING: microNodeHandler(result, ci, MicroNodeStatusEnum.EXITED);break;
            case CREATE_BUBBLE: createBubble(result,ci);break;
            case RELEASE_BUBBLE: releaseBubble(result,ci);break;
        }
    }

    /**
     *The bubble is released, and the bubbleId and bubbleCreator of the node need to be reset.
     * @param result
     * @param ci
     */
    @Transactional(rollbackFor = Exception.class)
    public void releaseBubble(CollectionTransaction result, ComplementInfo ci) {
        ReleaseBubbleParam releaseBubbleParam = JSONObject.parseObject(ci.getInfo(), ReleaseBubbleParam.class);
        MicroNode microNode = new MicroNode();
        microNode.setBubbleId(0L);
        microNode.setBubbleCreator("");
        MicroNodeExample microNodeExample = new MicroNodeExample();
        microNodeExample.createCriteria().andBubbleIdEqualTo(releaseBubbleParam.getBubbleId().longValue());
        microNodeMapper.updateByExampleSelective(microNode,microNodeExample);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createBubble(CollectionTransaction collectionTransaction, ComplementInfo ci) {
        CreateBubbleParam createBubbleParam = JSONObject.parseObject(ci.getInfo(), CreateBubbleParam.class);
        String bubbleInfo = turnClient.getBubbleInfo(createBubbleParam.getBubbleId());
        JSONObject info = JSONObject.parseObject(bubbleInfo);
        JSONObject basics = info.getJSONObject("Basics");
        JSONArray microNodes = basics.getJSONArray("MicroNodes");
        List<String> result = new ArrayList<>(microNodes.size());
        for (Object microNode : microNodes) {
            JSONObject microNodeJson = (JSONObject)microNode;
            result.add(microNodeJson.getString("StakingAddress"));
        }
        String creator = basics.getString("Creator");
        MicroNode microNode = new MicroNode();
        microNode.setBubbleId(createBubbleParam.getBubbleId().longValue());
        microNode.setBubbleCreator(creator);
        MicroNodeExample microNodeExample = new MicroNodeExample();
        microNodeExample.createCriteria().andOperationAddrIn(result);
        microNodeMapper.updateByExampleSelective(microNode,microNodeExample);
    }

    private void microNodeHandler(CollectionTransaction result, ComplementInfo ci, MicroNodeStatusEnum microNodeStatusEnum) {
        if(MicroNodeStatusEnum.CANDIDATE == microNodeStatusEnum){
            createStaking(result, ci,microNodeStatusEnum);
        }else {
            editWithdrew(result, ci,microNodeStatusEnum);
        }
    }

    /**
     * Micronode staking processing
     * @param result
     * @param ci
     * @param microNodeStatusEnum
     */
    private void createStaking(CollectionTransaction result, ComplementInfo ci, MicroNodeStatusEnum microNodeStatusEnum) {
        CreateStakeParam createStakeParam = JSONObject.parseObject(ci.getInfo(), CreateStakeParam.class);
        MicroNodeExample microNodeExample = new MicroNodeExample();
        microNodeExample.createCriteria().andNodeIdEqualTo(createStakeParam.getNodeId());
        List<MicroNode> microNodes = microNodeMapper.selectByExample(microNodeExample);
        // The node has not been staked
        if(CollectionUtils.isEmpty(microNodes)){
            MicroNode microNode = new MicroNode();
            microNode.setNodeId(createStakeParam.getNodeId());
            microNode.setAmount(new BigDecimal(createStakeParam.getAmount()));
            microNode.setBeneficiary(createStakeParam.getBeneficiary());
            microNode.setDetails(createStakeParam.getDetails());
            microNode.setElectronUri(createStakeParam.getElectronURI());
            microNode.setIsOperator(createStakeParam.getIsOperator());
            microNode.setName(createStakeParam.getName());
            microNode.setNodeStatus(microNodeStatusEnum.getCode());
            microNode.setP2pUri(createStakeParam.getP2pURI());
            microNode.setVersion(createStakeParam.getVersion());
            microNode.setOperationAddr(result.getFrom());
            microNode.setCreateTime(new Date());
            microNodeMapper.insert(microNode);
        }else {
            // The node has been staked
            MicroNode microNode = microNodes.get(0);
            microNode.setNodeStatus(microNodeStatusEnum.getCode());
            microNode.setAmount(new BigDecimal(createStakeParam.getAmount()));
            microNode.setBeneficiary(createStakeParam.getBeneficiary());
            microNode.setDetails(createStakeParam.getDetails());
            microNode.setElectronUri(createStakeParam.getElectronURI());
            microNode.setIsOperator(createStakeParam.getIsOperator());
            microNode.setNodeStatus(microNodeStatusEnum.getCode());
            microNode.setP2pUri(createStakeParam.getP2pURI());
            microNode.setVersion(createStakeParam.getVersion());
            microNode.setOperationAddr(result.getFrom());
            microNode.setUpdateTime(new Date());
            microNode.setName(createStakeParam.getName());
            microNodeMapper.updateByPrimaryKey(microNode);
        }
        MicroNodeOptBak microNodeOptBak = new MicroNodeOptBak();
        microNodeOptBak.setNodeId(createStakeParam.getNodeId());
        microNodeOptBak.setType(OptTypeEnum.STAKE.code);
        microNodeOptBak.setbNum(result.getNum());
        microNodeOptBak.setTxHash(result.getHash());
        microNodeOptBak.setTime(result.getTime());
        microNodeOptBak.setCreTime(new Date());

        microNodeOptBakMapper.insert(microNodeOptBak);

        try {
            esMicroNodeOptService.add(microNodeOptBak);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Update node information, unstake, and process
     * @param result
     * @param ci
     * @param microNodeStatusEnum
     */
    private void editWithdrew(CollectionTransaction result, ComplementInfo ci, MicroNodeStatusEnum microNodeStatusEnum) {
        EditCandidateParam editCandidateParam = JSONObject.parseObject(ci.getInfo(), EditCandidateParam.class);
        MicroNodeExample microNodeExample = new MicroNodeExample();
        microNodeExample.createCriteria().andNodeIdEqualTo(editCandidateParam.getNodeId());
        List<MicroNode> microNodes = microNodeMapper.selectByExample(microNodeExample);
        MicroNode microNode = microNodes.get(0);
        if(ObjectUtil.isNull(microNodeStatusEnum)){
            microNode.setBeneficiary(editCandidateParam.getBeneficiary());
            microNode.setName(editCandidateParam.getName());
            microNode.setDetails(editCandidateParam.getDetails());
        }
        if(ObjectUtil.isNotNull(microNodeStatusEnum)){
            microNode.setAmount(BigDecimal.ZERO);
            microNode.setNodeStatus(microNodeStatusEnum.getCode());
        }

        microNode.setUpdateTime(new Date());
        microNodeMapper.updateByExample(microNode,microNodeExample);
        MicroNodeOptBak microNodeOptBak = new MicroNodeOptBak();
        microNodeOptBak.setNodeId(editCandidateParam.getNodeId());
        if(ObjectUtil.isNotNull(microNodeStatusEnum)){
            microNodeOptBak.setType(OptTypeEnum.WITHDRAW.code);
        }else {
            microNodeOptBak.setType(OptTypeEnum.UPDATE.code);
        }
        microNodeOptBak.setCreTime(new Date());
        microNodeOptBak.setbNum(result.getNum());
        microNodeOptBak.setTxHash(result.getHash());
        microNodeOptBak.setTime(result.getTime());
        microNodeOptBakMapper.insert(microNodeOptBak);

        try {
            esMicroNodeOptService.add(microNodeOptBak);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void resolveTx(String texasHoldemTxInfo) {

    }

    public enum OptTypeEnum{
        STAKE(1, "Pledge"),
        UPDATE(2, "Modify"),
        WITHDRAW(3, "Unstaking")
        ;
        private int code;
        private String desc;
        OptTypeEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        public int getCode(){return code;}
        public String getDesc(){return desc;}
        private static final Map<Integer, OptTypeEnum> ENUMS = new HashMap<>();
        static {
            Arrays.asList(OptTypeEnum.values()).forEach(en->ENUMS.put(en.code,en));}
        public static OptTypeEnum getEnum(Integer code){
            return ENUMS.get(code);
        }
        public static boolean contains(int code){return ENUMS.containsKey(code);}
        public static boolean contains(CustomStaking.StatusEnum en){return ENUMS.containsValue(en);}
    }
}
