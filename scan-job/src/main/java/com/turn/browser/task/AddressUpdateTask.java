package com.turn.browser.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.turn.browser.bean.AddressQty;
import com.turn.browser.dao.custommapper.CustomAddressMapper;
import com.turn.browser.dao.custommapper.StatisticBusinessMapper;
import com.turn.browser.dao.entity.*;
import com.turn.browser.dao.mapper.AddressMapper;
import com.turn.browser.dao.mapper.PointLogMapper;
import com.turn.browser.dao.mapper.TxBakMapper;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.task.bean.AddressStatistics;
import com.turn.browser.utils.AddressUtil;
import com.turn.browser.utils.AppStatusUtil;
import com.turn.browser.utils.TaskUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


/**
 * Address list supplement
 * <pre>
 * Requirements: Supplementary fields
 * staking_value The amount of pledge
 * delegate_value the amount delegated
 * redeemed_value the amount being redeemed
 * candidate_count Delegated validators
 * delegate_hes unlocks the delegate
 * delegate_locked has locked the delegate
 * delegate_released in redemption
 *
 * Precautions
 * The number of address tables is relatively large
 * <pre/>
 */
@Component
@Slf4j
public class AddressUpdateTask {

    @Resource
    private StatisticBusinessMapper statisticBusinessMapper;

    @Resource
    private AddressMapper addressMapper;

    @Resource
    private PointLogMapper pointLogMapper;

    @Resource
    private CustomAddressMapper customAddressMapper;

    @Resource
    private TxBakMapper txBakMapper;

    /**
     * Used for address table updates
     */
    private AtomicLong addressStart = new AtomicLong(0L);

    /**
     *Add address table information supplement
     * Executed every 5 seconds
     *
     * @param :
     * @return: void
     */
    @XxlJob("addressUpdateJobHandler")
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void addressUpdate() {
        //Execute tasks only when the program is running normally
        if (!AppStatusUtil.isRunning()) {
            return;
        }
        try {
            int batchSize = Convert.toInt(XxlJobHelper.getJobParam(), 1000);
            batchUpdate(addressStart.intValue(), batchSize);
            XxlJobHelper.handleSuccess(StrUtil.format("Address table information was added successfully, the current identification is [{}]", addressStart.get()));
        } catch (Exception e) {
            log.error("Address table information supplement exception", e);
            throw e;
        }
    }

    /**
     * Perform tasks
     *
     * @param start the starting block height
     * @param size executed batch
     * @return
     */
    protected void batchUpdate(int start, int size) {
        //Query the address to be added
        AddressExample addressExample = new AddressExample();
        addressExample.setOrderByClause("create_time limit " + start + "," + size);
        List<Address> addressList = addressMapper.selectByExample(addressExample);
        if (CollUtil.isEmpty(addressList)) {
            addressStart.set(0L);
            return;
        } else {
            addressStart.set(addressStart.get() + addressList.size());
        }
        List<String> addressStringList = addressList.stream().map(Address::getAddress).collect(Collectors.toList());
        //Query the pledge initiated by this address (valid pledge and redeemed pledge)
        List<AddressStatistics> stakingList = statisticBusinessMapper.getAddressStatisticsFromStaking(addressStringList);
        //Query the delegation initiated by this address
        List<AddressStatistics> delegationList = statisticBusinessMapper.getAddressStatisticsFromDelegation(addressStringList);
        //summary results
        Map<String, AddressStatistics> stakingMap = stakingList.stream().collect(Collectors.toMap(AddressStatistics::getStakingAddr, v -> v, (v1, v2) -> {
            v1.setStakingHes(v1.getStakingHes().add(v2.getStakingHes()));
            v1.setStakingLocked(v1.getStakingLocked().add(v2.getStakingLocked()));
            v1.setStakingReduction(v1.getStakingReduction().add(v2.getStakingReduction()));
            return v1;
        }));
        Map<String, AddressStatistics> delegationMap = delegationList.stream().collect(Collectors.toMap(AddressStatistics::getDelegateAddr, v -> v, (v1, v2) -> {
            v1.setDelegateHes(v1.getDelegateHes().add(v2.getDelegateHes()));
            v1.setDelegateLocked(v1.getDelegateLocked().add(v2.getDelegateLocked()));
            v1.setDelegateReleased(v1.getDelegateReleased().add(v2.getDelegateReleased()));
            v1.getNodeIdSet().add(v2.getNodeId());
            return v1;
        }));
        List<Address> updateAddressList = new ArrayList<>();
        addressList.forEach(item -> {
            AddressStatistics staking = stakingMap.get(item.getAddress());
            AddressStatistics delegation = delegationMap.get(item.getAddress());
            boolean hasChange = false;
            BigDecimal stakingValue = staking == null ? BigDecimal.ZERO : staking.getStakingHes().add(staking.getStakingLocked());
            if (stakingValue.compareTo(item.getStakingValue()) != 0) {
                item.setStakingValue(stakingValue);
                hasChange = true;
            }

            BigDecimal stakingReduction = staking == null ? BigDecimal.ZERO : staking.getStakingReduction();
            if (stakingReduction.compareTo(item.getRedeemedValue()) != 0) {
                item.setRedeemedValue(stakingReduction);
                hasChange = true;
            }

            BigDecimal delegateHes = delegation == null ? BigDecimal.ZERO : delegation.getDelegateHes();
            if (delegateHes.compareTo(item.getDelegateHes()) != 0) {
                item.setDelegateHes(delegateHes);
                hasChange = true;
            }

            BigDecimal delegateLocked = delegation == null ? BigDecimal.ZERO : delegation.getDelegateLocked();
            if (delegateLocked.compareTo(item.getDelegateLocked()) != 0) {
                item.setDelegateLocked(delegateLocked);
                hasChange = true;
            }

            BigDecimal delegateValue = delegation == null ? BigDecimal.ZERO : delegation.getDelegateLocked().add(delegation.getDelegateHes());
            if (delegateValue.compareTo(item.getDelegateValue()) != 0) {
                item.setDelegateValue(delegateValue);
                hasChange = true;
            }

            BigDecimal delegateReleased = delegation == null ? BigDecimal.ZERO : delegation.getDelegateReleased();
            if (delegateReleased.compareTo(item.getDelegateReleased()) != 0) {
                item.setDelegateReleased(delegateReleased);
                hasChange = true;
            }

            if (delegation != null) {
                delegation.getNodeIdSet().add(delegation.getNodeId());
            }
            int candidateCount = delegation == null ? 0 : delegation.getNodeIdSet().size();
            if (candidateCount != item.getCandidateCount()) {
                item.setCandidateCount(candidateCount);
                hasChange = true;
            }

            if (hasChange) {
                updateAddressList.add(item);
            }
        });

        if (!updateAddressList.isEmpty()) {
            statisticBusinessMapper.batchUpdateFromTask(updateAddressList);
        }
    }

