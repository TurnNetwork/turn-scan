package com.turn.browser.response.transaction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;
import com.turn.browser.config.json.CustomVersionSerializer;

import java.math.BigDecimal;
import java.util.List;

/**
 * Transaction details return object
 */
public class TransactionDetailsResp {

    private String txHash; // transaction hash

    private String from; // sender address

    private String to; // Receiver address (operation address)

    private Long timestamp; // transaction time

    private Long serverTime; // server time

    private String confirmNum; // Number of block confirmations

    private Long blockNumber; // The block height of the transaction

    private String gasLimit; // Energy limit

    private String gasUsed; // Energy consumption

    private BigDecimal gasPrice; // Energy price

    private BigDecimal value; // Amount (unit: AAA)

    private BigDecimal actualTxCost; // Transaction fee (unit: AAA)

    private String txType; // Transaction type

    private String input; // Additional input data

    private String txInfo; //Additional input data parsed structure

    private String failReason; // Reason for failure

    private Boolean first; // Whether it is the first record

    private Boolean last; // Whether it is the last record

    private String receiveType; // This field represents the account type stored in the to field: account-wallet address, contract-contract address,

    private Integer contractType; //Contract type 1-evm contract 2-wasm contract

    private String method; //Contract calling function

    private String contractName; //Contract name

    private String rPAccount; //The address of the lock-up plan

    private BigDecimal rPNum;

    private List<TransactionDetailsRPPlanResp> rPPlan;

    private List<TransactionDetailsEvidencesResp> evidences;

    private String nodeId; // node id

    private String nodeName; // node name

    private String benefitAddr; // Profit account used to receive block rewards and staking rewards

    private String externalId; // External Id (limited in length, used to pull the ID of the node description to a third party)
    private String externalUrl; // external url

    private String website; // The third-party homepage of the node (with a length limit, indicating the homepage of the node)

    private String details; // Description of the node (with a length limit, indicating the description of the node)

    private String programVersion; // The real version of the program, obtained through rpc

    private BigDecimal applyAmount; //Amount applied for redemption

    private BigDecimal redeemLocked; // The amount locked during redemption

    private Integer redeemStatus; // Redemption status, 1: Returning 2: Returning successfully

    private String redeemUnLockedBlock; // The block expected to be redeemed

    private String proposalUrl; // The github address of the proposal https://github.com/ethereum/EIPs/blob/master/EIPS/eip-100.md eip-100 is the proposal id

    private String proposalHash; // proposal id

    private String proposalOption; // Voting 1: Text proposal 2: Upgrade proposal 3: Parameter proposal

    private String proposalNewVersion; // Upgrade proposal version

    private String declareVersion; // declared version

    private Integer txReceiptStatus; // Transaction status

    private String voteStatus; // Voting options 1: Support 2: Oppose 3: Abstain

    private String evidence;//evidence

    private Integer reportType; // Report type: 1: Block double signature

    private BigDecimal reportRewards;//Report rewards

    private Integer reportStatus; // Report status \r\n1: Failure \r\n2: Success

    private String pipNum;//Proposal pip number

    private Integer proposalStatus;//Proposal status\r\n1: Voting\r\n2: Passed\r\n3: Failed\r\n4: Pre-upgrade\r\n5: Upgrade completed

    private String proposalTitle;//proposal title

    private String preHash; // Previous record

    private String nextHash; // next record

    private BigDecimal txAmount; // Transaction fee

    private String delegationRatio; // delegation ratio

    private List<TransactionDetailsRewardsResp> rewards;

    // private String innerFrom; // internal transaction from
    // private String innerTo; // internal transaction to
    // private String innerValue; // internal transaction value
    // private String innerContractAddr; // Internal transaction corresponding address
    // private String innerContractName; // Corresponding name of internal transaction
    // private String innerSymbol; // Internal transaction corresponding unit

    private List<Arc20Param> erc20Params;

    private List<Arc721Param> erc721Params;

    private List<Arc1155Param> erc1155Params;

    private List<TexasHoldemParam> texasHoldemParam;

    private BigDecimal redeemDelegationValue;

