package com.turn.browser.bean;

import com.turn.browser.SubChainTopic;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SubChainTx {

    private long id;
    private int type;
    private long seq;
    private String bHash;
    private long num;
    private int index;
    private String hash;
    private String from;
    private String to;
    private int fromType;
    private int toType;
    private long nonce;
    private String gasLimit;
    private String  gasPrice;
    private String gasUsed;
    private String cost;
    private String value;
    private int status;
    private Timestamp time;
    private String failReason;
    private String remark;
    private long bubbleId;
    private String input;
    List<SubChainTopic> subChainTopics;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public String getbHash() {
        return bHash;
    }

    public void setbHash(String bHash) {
        this.bHash = bHash;
    }

    public long getNum() {
        return num;
    }

    public void setNum(long num) {
        this.num = num;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getFromType() {
        return fromType;
    }

    public void setFromType(int fromType) {
        this.fromType = fromType;
    }

    public int getToType() {
        return toType;
    }

    public void setToType(int toType) {
        this.toType = toType;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }

    public String getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(String gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getBubbleId() {
        return bubbleId;
    }

    public void setBubbleId(long bubbleId) {
        this.bubbleId = bubbleId;
    }

    public List<SubChainTopic> getSubChainTopics() {
        return subChainTopics;
    }

    public void setSubChainTopics(List<SubChainTopic> subChainTopics) {
        this.subChainTopics = subChainTopics;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public enum TypeEnum {

        CREATE_GAME(1, "创建游戏"),

        JOIN_GAME(2, "加入游戏"),

        END_GAME(3, "结束游戏");
        private static Map<Integer, SubChainTx.TypeEnum> map = new HashMap<>();

        static {
            Arrays.asList(SubChainTx.TypeEnum.values()).forEach(typeEnum -> map.put(typeEnum.code, typeEnum));
        }

        public static SubChainTx.TypeEnum getEnum(int code) {
            return map.get(code);
        }

        private int code;

        private String desc;

        TypeEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return this.code;
        }

        public String getDesc() {
            return this.desc;
        }
    }
}
