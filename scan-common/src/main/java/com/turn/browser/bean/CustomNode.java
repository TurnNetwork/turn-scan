package com.turn.browser.bean;

import com.turn.browser.dao.entity.Node;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: Node entity extension class
 */
@Data
public class CustomNode extends Node {

     public CustomNode(){
         super();
         Date date = new Date();
         this.setUpdateTime(date);
         this.setCreateTime(date);
         setNodeId("");
         setStatSlashLowQty(0);
         setStatBlockQty(0L);
         setStatExpectBlockQty(0L);
         setStatVerifierTime(0);
         setIsRecommend(YesNoEnum.NO.getCode());
         setTotalValue(BigDecimal.ZERO);
         setStakingBlockNum(0L);
         setStakingTxIndex(0);
         setStakingHes(BigDecimal.ZERO);
         setStakingLocked(BigDecimal.ZERO);
         setStakingReduction(BigDecimal.ZERO);
         setStakingReductionEpoch(0);
         setNodeName("");
         setNodeIcon("");
         setExternalId("");
         setExternalName("");
         setStakingAddr("");
         setBenefitAddr("");
         setAnnualizedRate(0.0);
         setProgramVersion(0);
         setBigVersion(0);
         setWebSite("");
         setDetails("");
         setJoinTime(new Date());
         setLeaveTime(null);
         setStatus(CustomStaking.StatusEnum.CANDIDATE.getCode());
         setIsConsensus(CustomStaking.YesNoEnum.NO.getCode());
         setIsSettle(CustomStaking.YesNoEnum.NO.getCode());
         setIsInit(CustomStaking.YesNoEnum.NO.getCode());
         setStatDelegateValue(BigDecimal.ZERO);
         setStatDelegateReleased(BigDecimal.ZERO);
         setStatValidAddrs(0);
         setStatInvalidAddrs(0);
         setStatBlockRewardValue(BigDecimal.ZERO);
         setStatFeeRewardValue(BigDecimal.ZERO);
         setStatStakingRewardValue(BigDecimal.ZERO);
         setStatSlashLowQty(0);
         setStatSlashMultiQty(0);
     }

    public CustomNode updateWithCustomStaking(CustomStaking staking) {
        BeanUtils.copyProperties(staking,this);
        return this;
    }

    /**
     * Whether the node officially recommends type enumeration class:
     * 1.Yes
     * 2.No
     */
    public enum YesNoEnum{
        YES(1, "Yes"),
        NO(2, "No")
          ;
          private int code;
          private String desc;
          YesNoEnum(int code, String desc) {
               this.code = code;
               this.desc = desc;
          }
          public int getCode(){return code;}
          public String getDesc(){return desc;}
          private static final Map<Integer, YesNoEnum> ENUMS = new HashMap<>();
          static {Arrays.asList(YesNoEnum.values()).forEach(en->ENUMS.put(en.code,en));}
          public static YesNoEnum getEnum(Integer code){
               return ENUMS.get(code);
          }
          public static boolean contains(int code){return ENUMS.containsKey(code);}
          public static boolean contains(YesNoEnum en){return ENUMS.containsValue(en);}
     }

}
