package com.turn.browser.bean;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.turn.browser.dao.entity.Address;

import lombok.Data;

/**
 * @Description: Address entity extension class
 */
@Data
public class CustomAddress extends Address {

    public CustomAddress() {
        super();
        Date date = new Date();
        this.setUpdateTime(date);
        this.setCreateTime(date);
        /* Initialize default value */
        this.setBalance(BigDecimal.ZERO);
        this.setRestrictingBalance(BigDecimal.ZERO);
        this.setStakingValue(BigDecimal.ZERO);
        this.setDelegateValue(BigDecimal.ZERO);
        this.setRedeemedValue(BigDecimal.ZERO);
        this.setTxQty(BigInteger.ZERO.intValue());
        this.setTransferQty(BigInteger.ZERO.intValue());
        this.setStakingQty(BigInteger.ZERO.intValue());
        this.setDelegateQty(BigInteger.ZERO.intValue());
        this.setProposalQty(BigInteger.ZERO.intValue());
        this.setCandidateCount(BigInteger.ZERO.intValue());
        this.setDelegateHes(BigDecimal.ZERO);
        this.setDelegateLocked(BigDecimal.ZERO);
        this.setContractName("");
        this.setContractCreate("");
        this.setContractCreatehash("");
    }

    /**
     * Address type: 1 account, 2 built-in contracts, 3 EVM contracts, 4 WASM contracts
     */
    public enum TypeEnum {
        ACCOUNT(1, "Account"),
        INNER_CONTRACT(2, "Built-in Contract"),
        EVM(3, "3EVM Contract"),
        WASM(4, "WASM Contract"),
        ERC20_EVM(5, "ERC20 Contract"),
        ERC721_EVM(6, "ERC721 Contract"),
        ERC1155_EVM(7, "ERC1155 Contract");

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

        private static final Map<Integer, TypeEnum> ENUMS = new HashMap<>();
        static {
            Arrays.asList(TypeEnum.values()).forEach(en -> ENUMS.put(en.code, en));
        }

        public static TypeEnum getEnum(Integer code) {
            return ENUMS.get(code);
        }

        public static boolean contains(int code) {
            return ENUMS.containsKey(code);
        }

        public static boolean contains(CustomStaking.StatusEnum en) {
            return ENUMS.containsValue(en);
        }
    }
}