    /**
     * Update the number of address transactions
     * Executed every 30 seconds
     *
     * @param :
     * @return: void
     */
    @XxlJob("updateAddressQtyJobHandler")
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void updateQty() throws Exception {
        try {
            int pageSize = Convert.toInt(XxlJobHelper.getJobParam(), 500);
            PointLog pointLog = pointLogMapper.selectByPrimaryKey(2);
            long oldPosition = Convert.toLong(pointLog.getPosition());
            TaskUtil.console("当前页数为[{}]，断点为[{}]", pageSize, oldPosition);
            List<TxBak> transactionList = getTransactionList(oldPosition, pageSize);
            if (CollUtil.isNotEmpty(transactionList)) {
                String minId = CollUtil.getFirst(transactionList).getId().toString();
                String maxId = CollUtil.getLast(transactionList).getId().toString();
                pointLog.setPosition(maxId);
                TaskUtil.console("Find [{}] transactions, transaction ID is [{}-{}]", transactionList.size(), minId, maxId);
                Map<String, AddressQty> map = checkAddress(transactionList);
                TaskUtil.console("The data before update is {}", JSONUtil.toJsonStr(map.values()));
                for (TxBak txBak : transactionList) {
                    if (!AddressUtil.isAddrZero(txBak.getFrom())) {
                        AddressQty from = map.get(txBak.getFrom());
                        addQty(Transaction.TypeEnum.getEnum(txBak.getType()), from);
                    } else {
                        TaskUtil.console("Transaction [{}]from[{}] is zero address", txBak.getHash(), txBak.getFrom());
                    }
                    if (!AddressUtil.isAddrZero(txBak.getTo())) {
                        AddressQty to = map.get(txBak.getTo());
                        addQty(Transaction.TypeEnum.getEnum(txBak.getType()), to);
                    } else {
                        TaskUtil.console("Transaction [{}]to[{}] is zero address", txBak.getHash(), txBak.getTo());
                    }
                }
                List<AddressQty> list = CollUtil.newArrayList(map.values());
                customAddressMapper.batchUpdateAddressQty(list);
                pointLogMapper.updateByPrimaryKeySelective(pointLog);
                TaskUtil.console("The updated data is {}", JSONUtil.toJsonStr(map.values()));
                TaskUtil.console("Update the number of address transactions, the breakpoint (transaction id) is [{}]->[{}], update [{}] addresses", oldPosition, pointLog.getPosition(), list.size() );
            } else {
                XxlJobHelper.handleSuccess(StrUtil.format("The latest breakpoint [{}] did not find the transaction list, updating the address transaction number is completed", oldPosition));
            }
        } catch (Exception e) {
            log.error("Abnormal number of address update transactions", e);
            throw e;
        }
    }

