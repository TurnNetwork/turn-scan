package com.turn.browser.service.elasticsearch;

import com.turn.browser.dao.entity.MicroNodeOptBak;
import com.turn.browser.dao.entity.NOptBak;
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
 * ES micro-node operation record service
 */
@Slf4j
@Service
public class EsMicroNodeOptService implements EsService<MicroNodeOptBak> {

    @Resource
    private EsMicroNodeOptRepository esMicroNodeOptRepository;

    @Override
    @Retryable(value = BusinessException.class, maxAttempts = Integer.MAX_VALUE)
    public void save(Set<MicroNodeOptBak> microNodeOptBaks) throws IOException {
        if (microNodeOptBaks.isEmpty()) {
            return;
        }
        try {
            Map<String, MicroNodeOptBak> microNodeOptBakMap = new HashMap<>(microNodeOptBaks.size());
            // Use (<id>) as the docId of ES
            microNodeOptBaks.forEach(n -> microNodeOptBakMap.put(n.getId().toString(), n));
            esMicroNodeOptRepository.bulkAddOrUpdate(microNodeOptBakMap);
        } catch (Exception e) {
            log.error("", e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Retryable(value = BusinessException.class, maxAttempts = Integer.MAX_VALUE)
    public void add(MicroNodeOptBak microNodeOptBaks) throws IOException {
        if (Objects.isNull(microNodeOptBaks)) {
            return;
        }
        try {
            esMicroNodeOptRepository.add(microNodeOptBaks.getId().toString(),microNodeOptBaks);
        } catch (Exception e) {
            log.error("", e);
            throw new BusinessException(e.getMessage());
        }
    }


}
