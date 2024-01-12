package com.turn.browser.bean;

import com.turn.browser.dao.entity.Staking;
import com.turn.browser.dao.entity.StakingKey;
import com.turn.browser.utils.HexUtil;
import com.turn.browser.utils.ChainVersionUtil;
import com.bubble.contracts.dpos.dto.resp.Node;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * @Description: Pledge entity extension class
 */
@Data
public class CustomStaking extends Staking {
    private BigDecimal slashAmount;
    public CustomStaking() {
        super();
        Date date = new Date();
        this.setCreateTime(date);
        this.setUpdateTime(date);
        this.setJoinTime(date);
        /* Initialize default value */
        // Pledge amount (demurrage amount)
        this.setStakingHes(BigDecimal.ZERO);
// //   Pledge amount (locked amount)
        this.setStakingLocked(BigDecimal.ZERO);
// //   Total amount of entrusted transactions (amount during the hesitation period)
        this.setStatDelegateHes(BigDecimal.ZERO);
// //   Total amount of entrusted transactions (lock-up period amount)
        this.setStatDelegateLocked(BigDecimal.ZERO);
        setStatDelegateReleased(BigDecimal.ZERO);
// // Pledge amount (refunded amount)
        this.setStakingReduction(BigDecimal.ZERO);
// // Node name (pledge node name)
        this.setNodeName("Unknown");
// // Node head image (associated with external_id, obtained by third-party software)
        this.setNodeIcon("");
// // Estimated annualized rate
        this.setAnnualizedRate(0.0);
// // Block reward
        this.setBlockRewardValue(BigDecimal.ZERO);
//        // Program Version
        this.setProgramVersion(BigInteger.ZERO.toString());
// // Staking rewards
        this.setStakingRewardValue(BigDecimal.ZERO);
// // Billing cycle identifier
        this.setStakingReductionEpoch(0);
// // Number of times entering consensus verification theory
        this.setStakingReductionEpoch(0);
// // Number of blocks produced in the last consensus cycle
        this.setPreConsBlockQty(0l);
// // Number of blocks produced in the current consensus cycle
        this.setCurConsBlockQty(0l);
// // Node status 1: Candidate 2: Exiting 3: Exited
        this.setStatus(StatusEnum.CANDIDATE.code);
// //Whether it is a validator for the settlement cycle
        this.setIsSettle(YesNoEnum.NO.code);
// //Whether the consensus cycle validator is
        this.setIsConsensus(YesNoEnum.NO.code);
// // Whether it is a built-in candidate during chain initialization
        this.setIsInit(YesNoEnum.NO.code);
// //Node staking period handling fee
        this.setFeeRewardValue(BigDecimal.ZERO);
    }

    /**
     * Update staking information using node information
     * @param verifier
     */
    public void updateWithVerifier(Node verifier){
        // Pledge block height
        if(verifier.getStakingBlockNum()!=null) this.setStakingBlockNum(verifier.getStakingBlockNum().longValue());
        //Pledge node address
        this.setNodeId(HexUtil.prefix(verifier.getNodeId()));
        // Index for initiating pledge transactions
        if(verifier.getStakingTxIndex()!=null) this.setStakingTxIndex(verifier.getStakingTxIndex().intValue());
        //The account address that initiated the pledge
        this.setStakingAddr(verifier.getStakingAddress());
        // Third-party social software associated id
        this.setExternalId(verifier.getExternalId());
        //Income address
        this.setBenefitAddr(verifier.getBenifitAddress());
//      Node status 1: Candidate 2: Exiting 3: Exited
        if(verifier.getStatus()!=null) this.setStatus(verifier.getStatus().intValue());
//      Node name (pledge node name)
        this.setNodeName(StringUtils.isBlank(verifier.getNodeName())?this.getNodeName():verifier.getNodeName());
        // Third-party homepage of node
        this.setWebSite(verifier.getWebsite());
        this.setDetails(verifier.getDetails());

        // program version
        BigInteger programVersion=verifier.getProgramVersion();
        BigInteger bigVersion = ChainVersionUtil.toBigVersion(programVersion);
        this.setProgramVersion(programVersion.toString());
        this.setBigVersion(bigVersion.toString());
    }

    /**
     * Update staking information using node information
     * @param candidate
     */
    public void updateWithCandidate(Node candidate){
        // Set node name
        String nodeName = candidate.getNodeName();
        if(StringUtils.isNotBlank(nodeName)) setNodeName(nodeName);
        // Set program version number
        String programVersion=candidate.getProgramVersion().toString();
        if(StringUtils.isNotBlank(programVersion)){
            setProgramVersion(programVersion);
            BigInteger bigVersion = ChainVersionUtil.toBigVersion(candidate.getProgramVersion());
            setBigVersion(bigVersion.toString());
        }
        // Set external ID
        String externalId = candidate.getExternalId();
        if(StringUtils.isNotBlank(externalId)) setExternalId(externalId);
        // Set benefit address
        String benefitAddr = candidate.getBenifitAddress();
        if(StringUtils.isNotBlank(benefitAddr)) setBenefitAddr(benefitAddr);
        // Setup details
        String details = candidate.getDetails();
        if(StringUtils.isNotBlank(details)) setDetails(details);
        // set website
        String website = candidate.getWebsite();
        if(StringUtils.isNotBlank(website)) setWebSite(website);
        // Set staking amount
        if(candidate.getShares()!=null&&candidate.getShares().compareTo(BigInteger.ZERO)>0){
            setStakingLocked(new BigDecimal(candidate.getShares()));
        }
    }

    /**
     * Pledge status type enumeration class:
     * 1. Candidate
     * 2. Exiting
     * 3.Exited
     */
    public enum StatusEnum{
        CANDIDATE(1, "Candidate"),
        EXITING(2, "Exiting"),
        EXITED(3, "Exited"),
        LOCKED(4, "Locked"),
        ;
        private int code;
        private String desc;
        StatusEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        public int getCode(){return code;}
        public String getDesc(){return desc;}
        private static final Map<Integer, StatusEnum> ENUMS = new HashMap<>();
        static {
            Arrays.asList(StatusEnum.values()).forEach(en->ENUMS.put(en.code,en));}
        public static StatusEnum getEnum(Integer code){
            return ENUMS.get(code);
        }
        public static boolean contains(int code){return ENUMS.containsKey(code);}
        public static boolean contains(StatusEnum en){return ENUMS.containsValue(en);}
    }
    /**
     * Pledge node - whether the consensus cycle validator type/whether the settlement cycle validator type enumeration class:
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

    public enum ExceptionStatusEnum {
        NORMAL(1, "normal"),
        LOW_RATE(2, "Low block exception"),
        MULTI_SIGN(3, "Double signature exception"),
        LOW_RATE_SLASHED(4, "Penalized for low block rate"),
        MULTI_SIGN_SLASHED(5, "Penalized for double signing");

        private int code;
        private String desc;

        ExceptionStatusEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    @Override
    public boolean equals ( Object o ) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StakingKey that = (StakingKey) o;
        return Objects.equals(this.getStakingBlockNum(), that.getStakingBlockNum()) &&
                Objects.equals(this.getNodeId(), that.getNodeId());
    }

    @Override
    public int hashCode () {
        return Objects.hash(this.getStakingBlockNum(), this.getNodeId());
    }
}
