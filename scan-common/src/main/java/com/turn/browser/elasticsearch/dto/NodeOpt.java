package com.turn.browser.elasticsearch.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class NodeOpt {
    private Long id;
    private String nodeId;
    private Integer type;
    private String txHash;
    private Long bNum;
    private Date time;
    private String desc;
    private Date creTime;
    private Date updTime;

    /**
     * 1 create
     2 modify
     3 quit
     4 proposals
     5 vote
     6 multisign double signature
     7 lowBlockRate The block rate is low
     11 Unlock
     */
    public enum TypeEnum{
        CREATE("1", "Create",""),
        MODIFY("2", "Modify","BEFORERATE|AFTERRATE"),
        QUIT("3", "Quit",""),
        PROPOSALS("4", "Proposal","ID|TITLE|TYPE|VERSION"),
        PARAMETER("9", "Parameter proposal","ID|TITLE|TYPE|MODULE|NAME|VALUE"),
        VOTE("5", "Vote","ID|TITLE|OPTION|TYPE|VERSION"),
        MULTI_SIGN("6", "Double Sign","PERCENT|AMOUNT"),
        LOW_BLOCK_RATE("7", "Low block rate","BLOCK_COUNT|SLASH_BLOCK_COUNT|AMOUNT|KICK_OUT"),
        VERSION("8", "Version Statement","NODE_NAME|ACTIVE_NODE|VERSION"),
        INCREASE("10", "Increase pledge",""),
        UNLOCKED("11", "Unlock","LOCKED_EPOCH|UNLOCKED_EPOCH|FREEZE_DURATION"),
        ;
        private String code;
        private String desc;
        private String tpl;
        TypeEnum(String code, String desc,String tpl) {
            this.code = code;
            this.desc = desc;
            this.tpl = tpl;
        }
        public String getCode(){return code;}
        public String getDesc(){return desc;}
        public String getTpl(){return tpl;}
        private static final Map<String,TypeEnum> ENUMS = new HashMap<>();
        static {
            Arrays.asList(TypeEnum.values()).forEach(en->ENUMS.put(en.code,en));}
        public static TypeEnum getEnum(String code){
            return ENUMS.get(code);
        }
        public static boolean contains(String code){return ENUMS.containsKey(code);}
        public static boolean contains(TypeEnum en){return ENUMS.containsValue(en);}
    }
}