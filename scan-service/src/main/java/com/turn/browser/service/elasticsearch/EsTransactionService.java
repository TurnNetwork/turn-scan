package com.turn.browser.service.elasticsearch;

import com.turn.browser.elasticsearch.dto.Transaction;
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
public class EsTransactionService implements EsService<Transaction> {

    @Resource
    private EsTransactionRepository ESTransactionRepository;

    @Override
    @Retryable(value = BusinessException.class, maxAttempts = Integer.MAX_VALUE)
    public void save(Set<Transaction> transactions) throws IOException {
        if (transactions.isEmpty()) {
            return;
        }
        try {
            Map<String, Transaction> transactionMap = new HashMap<>();
            // Use the transaction Hash as the docId of ES
            transactions.forEach(t -> transactionMap.put(t.getHash(), t));
            ESTransactionRepository.bulkAddOrUpdate(transactionMap);
        } catch (Exception e) {
            log.error("", e);
            throw new BusinessException(e.getMessage());
        }
    }

}