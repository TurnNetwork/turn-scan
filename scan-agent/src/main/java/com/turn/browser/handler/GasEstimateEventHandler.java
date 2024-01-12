package com.turn.browser.handler;

import cn.hutool.json.JSONUtil;
import com.lmax.disruptor.EventHandler;
import com.turn.browser.bean.CommonConstant;
import com.turn.browser.bean.GasEstimateEvent;
import com.turn.browser.dao.custommapper.EpochBusinessMapper;
import com.turn.browser.dao.entity.GasEstimate;
import com.turn.browser.dao.mapper.GasEstimateLogMapper;
import com.turn.browser.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Block event handler
 */
@Slf4j
@Component
public class GasEstimateEventHandler implements EventHandler<GasEstimateEvent> {

    @Resource
    private GasEstimateLogMapper gasEstimateLogMapper;

    @Resource
    private EpochBusinessMapper epochBusinessMapper;

    private Long prevSeq = 0L;

    /**
     * number of retries
     */
    private AtomicLong retryCount = new AtomicLong(0);

    @Override
    @Retryable(value = Exception.class, maxAttempts = CommonConstant.reTryNum)
    public void onEvent(GasEstimateEvent event, long sequence, boolean endOfBatch) {
        surroundExec(event, sequence, endOfBatch);
    }

    /**
     * If the retry is completed or unsuccessful, this method will be called back.
     *
     * @param e:
     * @return: void
     */
    @Recover
    public void recover(Exception e) {
        retryCount.set(0);
        log.error("If the retry is completed or the service fails, please contact the administrator for processing.");
    }

    private void surroundExec(GasEstimateEvent event, long sequence, boolean endOfBatch) {
        CommonUtil.putTraceId(event.getTraceId());
        long startTime = System.currentTimeMillis();
        exec(event, sequence, endOfBatch);
        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
        CommonUtil.removeTraceId();
    }

    private void exec(GasEstimateEvent event, long sequence, boolean endOfBatch) {
        try {
            if (retryCount.incrementAndGet() > 1) {
                log.error("Number of retries [{}]. Repeated processing of seq[{}] may cause repeated data statistics. The event object data is [{}]", retryCount.get(), event.getSeq(), JSONUtil.toJsonStr(event));
            }
            if (prevSeq.equals(event.getSeq())) {
                // If the current sequence number is equal to the previous sequence number, it proves that the message has been processed
                retryCount.set(0);
                return;
            }
            List<GasEstimate> estimateList = event.getEstimateList();
            if (estimateList != null && !estimateList.isEmpty()) {
                epochBusinessMapper.updateGasEstimate(estimateList);
            }
            // Delete the log records in mysql after the es database is completed
            gasEstimateLogMapper.deleteByPrimaryKey(event.getSeq());
            prevSeq = event.getSeq();
            retryCount.set(0);
        } catch (Exception e) {
            log.error("", e);
        }
    }

}