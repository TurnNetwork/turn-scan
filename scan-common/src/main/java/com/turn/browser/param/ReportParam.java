package com.turn.browser.param;

import com.alibaba.fastjson.JSON;
import com.turn.browser.param.evidence.PrepareEvidence;
import com.turn.browser.param.evidence.VoteEvidence;
import com.turn.browser.utils.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * txType=3000
 */
@Data
@Slf4j
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class ReportParam extends TxParam{

    /**
     *Multiple signature types
     */
    private BigInteger type;

    /**
     * The json value of the evidence, in the format of the return value of the RPC interface Evidences
     */
    private String data;

    /**
     * The reported node id
     */
    private String verify;
    public void setVerify(String verify){
        this.verify= HexUtil.prefix(verify);
    }

    /**
     * The name of the reported node (with a length limit, indicating the name of the node)
     */
    private String nodeName;

    /**
     * Pledge transaction speeds up
     */
    private BigInteger stakingBlockNum;

    /**
     * Report amount
     */
    private BigDecimal reward;

    public ReportParam init() {
        this.verify = format(type, data);
        return this;
    }

    private String format (BigInteger type, String date) {
        String info = "";
        try {
            if(BigInteger.ONE.compareTo(type) == 0) {
                PrepareEvidence evidence = JSON.parseObject(date, PrepareEvidence.class);
                if (isObjectFieldEmpty(evidence)) {
                    if (isObjectFieldEmpty(evidence.getPrepareA())) {
                        info = evidence.getPrepareA().getValidateNode().getNodeId();
                    }
                }
            } else if(BigInteger.valueOf(2l).compareTo(type) == 0) {
                VoteEvidence evidence = JSON.parseObject(date, VoteEvidence.class);
                if (isObjectFieldEmpty(evidence)) {
                    if (isObjectFieldEmpty(evidence.getVoteA())) {
                        info = evidence.getVoteA().getValidateNode().getNodeId();
                    }
                }
            }

        } catch (Exception e) {
            log.error("json decode error", e);
        }
        return info;
    }

    //Reflection to get whether the object property is empty
    public boolean isObjectFieldEmpty ( Object object ) {
        boolean flag = false;
        if (object != null) {
            Class <?> entity = object.getClass();
            Field[] fields = entity.getDeclaredFields();//Get all member variables of this class (private)
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    if (field.get(object) != null && !"".equals(field.get(object))) {
                        flag = true;
                        break;
                    }
                } catch (IllegalAccessException e) {
                    log.error("",e);
                }
            }
        }
        return flag;
    }

}
