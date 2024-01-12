package com.turn.browser.cache;

import cn.hutool.core.collection.CollUtil;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.dao.mapper.NetworkStatMapper;
import com.turn.browser.elasticsearch.dto.Block;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Transaction TPS calculation cache
 */
@Slf4j
@Component
public class TpsCalcCache {

    @Resource
    private NetworkStatMapper networkStatMapper;

    // <block generation seconds, number of transactions>
    private Map<Long, Integer> cacheMap = new HashMap<>();

    private Map<Long, Integer> maxCacheMap = new HashMap<>();

    private int tps = 0;

    private int maxTps = 0;

    private static Long startTime = 0l;

    private static Long maxTime = 0l;

    /**
     * The currently processed block height is used to handle the retry mechanism
     */
    private final AtomicLong curHandleBlockNum = new AtomicLong(0);

    /**
     * Process if the block is not counted
     *
     * @param block:
     * @return: void
     */
    public void updateIfNotHandle(Block block) {
        if (curHandleBlockNum.get() == 0) {
            List<NetworkStat> networkStatList = networkStatMapper.selectByExample(null);
            if (CollUtil.isNotEmpty(networkStatList)) {
                curHandleBlockNum.set(networkStatList.get(0).getCurNumber());
            }
        }
        if (block.getNum().compareTo(curHandleBlockNum.get()) > 0) {
            update(block);
            curHandleBlockNum.set(block.getNum());
        }
    }

    public void update(Block block) {
        BigDecimal seconds = BigDecimal.valueOf(block.getTime().getTime()).divide(BigDecimal.valueOf(1000), 0, RoundingMode.CEILING);
        Long now = seconds.longValue();
        Integer txQty = cacheMap.get(TpsCalcCache.startTime);
        if (txQty == null) {
            txQty = 0;
            TpsCalcCache.startTime = now;
            cacheMap.putIfAbsent(TpsCalcCache.startTime, 0);
        }
        if (now <= startTime + 10) {
            txQty += block.getTransactions().size();
            cacheMap.put(startTime, txQty);
        } else {
            tps = BigDecimal.valueOf(txQty).divide(BigDecimal.TEN, 0, RoundingMode.CEILING).intValue();
            startTime = startTime + 10;
            cacheMap.clear();
            cacheMap.put(startTime, block.getTransactions().size());
        }

        Integer maxTxQty = maxCacheMap.get(maxTime);
        if (maxTxQty == null) {
            maxTxQty = 0;
            TpsCalcCache.maxTime = now;
            maxCacheMap.putIfAbsent(maxTime, 0);
        }
        if (TpsCalcCache.maxTime.longValue() == now.longValue()) {
            maxTxQty += block.getTransactions().size();
            maxCacheMap.put(maxTime, maxTxQty);
        } else {
            maxTps = maxTxQty.intValue();
            if (maxTps == 0) {
                maxTps = block.getTransactions().size();
            }
            maxCacheMap.clear();
            maxTime = now;
            maxCacheMap.put(maxTime, block.getTransactions().size());
        }
    }

    public int getTps() {
        return tps;
    }

    public int getMaxTps() {
        return maxTps;
    }

}
