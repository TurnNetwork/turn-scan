package com.turn.browser.v0150.service;

import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.CustomAddressMapper;
import com.turn.browser.dao.entity.*;
import com.turn.browser.dao.mapper.DelegationMapper;
import com.turn.browser.dao.mapper.NodeMapper;
import com.turn.browser.dao.mapper.StakingMapper;
import com.turn.browser.dao.param.ppos.DelegateExit;
import com.turn.browser.exception.BlockNumberException;
import com.turn.browser.v0150.V0150Config;
import com.turn.browser.v0150.bean.AdjustParam;
import com.turn.browser.v0150.bean.ValidatedContext;
import com.turn.browser.v0150.context.AbstractAdjustContext;
import com.turn.browser.v0150.context.DelegateAdjustContext;
import com.turn.browser.v0150.context.StakingAdjustContext;
import com.turn.browser.v0150.dao.StakingDelegateBalanceAdjustmentMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Service
public class StakingDelegateBalanceAdjustmentService {

    @Resource
    protected DelegationMapper delegationMapper;

    @Resource
    protected StakingMapper stakingMapper;

    @Resource
    protected NodeMapper nodeMapper;

    @Resource
    protected StakingDelegateBalanceAdjustmentMapper stakingDelegateBalanceAdjustmentMapper;

    @Resource
    protected BlockChainConfig chainConfig;

    @Resource
    protected V0150Config v0150Config;

    @Resource
    private CustomAddressMapper customAddressMapper;

    private static final Logger log = Logger.getLogger(StakingDelegateBalanceAdjustmentService.class.getName());

    @PostConstruct
    private void init() {
        File logFile = new File(v0150Config.getAdjustLogFilePath());
        if (logFile.exists()) {
            boolean deleted = logFile.delete();
            if (!deleted) log.warning("Failed to delete log file！");
        }
        try {
            log.setLevel(Level.INFO);
            FileHandler fileHandler = new FileHandler(v0150Config.getAdjustLogFilePath());
            fileHandler.setLevel(Level.INFO);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            log.addHandler(fileHandler);
        } catch (SecurityException | IOException e) {
            log.warning(e.getMessage());
        }
    }

    /**
     * Verify the adjustment parameters and return the verification results
     *
     * @param adjustParams
     * @return
     */
    public void adjust(List<AdjustParam> adjustParams) throws BlockNumberException {
        ValidatedContext validatedContext = new ValidatedContext();
        if (adjustParams.isEmpty()) return;
        // Obtain complete [Adjustment context data] for each entrusted adjustment data.
        for (AdjustParam adjustParam : adjustParams) {
            AbstractAdjustContext context = null;
            if ("staking".equals(adjustParam.getOptType())) {
                // Assembling the complete contextual data required for mortgage adjustment
                StakingAdjustContext sac = new StakingAdjustContext();
                validatedContext.getStakingAdjustContextList().add(sac);
                context = sac;
            }

            if ("delegate".equals(adjustParam.getOptType())) {
                // Find the corresponding delegation information based on <delegator address, pledge block height, node ID>
                DelegationKey delegationKey = new DelegationKey();
                delegationKey.setDelegateAddr(adjustParam.getAddr());
                delegationKey.setStakingBlockNum(Long.valueOf(adjustParam.getStakingBlockNum()));
                delegationKey.setNodeId(adjustParam.getNodeId());
                Delegation delegation = delegationMapper.selectByPrimaryKey(delegationKey);
                // Assembling the complete contextual data required for commissioned accounting
                DelegateAdjustContext dac = new DelegateAdjustContext();
                dac.setDelegation(delegation);
                validatedContext.getDelegateAdjustContextList().add(dac);
                context = dac;
            }

            if (context != null) {
                context.setChainConfig(chainConfig);
                context.setAdjustParam(adjustParam);
                // Find the corresponding pledge information based on <pledge block height, node ID>
                StakingKey stakingKey = new StakingKey();
                stakingKey.setNodeId(adjustParam.getNodeId());
                stakingKey.setStakingBlockNum(Long.valueOf(adjustParam.getStakingBlockNum()));
                Staking staking = stakingMapper.selectByPrimaryKey(stakingKey);
                context.setStaking(staking);
                // Find the corresponding node information based on <node ID>
                Node node = nodeMapper.selectByPrimaryKey(adjustParam.getNodeId());
                context.setNode(node);
                // Validation context
                context.validate();

                String adjustMsg;
                if ("delegate".equals(adjustParam.getOptType())) {
                    // Entrusted to adjust accounts
                    if (StringUtils.isBlank(context.errorInfo())) {
                        AdjustParam param = context.getAdjustParam();
                        // If there is no error message in the reconciliation context, the reconciliation operation will be performed.
                        stakingDelegateBalanceAdjustmentMapper.adjustDelegateData(param);

                        // Update the delegator’s address in the cache to claim the reward
                        Delegation delegation = ((DelegateAdjustContext) context).getDelegation();
                        DelegateExit delegateExit = DelegateExit.builder().txFrom(delegation.getDelegateAddr()).delegateReward(param.getReward()).build();
                        customAddressMapper.updateAddressHaveReward(delegateExit.getTxFrom(), delegateExit.getDelegateReward());
                        StringBuilder sb = new StringBuilder("============ ").append(context.getAdjustParam().getOptType()).append("Account adjustment successful ============\n").append(context.contextInfo());
                        adjustMsg = sb.toString();
                        log.info(adjustMsg);
                    } else {
                        // There is an error message in the reconciliation context, and the reconciliation parameters are printed to the error file.
                        adjustMsg = context.errorInfo();
                        log.warning(adjustMsg);
                    }
                }

                if ("staking".equals(adjustParam.getOptType())) {
                    if (StringUtils.isBlank(context.errorInfo())) {
                        // If there is no error message in the reconciliation context, the reconciliation operation will be performed.
                        stakingDelegateBalanceAdjustmentMapper.adjustStakingData(context.getAdjustParam());
                        StringBuilder sb = new StringBuilder("============ ").append(context.getAdjustParam().getOptType()).append("Account adjustment successful ============\n").append(context.contextInfo());
                        adjustMsg = sb.toString();
                        log.info(adjustMsg);
                    } else {
                        // There is an error message in the reconciliation context, and the reconciliation parameters are printed to the error file.
                        adjustMsg = context.errorInfo();
                        log.warning(adjustMsg);
                    }
                }
            }
        }
    }

}