    public String getTxHash() {
        return this.txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getServerTime() {
        return this.serverTime;
    }

    public void setServerTime(Long serverTime) {
        this.serverTime = serverTime;
    }

    public String getConfirmNum() {
        return this.confirmNum;
    }

    public void setConfirmNum(String confirmNum) {
        this.confirmNum = confirmNum;
    }

    public Long getBlockNumber() {
        return this.blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getGasLimit() {
        return this.gasLimit;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }

    public String getGasUsed() {
        return this.gasUsed;
    }

    public void setGasUsed(String gasUsed) {
        this.gasUsed = gasUsed;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getGasPrice() {
        return this.gasPrice;
    }

    public void setGasPrice(BigDecimal gasPrice) {
        this.gasPrice = gasPrice;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getValue() {
        return this.value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getActualTxCost() {
        return this.actualTxCost;
    }

    public void setActualTxCost(BigDecimal actualTxCost) {
        this.actualTxCost = actualTxCost;
    }

    public String getTxType() {
        return this.txType;
    }

    public void setTxType(String txType) {
        this.txType = txType;
    }

    public String getInput() {
        return this.input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getTxInfo() {
        return this.txInfo;
    }

    public void setTxInfo(String txInfo) {
        this.txInfo = txInfo;
    }

    public String getFailReason() {
        return this.failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public Boolean getFirst() {
        return this.first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    public Boolean getLast() {
        return this.last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public String getReceiveType() {
        return this.receiveType;
    }

    public void setReceiveType(String receiveType) {
        this.receiveType = receiveType;
    }

    public String getRPAccount() {
        return this.rPAccount;
    }

    public void setRPAccount(String rPAccount) {
        this.rPAccount = rPAccount;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getRPNum() {
        return this.rPNum;
    }

    public void setRPNum(BigDecimal rPNum) {
        this.rPNum = rPNum;
    }

    public List<TransactionDetailsRPPlanResp> getRPPlan() {
        return this.rPPlan;
    }

    public void setRPPlan(List<TransactionDetailsRPPlanResp> rPPlan) {
        this.rPPlan = rPPlan;
    }

    public List<TransactionDetailsEvidencesResp> getEvidences() {
        return this.evidences;
    }

    public void setEvidences(List<TransactionDetailsEvidencesResp> evidences) {
        this.evidences = evidences;
    }

    public String getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return this.nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getBenefitAddr() {
        return this.benefitAddr;
    }

    public void setBenefitAddr(String benefitAddr) {
        this.benefitAddr = benefitAddr;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getWebsite() {
        return this.website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDetails() {
        return this.details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @JsonSerialize(using = CustomVersionSerializer.class)
    public String getProgramVersion() {
        return this.programVersion;
    }

    public void setProgramVersion(String programVersion) {
        this.programVersion = programVersion;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getApplyAmount() {
        return this.applyAmount;
    }

    public void setApplyAmount(BigDecimal applyAmount) {
        this.applyAmount = applyAmount;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getRedeemLocked() {
        return this.redeemLocked;
    }

    public void setRedeemLocked(BigDecimal redeemLocked) {
        this.redeemLocked = redeemLocked;
    }

    public Integer getRedeemStatus() {
        return this.redeemStatus;
    }

    public void setRedeemStatus(Integer redeemStatus) {
        this.redeemStatus = redeemStatus;
    }

    public String getRedeemUnLockedBlock() {
        return this.redeemUnLockedBlock;
    }

    public void setRedeemUnLockedBlock(String redeemUnLockedBlock) {
        this.redeemUnLockedBlock = redeemUnLockedBlock;
    }

    public String getProposalUrl() {
        return this.proposalUrl;
    }

    public void setProposalUrl(String proposalUrl) {
        this.proposalUrl = proposalUrl;
    }

    public String getProposalHash() {
        return this.proposalHash;
    }

    public void setProposalHash(String proposalHash) {
        this.proposalHash = proposalHash;
    }

    public String getProposalOption() {
        return this.proposalOption;
    }

    public void setProposalOption(String proposalOption) {
        this.proposalOption = proposalOption;
    }

    @JsonSerialize(using = CustomVersionSerializer.class)
    public String getProposalNewVersion() {
        return this.proposalNewVersion;
    }

    public void setProposalNewVersion(String proposalNewVersion) {
        this.proposalNewVersion = proposalNewVersion;
    }

    @JsonSerialize(using = CustomVersionSerializer.class)
    public String getDeclareVersion() {
        return this.declareVersion;
    }

    public void setDeclareVersion(String declareVersion) {
        this.declareVersion = declareVersion;
    }

    public Integer getTxReceiptStatus() {
        return this.txReceiptStatus;
    }

    public void setTxReceiptStatus(Integer txReceiptStatus) {
        this.txReceiptStatus = txReceiptStatus;
    }

    public String getEvidence() {
        return this.evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public Integer getReportType() {
        return this.reportType;
    }

    public void setReportType(Integer reportType) {
        this.reportType = reportType;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getReportRewards() {
        return this.reportRewards;
    }

    public void setReportRewards(BigDecimal reportRewards) {
        this.reportRewards = reportRewards;
    }

    public Integer getReportStatus() {
        return this.reportStatus;
    }

    public void setReportStatus(Integer reportStatus) {
        this.reportStatus = reportStatus;
    }

    public String getPipNum() {
        return this.pipNum;
    }

    public void setPipNum(String pipNum) {
        this.pipNum = pipNum;
    }

    public Integer getProposalStatus() {
        return this.proposalStatus;
    }

    public void setProposalStatus(Integer proposalStatus) {
        this.proposalStatus = proposalStatus;
    }

    public String getProposalTitle() {
        return this.proposalTitle;
    }

    public void setProposalTitle(String proposalTitle) {
        this.proposalTitle = proposalTitle;
    }

    public String getPreHash() {
        return this.preHash;
    }

    public void setPreHash(String preHash) {
        this.preHash = preHash;
    }

    public String getNextHash() {
        return this.nextHash;
    }

    public void setNextHash(String nextHash) {
        this.nextHash = nextHash;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getTxAmount() {
        return this.txAmount;
    }

    public void setTxAmount(BigDecimal txAmount) {
        this.txAmount = txAmount;
    }

    public String getVoteStatus() {
        return this.voteStatus;
    }

    public void setVoteStatus(String voteStatus) {
        this.voteStatus = voteStatus;
    }

    public String getExternalUrl() {
        return this.externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getrPAccount() {
        return this.rPAccount;
    }

    public void setrPAccount(String rPAccount) {
        this.rPAccount = rPAccount;
    }

    public BigDecimal getrPNum() {
        return this.rPNum;
    }

    public void setrPNum(BigDecimal rPNum) {
        this.rPNum = rPNum;
    }

    public List<TransactionDetailsRPPlanResp> getrPPlan() {
        return this.rPPlan;
    }

    public void setrPPlan(List<TransactionDetailsRPPlanResp> rPPlan) {
        this.rPPlan = rPPlan;
    }

    public String getDelegationRatio() {
        return this.delegationRatio;
    }

    public void setDelegationRatio(String delegationRatio) {
        this.delegationRatio = delegationRatio;
    }

    public List<TransactionDetailsRewardsResp> getRewards() {
        return this.rewards;
    }

    public void setRewards(List<TransactionDetailsRewardsResp> rewards) {
        this.rewards = rewards;
    }

    public Integer getContractType() {
        return this.contractType;
    }

    public void setContractType(Integer contractType) {
        this.contractType = contractType;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getContractName() {
        return this.contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public List<Arc721Param> getErc721Params() {
        return erc721Params;
    }

    public void setErc721Params(List<Arc721Param> erc721Params) {
        this.erc721Params = erc721Params;
    }

    public List<Arc20Param> getErc20Params() {
        return erc20Params;
    }

    public void setErc20Params(List<Arc20Param> erc20Params) {
        this.erc20Params = erc20Params;
    }

    public List<Arc1155Param> getErc1155Params() {
        return erc1155Params;
    }

    public void setErc1155Params(List<Arc1155Param> erc1155Params) {
        this.erc1155Params = erc1155Params;
    }

    public BigDecimal getRedeemDelegationValue() {
        return redeemDelegationValue;
    }

    public void setRedeemDelegationValue(BigDecimal redeemDelegationValue) {
        this.redeemDelegationValue = redeemDelegationValue;
    }

    public List<TexasHoldemParam> getTexasHoldemParam() {
        return texasHoldemParam;
    }

    public void setTexasHoldemParam(List<TexasHoldemParam> texasHoldemParam) {
        this.texasHoldemParam = texasHoldemParam;
    }

}
