package com.turn.browser.v0152.analyzer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.turn.browser.bean.CollectionTransaction;
import com.turn.browser.bean.ComplementInfo;
import com.turn.browser.bean.CustomStaking;
import com.turn.browser.client.TurnClient;
import com.turn.browser.dao.entity.*;
import com.turn.browser.dao.mapper.BubbleMapper;
import com.turn.browser.dao.mapper.MicroNodeMapper;
import com.turn.browser.dao.mapper.MicroNodeOptBakMapper;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.enums.BubbleStatusEnum;
import com.turn.browser.enums.MicroNodeStatusEnum;
import com.turn.browser.enums.TransactionStatusEnum;
import com.turn.browser.param.CreateStakeParam;
import com.turn.browser.param.EditCandidateParam;
import com.turn.browser.service.BubbleCacheService;
import com.turn.browser.service.ReleaseBubbleCacheService;
import com.turn.browser.service.elasticsearch.EsMicroNodeOptService;
import com.turn.browser.v0152.bean.BubbleDetailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class MicroNodeAnalyzer {

    @Resource
    private MicroNodeMapper microNodeMapper;

    @Resource
    private MicroNodeOptBakMapper microNodeOptBakMapper;

    @Resource
    private EsMicroNodeOptService esMicroNodeOptService;

    @Resource
    private TurnClient turnClient;

    @Resource
    private BubbleCacheService bubbleCacheService;

    @Resource
    private ReleaseBubbleCacheService releaseBubbleCacheService;

    @Resource
    private BubbleMapper bubbleMapper;

    public void resolveTx(CollectionTransaction result, ComplementInfo ci, int status) {
        if(TransactionStatusEnum.FAIL.getCode() == status){
            return;
        }
        Transaction.TypeEnum typeEnum = Transaction.TypeEnum.getEnum(ci.getType());
        switch (typeEnum){
            case CREATE_STAKING: microNodeHandler(result, ci, MicroNodeStatusEnum.CANDIDATE);break;
            case EDIT_CANDIDATE: microNodeHandler(result, ci, null);break;
            case WITHDREW_STAKING: microNodeHandler(result, ci, MicroNodeStatusEnum.EXITED);break;
        }
    }

    /**
     * The bubble is released and the bubbleId of the node needs to be reset.
     */
    @Transactional(rollbackFor = Exception.class)
    public void releaseBubble(Long releaseBubbleNum) {
        List<Long> bubblePreReleaseCache = releaseBubbleCacheService.getBubblePreReleaseCache(releaseBubbleNum);
        if(CollUtil.isNotEmpty(bubblePreReleaseCache)){
            Bubble bubble = new Bubble();
            bubble.setStatus(BubbleStatusEnum.RELEASING.getCode());
            BubbleExample bubbleExample = new BubbleExample();
            bubbleExample.createCriteria().andBubbleIdIn(bubblePreReleaseCache);
            bubbleMapper.updateByExampleSelective(bubble,bubbleExample);
            releaseBubbleCacheService.delReleaseBubbleCache(releaseBubbleNum);
        }
        List<Long> bubbleReleaseCache = releaseBubbleCacheService.getBubbleReleaseCache(releaseBubbleNum);
        if(CollUtil.isNotEmpty(bubbleReleaseCache)){
            MicroNode microNode = new MicroNode();
            microNode.setBubbleId(0L);
            MicroNodeExample microNodeExample = new MicroNodeExample();
            microNodeExample.createCriteria().andBubbleIdIn(bubbleReleaseCache);
            microNodeMapper.updateByExampleSelective(microNode,microNodeExample);
            bubbleReleaseCache.forEach(item->bubbleCacheService.delBubbleCache(item));;
            releaseBubbleCacheService.delReleaseBubbleCache(releaseBubbleNum);
        }

    }

    /**
     * create bubble
     */
    @Transactional(rollbackFor = Exception.class)
    public void createBubble(Long bubbleId) {
        Bubble bubble = new Bubble();
        bubble.setBubbleId(bubbleId);
        bubble.setCreateTime(new Date());
        bubbleMapper.insert(bubble);
        String bubbleInfo = turnClient.getBubbleInfo(BigInteger.valueOf(bubbleId));
        JSONObject info = JSONObject.parseObject(bubbleInfo);
        if(ObjectUtil.isNull(info)){
            log.error("{} info not exist",bubbleId);
            return;
        }
        JSONArray microNodes = info.getJSONArray("MicroNodes");
        List<String> result = new ArrayList<>(microNodes.size());
        List<String> rpcUris = new ArrayList<>(microNodes.size());
        for (Object microNode : microNodes) {
            JSONObject microNodeJson = (JSONObject)microNode;
            if(StrUtil.isNotBlank(microNodeJson.getString("RPCURI"))){
                rpcUris.add(microNodeJson.getString("RPCURI"));
            }
            result.add(microNodeJson.getString("NodeId"));
        }
        Long releaseBlock = info.getLong("ReleaseBlock")+1;
        Long preReleaseBlock = info.getLong("PreReleaseBlock")+1;
        MicroNode microNode = new MicroNode();
        microNode.setBubbleId(bubbleId);
        MicroNodeExample microNodeExample = new MicroNodeExample();
        microNodeExample.createCriteria().andNodeIdIn(result);
        microNodeMapper.updateByExampleSelective(microNode,microNodeExample);
        List<MicroNode> microNodeList = microNodeMapper.selectByExample(microNodeExample);
        BubbleDetailDto bubbleDetailDto = new BubbleDetailDto();
        bubbleDetailDto.setRpcUris(rpcUris);
        bubbleDetailDto.setReleaseBubbleNum(releaseBlock);
        bubbleDetailDto.setMicroNodes(microNodeList);
        bubbleCacheService.addBubbleCache(bubbleDetailDto,bubbleId);
        List<Long> bubbleReleaseCache = releaseBubbleCacheService.getBubbleReleaseCache(releaseBlock);
        bubbleReleaseCache.add(bubbleId);
        releaseBubbleCacheService.addReleaseBubbleCache(releaseBlock,bubbleReleaseCache);
        List<Long> bubblePreReleaseCache = releaseBubbleCacheService.getBubblePreReleaseCache(preReleaseBlock);
        bubblePreReleaseCache.add(bubbleId);
        releaseBubbleCacheService.addPreReleaseBubbleCache(preReleaseBlock,bubblePreReleaseCache);
    }

    private void microNodeHandler(CollectionTransaction result, ComplementInfo ci, MicroNodeStatusEnum microNodeStatusEnum) {
        if(MicroNodeStatusEnum.CANDIDATE == microNodeStatusEnum){
            createStaking(result, ci,microNodeStatusEnum);
        }else {
            editWithdrew(result, ci,microNodeStatusEnum);
        }
    }

    /**
     * Micronode staked processing
     * @param result
     * @param ci
     * @param microNodeStatusEnum
     */
    @Transactional(rollbackFor = Exception.class)
    public void createStaking(CollectionTransaction result, ComplementInfo ci, MicroNodeStatusEnum microNodeStatusEnum) {
        CreateStakeParam createStakeParam = JSONObject.parseObject(ci.getInfo(), CreateStakeParam.class);
        MicroNodeExample microNodeExample = new MicroNodeExample();
        microNodeExample.createCriteria().andNodeIdEqualTo(createStakeParam.getNodeId().substring(2));
        List<MicroNode> microNodes = microNodeMapper.selectByExample(microNodeExample);
        // The node has not been staked
        if(CollectionUtils.isEmpty(microNodes)){
            MicroNode microNode = new MicroNode();
            microNode.setNodeId(createStakeParam.getNodeId().substring(2));
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
            microNode.setRpcUri(createStakeParam.getRpcUri());
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
            microNode.setRpcUri(createStakeParam.getRpcUri());
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
    @Transactional(rollbackFor = Exception.class)
    public void editWithdrew(CollectionTransaction result, ComplementInfo ci, MicroNodeStatusEnum microNodeStatusEnum) {
        EditCandidateParam editCandidateParam = JSONObject.parseObject(ci.getInfo(), EditCandidateParam.class);
        MicroNodeExample microNodeExample = new MicroNodeExample();
        microNodeExample.createCriteria().andNodeIdEqualTo(editCandidateParam.getNodeId().substring(2));
        List<MicroNode> microNodes = microNodeMapper.selectByExample(microNodeExample);
        MicroNode microNode = microNodes.get(0);
        if(ObjectUtil.isNull(microNodeStatusEnum)){
            microNode.setBeneficiary(editCandidateParam.getBeneficiary());
            microNode.setName(editCandidateParam.getName());
            microNode.setDetails(editCandidateParam.getDetails());
            microNode.setRpcUri(editCandidateParam.getRpcUri());
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

    public enum OptTypeEnum{
        STAKE(1, "STAKE"),
        UPDATE(2, "Modify"),
        WITHDRAW(3, "Unstaked")
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
