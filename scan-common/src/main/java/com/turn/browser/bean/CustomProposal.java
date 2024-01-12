package com.turn.browser.bean;

import com.turn.browser.dao.entity.Proposal;
import lombok.Data;

import java.util.*;

/**
 * @Description: Proposal entity extension class
 */
@Data
public class CustomProposal extends Proposal {
    //vote in favor
    private List <CustomVote> yesList = new ArrayList <>();
    //Veto vote
    private List <CustomVote> noList = new ArrayList <>();
    //Abstain vote
    private List <CustomVote> abstentionList = new ArrayList <>();

    public static final String QUERY_FLAG = "inquiry";

    public CustomProposal(){
        super();
        Date date = new Date();
        this.setCreateTime(date);
        this.setUpdateTime(date);
    }

    /**
     * Proposal type enumeration class:
     * 1. Text proposal
     * 2. Upgrade proposal
     * 3. Parameter proposal
     * 4. Cancel proposal
     */
    public enum TypeEnum {
        TEXT(1, "Text Proposal"),
        UPGRADE(2, "Upgrade Proposal"),
        PARAMETER(3, "Parameter proposal"),
        CANCEL(4, "Cancel proposal");
        private int code;
        private String desc;
        TypeEnum ( int code, String desc ) {
            this.code = code;
            this.desc = desc;
        }
        public int getCode () {
            return code;
        }
        public String getDesc () {
            return desc;
        }
        private static final Map <Integer, TypeEnum> ENUMS = new HashMap <>();
        static {
            Arrays.asList(TypeEnum.values()).forEach(en -> ENUMS.put(en.code, en));
        }
        public static TypeEnum getEnum ( int code ) {
            return ENUMS.get(code);
        }
        public static boolean contains ( int code ) {
            return ENUMS.containsKey(code);
        }
        public static boolean contains ( TypeEnum en ) {
            return ENUMS.containsValue(en);
        }
    }

    public enum StatusEnum {
        VOTING(1, "Voting"),
        PASS(2, "pass"),
        FAIL(3, "Failure"),
        PRE_UPGRADE(4, "Pre-upgrade"),
        FINISH(5, "effective"),
        CANCEL(6, "Cancelled");
        private int code;
        private String desc;
        StatusEnum ( int code, String desc ) {
            this.code = code;
            this.desc = desc;
        }
        public int getCode () {
            return code;
        }
        public String getDesc () {
            return desc;
        }
        private static final Map <Integer, StatusEnum> ENUMS = new HashMap <>();
        static {
            Arrays.asList(StatusEnum.values()).forEach(en -> ENUMS.put(en.code, en));
        }
        public static StatusEnum getEnum ( Integer code ) {
            return ENUMS.get(code);
        }
        public static boolean contains ( int code ) {
            return ENUMS.containsKey(code);
        }
        @SuppressWarnings("unlikely-arg-type")
		public static boolean contains ( TypeEnum en ) {
            return ENUMS.containsValue(en);
        }
    }
    /**
     * Proposal information synchronization type enumeration class:
     * 1. Completed
     * 2. Not completed
     */
    public enum FlagEnum {
        COMPLETE(1, "Completed"),
        INCOMPLETE(2, "Unfinished");
        private int code;
        private String desc;
        FlagEnum ( int code, String desc ) {
            this.code = code;
            this.desc = desc;
        }
        public int getCode () {
            return code;
        }
        public String getDesc () {
            return desc;
        }
        private static final Map <Integer, FlagEnum> ENUMS = new HashMap <>();
        static {
            Arrays.asList(FlagEnum.values()).forEach(en -> ENUMS.put(en.code, en));
        }
        public static FlagEnum getEnum ( int code ) {
            return ENUMS.get(code);
        }
        public static boolean contains ( int code ) {
            return ENUMS.containsKey(code);
        }
        public static boolean contains ( FlagEnum en ) {
            return ENUMS.containsValue(en);
        }
    }
}
