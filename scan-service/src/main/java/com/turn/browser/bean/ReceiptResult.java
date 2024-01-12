package com.turn.browser.bean;

import com.bubble.protocol.core.Response;
import com.turn.browser.utils.HexUtil;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

@Data
public class ReceiptResult extends Response<List<Receipt>> {
    private Map<String,Receipt> map = new ConcurrentHashMap<>();


    public void resolve(Long blockNumber, ExecutorService threadPool) throws InterruptedException {
        if(getResult().isEmpty()) return;
        CountDownLatch latch = new CountDownLatch(getResult().size());
        getResult().forEach(receipt->{
            map.put(HexUtil.prefix(receipt.getTransactionHash()),receipt);
            threadPool.submit(()->{
                try {
                    receipt.setBlockNumber(blockNumber);
                    receipt.decodeLogs();
                }finally {
                    latch.countDown();
                }
            });
        });
        latch.await();
    }


}