package com.turn.browser.bean;

import com.turn.browser.dao.entity.Vote;
import lombok.Data;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: Voting proposal entity extension class
 */
@Data
public class CustomVoteProposal extends Vote {
	private String pproposalHash;
    private String type;
    private String proposalVerifier;
    private String proposalVerifierName;
    private String url;
    private String newVersion;
    private String endVotingBlock;
    private String activeBlock;
    private Date proposalTimestamp;
    private Long yeas;
    private Long nays;
    private Long abstentions;
    private Long accuVerifiers;
    private Integer status;
    private Date proposalUpdateTime;
    private Date proposalCreateTime;
    private String pipNum;
    private String pipId;
    private String topic;
    private String description;
    private String canceledPipId;
    private String canceledTopic;

    public CustomVoteProposal(){
        Date date = new Date();
        this.setCreateTime(date);
        this.setUpdateTime(date);
    }

    /**
     * Voting type enumeration class:
     * 1.Support
     * 2.Objection
     * 3. Abstain
     */
    public enum OptionEnum {
        SUPPORT("1", "support"),
        OPPOSITION("2", "Objection"),
        ABSTENTION("3", "Abstain"),
        INVALID_SUPPORT("11", "Supported (invalid)"),
        INVALID_OPPOSITION("12", "Objection (invalid)"),
        INVALID_ABSTENTION("13", "Abstention (invalid)");
        private String code;
        private String desc;
        OptionEnum ( String code, String desc ) {
            this.code = code;
            this.desc = desc;
        }
        public String getCode () {
            return code;
        }
        public String getDesc () {
            return desc;
        }
        private static final Map<String, OptionEnum> ENUMS = new HashMap<>();
        static {
            Arrays.asList(OptionEnum.values()).forEach(en -> ENUMS.put(en.code, en));
        }
        public static OptionEnum getEnum ( String code ) {
            return ENUMS.get(code);
        }
        public static boolean contains ( String code ) {
            return ENUMS.containsKey(code);
        }
        public static boolean contains ( OptionEnum en ) {
            return ENUMS.containsValue(en);
        }
    }
}
