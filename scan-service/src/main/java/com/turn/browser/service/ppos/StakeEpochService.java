package com.turn.browser.service.ppos;

import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.entity.Proposal;
import com.turn.browser.dao.custommapper.CustomProposalMapper;
import com.turn.browser.enums.ModifiableGovernParamEnum;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.service.govern.ParameterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.List;

/**
 * Miscellaneous periodic services related to pledge
 * 1. Real de-pledge exit block number
 */
@Service
public class StakeEpochService {
    @Resource
    private ParameterService parameterService;
    @Resource
    private BlockChainConfig chainConfig;
    @Resource
    private CustomProposalMapper customProposalMapper;

    /**
     * Get the actual exit block number for unstaking
     * @param nodeId
     * @param curSettleEpoch
     * @param compareProposalVotingEndBlock Whether it is necessary to compare the associated proposals
     * @return
     */
    public BigInteger getUnStakeEndBlock(String nodeId,BigInteger curSettleEpoch,Boolean compareProposalVotingEndBlock){
        // Theoretical exit block number, the actual exit block number should be compared with the voting deadline block of the proposal with status in progress, whichever is the largest
        BigInteger unStakeEndBlock = curSettleEpoch // The settlement cycle number of the current block
                .add(getUnStakeFreeDuration()) //+ The number of settlement cycles required to unstake
                .multiply(chainConfig.getSettlePeriodBlockCount()); // x number of blocks per settlement period
        if(compareProposalVotingEndBlock){
            // If you need to compare with the proposals in the current node's vote, execute the following logic
        	List<Proposal> proposalList = customProposalMapper.selectVotingProposal(nodeId);
            for (Proposal proposal : proposalList) {
                BigInteger endVotingBlock = BigInteger.valueOf(proposal.getEndVotingBlock());
                if(endVotingBlock.compareTo(unStakeEndBlock)>0){
                    // If the cut-off block number in the proposal is greater than the theoretical exit block number of the current pledge, use the cut-off block number of the proposal
                    unStakeEndBlock = endVotingBlock;
                }
            }
        }
        return unStakeEndBlock;
    }

    /**
     * Get the theoretical number of settlement cycles required to unpledge
     * @return
     */
    public BigInteger getUnStakeFreeDuration(){
        //Update the number of settlement cycles required to release the pledge to the account
        String configVal = parameterService.getValueInBlockChainConfig(ModifiableGovernParamEnum.UN_STAKE_FREEZE_DURATION.getName());
        if(StringUtils.isBlank(configVal)){
            throw new BusinessException("Parameter table parameter missing:"+ModifiableGovernParamEnum.UN_STAKE_FREEZE_DURATION.getName());
        }
        return new BigInteger(configVal);
    }

    /**
     * Get the number of locked settlement periods
     * @return
     */
    public BigInteger getZeroProduceFreeDuration(){
        String configVal = parameterService.getValueInBlockChainConfig(ModifiableGovernParamEnum.ZERO_PRODUCE_FREEZE_DURATION.getName());
        if(StringUtils.isBlank(configVal)){
            throw new BusinessException("Parameter table parameter missing:"+ModifiableGovernParamEnum.ZERO_PRODUCE_FREEZE_DURATION.getName());
        }
        return new BigInteger(configVal);
    }
}
