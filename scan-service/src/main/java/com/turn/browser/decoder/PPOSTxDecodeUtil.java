package com.turn.browser.decoder;

import com.bubble.protocol.core.methods.response.Log;
import com.bubble.rlp.solidity.RlpDecoder;
import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.rlp.solidity.RlpType;
import com.turn.browser.decoder.ppos.*;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.param.OthersTxParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

/**
 * Built-in contract transaction analysis tool:
 * 1. Obtain the built-in contract transaction type and parameters based on tx input
 * 2. Obtain information related to the execution results of the built-in contract based on logs
 */
@Slf4j
public class PPOSTxDecodeUtil {

    private PPOSTxDecodeUtil() {
    }

    public static PPOSTxDecodeResult decode(String txInput, List<Log> logs) {
        PPOSTxDecodeResult result = new PPOSTxDecodeResult();
        try {
            if (StringUtils.isNotEmpty(txInput) && !txInput.equals("0x")) {
                RlpList rlpList = RlpDecoder.decode(Hex.decode(txInput.replace("0x", "")));
                List<RlpType> rlpTypes = rlpList.getValues();

                if (rlpTypes.size() == 1 && rlpTypes.get(0).getClass().equals(RlpString.class)) {
                    OthersTxParam txParam = new OthersTxParam();
                    txParam.setData(((RlpString) rlpTypes.get(0)).asString());
                    result.setParam(txParam);
                    return result;
                }

                RlpList rootList = (RlpList) rlpTypes.get(0);

                RlpString rlpString = (RlpString) rootList.getValues().get(0);
                RlpList rlpList2 = RlpDecoder.decode(rlpString.getBytes());
                RlpString rl = (RlpString) rlpList2.getValues().get(0);
                BigInteger txCode = new BigInteger(1, rl.getBytes());

                Transaction.TypeEnum typeEnum = Transaction.TypeEnum.getEnum(txCode.intValue());
                if (typeEnum == null) {
                    OthersTxParam txParam = new OthersTxParam();
                    result.setParam(txParam);
                    return result;
                }
                result.setTypeEnum(typeEnum);
                switch (typeEnum) {
                    case STAKE_CREATE: // 1000 initiates pledge
                        return result.setParam(StakeCreateDecoder.decode(rootList));
                    case STAKE_MODIFY: // 1001 Modify pledge
                        return result.setParam(StakeModifyDecoder.decode(rootList));
                    case STAKE_INCREASE: // 1002 Increase pledge
                        return result.setParam(StakeIncreaseDecoder.decode(rootList));
                    case STAKE_EXIT: // 1003 cancel pledge
                        return result.setParam(StakeExitDecoder.decode(rootList));
                    case DELEGATE_CREATE: // 1004 initiate delegation
                        return result.setParam(DelegateCreateDecoder.decode(rootList));
                    case DELEGATE_EXIT: // 1005 reduction/cancellation of commission
                        return result.setParam(DelegateExitDecoder.decode(rootList, logs));
                    case REDEEM_DELEGATION: // 1006 Receive the unlocking commission
                        return result.setParam(RedeemDelegationDecoder.decode(rootList, logs));
                    case PROPOSAL_TEXT: // 2000 Submit text proposal
                        return result.setParam(ProposalTextDecoder.decode(rootList));
                    case PROPOSAL_UPGRADE: // 2001 Submit upgrade proposal
                        return result.setParam(ProposalUpgradeDecoder.decode(rootList));
                    case PROPOSAL_PARAMETER: // 2002 Submit upgrade proposal
                        return result.setParam(ProposalParameterDecoder.decode(rootList));
                    case PROPOSAL_CANCEL: // 2005 Submit cancellation proposal
                        return result.setParam(ProposalCancelDecoder.decode(rootList));
                    case PROPOSAL_VOTE: // 2003 vote for the proposal
                        return result.setParam(ProposalVoteDecoder.decode(rootList));
                    case VERSION_DECLARE: // 2004 version declaration
                        return result.setParam(VersionDeclareDecoder.decode(rootList));
                    case REPORT: // 3000 report double signature
                        return result.setParam(ReportDecoder.decode(rootList));
                    case RESTRICTING_CREATE: // 4000 Create hedging plan
                        return result.setParam(RestrictingCreateDecoder.decode(rootList));
                    case CLAIM_REWARDS: // 5000 to receive commission rewards
                        //Do not parse if the log is empty
                        if (logs.isEmpty()) {
                            return result;
                        }
                        return result.setParam(DelegateRewardClaimDecoder.decode(rootList, logs));
                    case CREATE_STAKING: // 7000
                        return result.setParam(CreateStakingDecoder.decode(rootList));
                    case EDIT_CANDIDATE: // 7001
                        return result.setParam(EditCandidateDecoder.decode(rootList));
                    case WITHDREW_STAKING: // 7003
                        return result.setParam(WithdrewStakingDecoder.decode(rootList));
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            log.error("Error in parsing the built-in contract transaction input:", e);
        }
        return result;
    }

}
