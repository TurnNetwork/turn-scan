package com.turn.browser.task;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.turn.browser.bean.RestrictingBalance;
import com.turn.browser.client.JobTurnClient;
import com.turn.browser.client.SpecialApi;
import com.turn.browser.config.TaskConfig;
import com.turn.browser.dao.entity.InternalAddress;
import com.turn.browser.dao.entity.InternalAddressExample;
import com.turn.browser.dao.mapper.InternalAddressMapper;
import com.turn.browser.enums.InternalAddressType;
import com.bubble.protocol.Web3j;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Component
@Slf4j
public class BalanceUpdateTask {

    @Resource
    private InternalAddressMapper internalAddressMapper;

    @Resource
    private SpecialApi specialApi;

    @Resource(name = "jobTurnClient")
    private JobTurnClient turnClient;

    @Resource
    private TaskConfig config;

    /**
     * Update foundation account balance
     * Executed every 6 minutes
     */
    @XxlJob("balanceUpdateJobHandler")
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void updateFundAccount() {
        try {
            updateBalance(InternalAddressType.FUND_ACCOUNT);
            XxlJobHelper.handleSuccess("Updating foundation account balance completed");
        } catch (Exception e) {
            log.error("Abnormal update of foundation account balance", e);
            throw e;
        }
    }

    /**
     * Update built-in contract account balance
     * Executed every 10 seconds
     */
    @XxlJob("updateContractAccountJobHandler")
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void updateContractAccount() {
        try {
            updateBalance(InternalAddressType.OTHER);
            XxlJobHelper.handleSuccess("Update of built-in contract account balance completed");
        } catch (Exception e) {
            log.error("Exception in updating built-in contract account balance", e);
            throw e;
        }
    }

    private void updateBalance(InternalAddressType type) {
        InternalAddressExample example = new InternalAddressExample();
        switch (type) {
            case FUND_ACCOUNT:
                example.createCriteria().andTypeEqualTo(type.getCode());
                break;
            case OTHER:
                example.createCriteria().andTypeNotEqualTo(InternalAddressType.FUND_ACCOUNT.getCode());
                break;
        }
        example.setOrderByClause(" address LIMIT " + config.getMaxAddressCount());
        Instant start = Instant.now();
        List<InternalAddress> addressList = internalAddressMapper.selectByExample(example);
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        log.debug("Querying the address takes: {} ms", duration.toMillis());
        if (!addressList.isEmpty()) {
            Instant start1 = Instant.now();
            updateBalance(addressList);
            Instant end1 = Instant.now();
            Duration duration1 = Duration.between(start1, end1);
            log.debug("Total address update time: {} ms", duration1.toMillis());
        } else {
            log.info("The number of addresses is 0, no operation!");
        }
    }

    private void updateBalance(List<InternalAddress> addressList) {
        List<Map<String, InternalAddress>> batchList = new ArrayList<>();
        Map<String, InternalAddress> batch = new HashMap<>();
        batchList.add(batch);
        for (InternalAddress address : addressList) {
            if (batch.size() >= config.getMaxBatchSize()) {
                // If the current batch size reaches the batch size, create a new batch
                batch = new HashMap<>();
                batchList.add(batch);
            }
            // <address-internal address> mapping
            batch.put(address.getAddress(), address);
        }
        log.info("The total number of addresses is {}, divided into {} batches, each batch has a maximum of {} addresses", addressList.size(), batchList.size(), config.getMaxBatchSize());

        // Query and update balance by batch
        batchList.forEach(addressMap -> {
            try {
                Web3j web3j = turnClient.getWeb3jWrapper().getWeb3j();
                Set<String> addressSet = addressMap.keySet();
                String addresses = String.join(";", addressSet);
                log.debug("Locked balance query parameters: {}", addresses);

                Instant start = Instant.now();
                List<RestrictingBalance> balanceList = specialApi.getRestrictingBalance(web3j, addresses);
                Instant end = Instant.now();
                Duration duration = Duration.between(start, end);
                log.debug("It takes time to query the address lock balance in this batch: {} ms", duration.toMillis());

                log.debug("Locked balance query result: {}", JSON.toJSONString(balanceList));
                //Set balance
                balanceList.forEach(balance -> {
                    InternalAddress address = addressMap.get(balance.getAccount());
                    address.setBalance(new BigDecimal(balance.getFreeBalance()));
                    address.setRestrictingBalance(new BigDecimal(balance.getLockBalance().subtract(balance.getPledgeBalance())));
                });

                // Synchronous updates to prevent deadlocks caused by table lock contention
                synchronized (BalanceUpdateTask.class) {
                    // Update balance in batches
                    Instant start1 = Instant.now();
                    if (CollUtil.isNotEmpty(addressMap)) {
                        for (Map.Entry<String, InternalAddress> entry : addressMap.entrySet()) {
                            internalAddressMapper.updateByPrimaryKey(entry.getValue());
                        }
                    }
                    Instant end1 = Instant.now();
                    Duration duration1 = Duration.between(start1, end1);
                    log.debug("The time it takes to update the address balance in this batch: {} ms", duration1.toMillis());
                    log.info("Batch update of address balance successful!");
                }
            } catch (Exception e) {
                log.error("Batch update of address balance failed!", e);
            }
        });
    }

}