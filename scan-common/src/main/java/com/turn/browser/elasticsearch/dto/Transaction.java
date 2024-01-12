package com.turn.browser.elasticsearch.dto;

import java.math.BigDecimal;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Transaction {

    private Long id;

    private String hash;

    private String bHash;

    private Long num;

    private Integer index;

    private Date time;

    private String nonce;

    private Integer status;

    private String gasPrice;

    private String gasUsed;

    private String gasLimit;

    private String from;

    private String to;

    private String value;

    private Integer type;

    private String cost;

    private Integer toType;

    private Long seq;

    private Date creTime;

    private Date updTime;

    private String input;

    private String info;

    private String erc1155TxInfo; // json array string to store basic information, from erc1155TxList

    private String erc721TxInfo; // json array string to store basic information, from erc721TxList

    private String erc20TxInfo; // json array string to store basic information, from erc20TxList

    private String transferTxInfo; // json array string that stores basic information, from transferTxList

    private String pposTxInfo; // json array string to store basic information, from pposTxList

    private String texasHoldemTxInfo;

    private List<String> gameContractEventInfo;

    private String failReason;

    private Integer contractType;

    private String method;

    private String bin;

    private String contractAddress;

    /**
     * erc1155 transaction list
     */
    @JSONField(serialize = false)
    private List<ErcTx> erc1155TxList = new ArrayList<>();

    /**
     * erc721 transaction list
     */
    @JSONField(serialize = false)
    private List<ErcTx> erc721TxList = new ArrayList<>();

    /**
     * erc20 transaction list
     */
    @JSONField(serialize = false)
    private List<ErcTx> erc20TxList = new ArrayList<>();

    /**
     * Internal transfer transactions
     */
    @JSONField(serialize = false)
    private List<Transaction> transferTxList = new ArrayList<>();

    /**
     * PPOS call transaction
     */
    @JSONField(serialize = false)
    private List<Transaction> pposTxList = new ArrayList<>();

    /**
     * Virtual trading
     */
    @JSONField(serialize = false)
    private List<Transaction> virtualTransactions = new ArrayList<>();

    /******** Convenient method to convert string values into large floating point numbers ********/
    public BigDecimal decimalGasLimit() {
        return new BigDecimal(this.getGasLimit());
    }

    public BigDecimal decimalGasPrice() {
        return new BigDecimal(this.getGasPrice());
    }

    public BigDecimal decimalGasUsed() {
        return new BigDecimal(this.getGasUsed());
    }

    public BigDecimal decimalValue() {
        return new BigDecimal(this.getValue());
    }

    public BigDecimal decimalCost() {
        return new BigDecimal(this.getCost());
    }

    public enum TypeEnum {
        /**
         * 0-Transfer
         */
        TRANSFER(0, "Transfer"),
        /**
         *Contract destroyed
         */
        CONTRACT_EXEC_DESTROY(21, "Contract Destruction"),
        /**
         * 1-EVM contract release (contract creation)
         */
        EVM_CONTRACT_CREATE(1, "EVM contract release (contract creation)"),
        /**
         * 2-Contract call (contract execution)
         */
        CONTRACT_EXEC(2, "Contract call (contract execution)"),
        /**
         * 3-WASM contract release (contract creation)
         */
        WASM_CONTRACT_CREATE(3, "WASM contract release (contract creation)"),
        /**
         * 4-MPC transaction
         */
        OTHERS(4, "Others"),
        /**
         * 5-MPC transaction
         */
        MPC(5, "MPC Transaction"),
        /**
         * 6-ERC20 contract release (contract creation)
         */
        ERC20_CONTRACT_CREATE(6, "ERC20 contract release (contract creation)"),
        /**
         * 7-ERC20 contract call (contract execution)
         */
        ERC20_CONTRACT_EXEC(7, "ERC20 contract call (contract execution)"),
        /**
         * 8-ERC721 contract release (contract creation)
         */
        ERC721_CONTRACT_CREATE(8, "ERC721 contract release (contract creation)"),
        /**
         * 9-ERC721 contract call (contract execution)
         */
        ERC721_CONTRACT_EXEC(9, "ERC721 contract call (contract execution)"),
        /**
         * 10-ERC1155 contract release (contract creation)
         */
        ERC1155_CONTRACT_CREATE(10, "ERC1155 contract release (contract creation)"),
        /**
         * 11-ERC1155 contract call (contract execution)
         */
        ERC1155_CONTRACT_EXEC(11, "ERC1155 contract call (contract execution)"),
        /**
         * 12-Game contract call (contract execution)
         */
        GAME_CONTRACT_EXEC(12, "Game contract call (contract execution)"),
        /**
         * 1000-initiate pledge (create validator)
         */
        STAKE_CREATE(1000, "Initiate pledge (create validator)"),
        /**
         * 1001-Modify pledge information (edit validator)
         */
        STAKE_MODIFY(1001, "Modify pledge information (edit validator)"),
        /**
         * 1002- Increase pledge (increase own pledge)
         */
        STAKE_INCREASE(1002, "Increase pledge (increase own pledge)"),
        /**
         * 1003-Revoke pledge (exit validator)
         */
        STAKE_EXIT(1003, "Revoke pledge (exit validator)"),
        /**
         * 1004-Initiate delegation (commission)
         */
        DELEGATE_CREATE(1004, "Initiate delegation (delegation)"),
        /**
         * 1005-Reduction of holdings/cancellation of entrustment (redemption entrustment)
         */
        DELEGATE_EXIT(1005, "Reduction of holdings/cancellation of commission (redemption commission)"),
        /**
         * 1005-Reduction of holdings/cancellation of entrustment (redemption entrustment)
         */
        REDEEM_DELEGATION(1006, "Receive the unlocked commission"),
        /**
         * 2000-Submit text proposal (Create proposal)
         */
        PROPOSAL_TEXT(2000, "Submit text proposal (Create proposal)"),
        /**
         * 2001-Submit upgrade proposal (create proposal)
         */
        PROPOSAL_UPGRADE(2001, "Submit an upgrade proposal (create proposal)"),
        /**
         * 2002-Submit parameter proposal (create proposal)
         */
        PROPOSAL_PARAMETER(2002, "Submit parameter proposal (create proposal)"),
        /**
         *2005-Cancellation proposal submitted
         */
        PROPOSAL_CANCEL(2005, "Submit cancellation proposal"),
        /**
         * 2003-vote for proposals (proposal voting)
         */
        PROPOSAL_VOTE(2003, "Vote for the proposal (vote for the proposal)"),
        /**
         *2004-Version Statement
         */
        VERSION_DECLARE(2004, "Version Statement"),
        /**
         * 3000-Report multi-signature (report verifier)
         */
        REPORT(3000, "Report multi-signature (report verifier)"),
        /**
         * 4000-Create a lock-up plan (create a lock-up)
         */
        RESTRICTING_CREATE(4000, "Create Hedging Plan (Create Hedging)"),
        /**
         * 5000- claim the reward
         */
        CLAIM_REWARDS(5000, "Claim rewards"),
        /**
         * 6000-coins
         */
        MINT_TOKEN(6000, "Minting"),
        /**
         *6001-Settlement
         */
        SETTLE_BUBBLEL2(6001, "Settlement"),
        /**
         * 7000-node pledge
         */
        CREATE_STAKING(7000, "Node Staking"),
        /**
         * 7001-Update node information
         */
        EDIT_CANDIDATE(7001, "Update node information"),
        /**
         * 7003-Node unstaking
         */
        WITHDREW_STAKING(7003, "Node unstaking"),
        /**
         * 8001-Create Bubble
         */
        CREATE_BUBBLE(8001, "Create Bubble"),
        /**
         * Release Bubble
         */
        RELEASE_BUBBLE(8002, "Release Bubble"),
        /**
         * 8003-Bet Token
         */
        STAKING_TOKEN(8003, "Staking Token"),
        /**
         *8004-Redemption Token
         */
        WITHDREW_TOKEN(8004, "Redeem Token"),
        /**
         *8005-Settlement
         */
        SETTLE_BUBBLE(8005, "Settlement");

        private static Map<Integer, TypeEnum> map = new HashMap<>();

        static {
            Arrays.asList(TypeEnum.values()).forEach(typeEnum -> map.put(typeEnum.code, typeEnum));
        }

        public static TypeEnum getEnum(int code) {
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

    /**
     * Transaction result success or failure enumeration class: 1. Success 2. Failure
     */
    public enum StatusEnum {
        SUCCESS(1, "Success"), FAILURE(2, "Failure");

        private int code;

        private String desc;

        StatusEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return this.code;
        }

        public String getDesc() {
            return this.desc;
        }

        private static final Map<Integer, StatusEnum> ENUMS = new HashMap<>();

        static {
            Arrays.asList(StatusEnum.values()).forEach(en -> ENUMS.put(en.code, en));
        }

        public static StatusEnum getEnum(Integer code) {
            return ENUMS.get(code);
        }

        public static boolean contains(int code) {
            return ENUMS.containsKey(code);
        }

        public static boolean contains(StatusEnum en) {
            return ENUMS.containsValue(en);
        }
    }

    /**
     * Transaction recipient type (to is a contract or an account): Address type: 1 account, 2 built-in contract, 3EVM contract, 4WASM contract
     */
    public enum ToTypeEnum {
        ACCOUNT(1, "Account"), INNER_CONTRACT(2, "Built-in Contract"), EVM_CONTRACT(3, "EVM Contract"), WASM_CONTRACT(4, "WASM Contract"),
        ERC20_CONTRACT(5, "ERC20-EVM Contract"), ERC721_CONTRACT(6, "ERC721-EVM Contract"),
        ERC1155_CONTRACT(7, "ERC1155-EVM Contract"),GAME_CONTRACT(8, "GAME-EVM Contract");

        private int code;

        private String desc;

        ToTypeEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return this.code;
        }

        public String getDesc() {
            return this.desc;
        }

        private static final Map<Integer, ToTypeEnum> ENUMS = new HashMap<>();

        static {
            Arrays.asList(ToTypeEnum.values()).forEach(en -> ENUMS.put(en.code, en));
        }

        public static ToTypeEnum getEnum(Integer code) {
            return ENUMS.get(code);
        }

        public static boolean contains(int code) {
            return ENUMS.containsKey(code);
        }

        public static boolean contains(ToTypeEnum en) {
            return ENUMS.containsValue(en);
        }
    }

    /**
     * Get the transaction type enumeration of the current transaction
     *
     * @return
     */
    @JsonIgnore
    public TypeEnum getTypeEnum() {
        return TypeEnum.getEnum(this.getType());
    }

    /**
     * Get transaction parameter information object based on type
     *
     * @return
     */
    @JsonIgnore
    public <T> T getTxParam(Class<T> clazz) {
        return JSON.parseObject(this.getInfo(), clazz);
    }

}