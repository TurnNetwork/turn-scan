package com.turn.browser.service.elasticsearch;

import com.turn.browser.elasticsearch.dto.DelegationReward;
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
 * @Description: ES service: Delegated reward service
 */
@Slf4j
@Service
public class EsDelegateRewardService implements EsService<DelegationReward> {

    @Resource
    private EsDelegationRewardRepository ESDelegationRewardRepository;

    @Override
    @Retryable(value = BusinessException.class, maxAttempts = Integer.MAX_VALUE)
    public void save(Set<DelegationReward> data) throws IOException {
        if (data.isEmpty()) {
            return;
        }
        try {
            Map<String, DelegationReward> map = new HashMap<>();
            // Use (<hash>) as the docId of ES
            data.forEach(e -> map.put(e.getHash(), e));
            ESDelegationRewardRepository.bulkAddOrUpdate(map);
        } catch (Exception e) {
            log.error("", e);
            throw new BusinessException(e.getMessage());
        }
    }

}