    /**
     * Address increases the number of transactions
     *
     * @param typeEnum:
     * @param addressQty:
     * @return: void
     */
    private void addQty(Transaction.TypeEnum typeEnum, AddressQty addressQty) {
        addressQty.setTxQty(addressQty.getTxQty() + 1);
        switch (typeEnum) {
            case TRANSFER:
                addressQty.setTransferQty(addressQty.getTransferQty() + 1);
                break;
            case EVM_CONTRACT_CREATE:
                break;
            case CONTRACT_EXEC:
                break;
            case WASM_CONTRACT_CREATE:
                break;
            case OTHERS:
                break;
            case MPC:
                break;
            case ERC20_CONTRACT_CREATE:
                break;
            case ERC20_CONTRACT_EXEC:
                break;
            case ERC721_CONTRACT_CREATE:
                break;
            case ERC721_CONTRACT_EXEC:
                break;
            case ERC1155_CONTRACT_CREATE:
                break;
            case ERC1155_CONTRACT_EXEC:
                break;
            case CONTRACT_EXEC_DESTROY:
                break;
            case STAKE_CREATE:
                addressQty.setStakingQty(addressQty.getStakingQty() + 1);
                break;
            case STAKE_MODIFY:
                addressQty.setStakingQty(addressQty.getStakingQty() + 1);
                break;
            case STAKE_INCREASE:
                addressQty.setStakingQty(addressQty.getStakingQty() + 1);
                break;
            case STAKE_EXIT:
                addressQty.setStakingQty(addressQty.getStakingQty() + 1);
                break;
            case DELEGATE_CREATE:
                addressQty.setDelegateQty(addressQty.getDelegateQty() + 1);
                break;
            case DELEGATE_EXIT:
                addressQty.setDelegateQty(addressQty.getDelegateQty() + 1);
                break;
            case PROPOSAL_TEXT:
                addressQty.setProposalQty(addressQty.getProposalQty() + 1);
                break;
            case PROPOSAL_UPGRADE:
                addressQty.setProposalQty(addressQty.getProposalQty() + 1);
                break;
            case PROPOSAL_PARAMETER:
                addressQty.setProposalQty(addressQty.getProposalQty() + 1);
                break;
            case PROPOSAL_VOTE:
                addressQty.setProposalQty(addressQty.getProposalQty() + 1);
                break;
            case VERSION_DECLARE:
                addressQty.setProposalQty(addressQty.getProposalQty() + 1);
                break;
            case PROPOSAL_CANCEL:
                addressQty.setProposalQty(addressQty.getProposalQty() + 1);
                break;
            case REPORT:
                addressQty.setStakingQty(addressQty.getStakingQty() + 1);
                break;
            case RESTRICTING_CREATE:
                break;
            case CLAIM_REWARDS:
                addressQty.setDelegateQty(addressQty.getDelegateQty() + 1);
                break;
            default:
                break;
        }
    }

    /**
     * Verification address
     *
     * @param transactionList:
     * @return: java.util.Map<java.lang.String, com.turn.browser.bean.AddressQty>
     */
    private Map<String, AddressQty> checkAddress(List<TxBak> transactionList) throws Exception {
        Set<String> addressSet = new HashSet<>();
        Set<String> froms = transactionList.stream().map(TxBak::getFrom).filter(from -> !AddressUtil.isAddrZero(from)).collect(Collectors.toSet());
        Set<String> tos = transactionList.stream().map(TxBak::getTo).filter(to -> !AddressUtil.isAddrZero(to)).collect(Collectors.toSet());
        addressSet.addAll(froms);
        addressSet.addAll(tos);
        AddressExample example = new AddressExample();
        example.createCriteria().andAddressIn(new ArrayList<>(addressSet));
        List<Address> addressList = addressMapper.selectByExample(example);
        if (addressList.size() != addressSet.size()) {
            Set<String> address1 = addressList.stream().map(Address::getAddress).collect(Collectors.toSet());
            String msg = StrUtil.format("The address obtained from the transaction cannot be found in the database, and the missing address is {}", JSONUtil.toJsonStr(CollUtil.subtractToList(addressSet, address1)));
            XxlJobHelper.log(msg);
            log.error(msg);
            throw new Exception("Exception in updating address transaction number");
        }
        Map<String, AddressQty> map = new HashMap();
        addressList.forEach(address -> {
            AddressQty addressQty = AddressQty.builder()
                                              .address(address.getAddress())
                                              .txQty(address.getTxQty())
                                              .transferQty(address.getTransferQty())
                                              .delegateQty(address.getDelegateQty())
                                              .stakingQty(address.getStakingQty())
                                              .proposalQty(address.getProposalQty())
                                              .build();
            map.put(address.getAddress(), addressQty);
        });
        return map;
    }

    /**
     * Get transaction list
     *
     * @param maxId:
     * @param pageSize:
     * @return: java.util.List<com.turn.browser.elasticsearch.dto.Transaction>
     */
    private List<TxBak> getTransactionList(long maxId, int pageSize) throws Exception {
        try {
            TxBakExample example = new TxBakExample();
            example.createCriteria().andIdGreaterThan(maxId);
            example.setOrderByClause("id asc limit " + pageSize);
            List<TxBak> list = txBakMapper.selectByExample(example);
            return list;
        } catch (Exception e) {
            log.error("Exception in getting transaction list", e);
            throw e;
        }
    }

}