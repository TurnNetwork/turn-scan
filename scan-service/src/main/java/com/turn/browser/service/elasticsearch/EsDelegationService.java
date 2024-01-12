package com.turn.browser.service.elasticsearch;

import com.turn.browser.dao.entity.Delegation;
import com.turn.browser.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Description: ES service
 */
@Slf4j
@Service
public class EsDelegationService implements EsService<Delegation> {

    @Resource
    private EsDelegationRepository ESDelegationRepository;

    @Override
    @Retryable(value = BusinessException.class, maxAttempts = Integer.MAX_VALUE)
    public void save(Set<Delegation> delegations) throws IOException {
        if (delegations.isEmpty()) {
            return;
        }
        try {
            Map<String, Delegation> delegationMap = new HashMap<>();
            // Use (<node ID>-<pledge block number>-<principal address>) as the docId of ES
            delegations.forEach(d -> delegationMap.put(d.getNodeId() + "-" + d.getStakingBlockNum() + "-" + d.getDelegateAddr(), d));
            ESDelegationRepository.bulkAddOrUpdate(delegationMap);
        } catch (Exception e) {
            log.error("", e);
            throw new BusinessException(e.getMessage());
        }
    }

}