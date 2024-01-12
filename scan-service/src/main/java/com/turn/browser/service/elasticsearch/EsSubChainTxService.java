package com.turn.browser.service.elasticsearch;

import com.turn.browser.bean.SubChainTx;
import com.turn.browser.dao.entity.MicroNodeOptBak;
import com.turn.browser.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * ES服务
 */
@Slf4j
@Service
public class EsSubChainTxService implements EsService<SubChainTx> {

    @Resource
    private EsSubChainTxRepository esSubChainTxRepository;

    @Override
    @Retryable(value = BusinessException.class, maxAttempts = Integer.MAX_VALUE)
    public void save(Set<SubChainTx> subChainTxs) throws IOException {
        if (subChainTxs.isEmpty()) {
            return;
        }
        try {
            Map<String, SubChainTx> microNodeOptBakMap = new HashMap<>(subChainTxs.size());
            // 使用(<id>)作ES的docId
            subChainTxs.forEach(n -> microNodeOptBakMap.put(n.getHash(), n));
            esSubChainTxRepository.bulkAddOrUpdate(microNodeOptBakMap);
        } catch (Exception e) {
            log.error("", e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Retryable(value = BusinessException.class, maxAttempts = Integer.MAX_VALUE)
    public void add(SubChainTx subChainTx) throws IOException {
        if (Objects.isNull(subChainTx)) {
            return;
        }
        try {
            esSubChainTxRepository.add(subChainTx.getHash(),subChainTx);
        } catch (Exception e) {
            log.error("", e);
            throw new BusinessException(e.getMessage());
        }
    }


}
