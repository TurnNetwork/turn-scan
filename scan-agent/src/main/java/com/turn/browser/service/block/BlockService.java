package com.turn.browser.service.block;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.turn.browser.analyzer.epoch.OnConsensusAnalyzer;
import com.turn.browser.analyzer.epoch.OnElectionAnalyzer;
import com.turn.browser.analyzer.epoch.OnNewBlockAnalyzer;
import com.turn.browser.analyzer.epoch.OnSettleAnalyzer;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.exception.NoSuchBeanException;
import com.bubble.protocol.core.methods.response.BubbleBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class BlockService {

    @Resource
    private BlockRetryService retryService;

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private OnNewBlockAnalyzer onNewBlockAnalyzer;

    @Resource
    private OnElectionAnalyzer onElectionAnalyzer;

    @Resource
    private OnConsensusAnalyzer onConsensusAnalyzer;

    @Resource
    private OnSettleAnalyzer onSettleAnalyzer;

    /**
     * Get blocks asynchronously
     */
    public CompletableFuture<BubbleBlock> getBlockAsync(Long blockNumber) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return retryService.getBlock(blockNumber);
            } catch (Exception e) {
                log.error("Retry to collect block ({}) exception:", blockNumber, e);
            }
            return null;
        });
    }

    public void checkBlockNumber(Long blockNumber) {
        try {
            retryService.checkBlockNumber(blockNumber);
        } catch (Exception e) {
            log.error("Retry sync chain error:", e);
        }
    }


    /**
     * Analyze blocks and construct business warehousing parameter information
     *
     * @return
     */
    public List<NodeOpt> analyze(CollectionEvent event) throws NoSuchBeanException {
        long startTime = System.currentTimeMillis();

        List<NodeOpt> nodeOptList = new ArrayList<>();
        Block block = event.getBlock();

        if (block.getNum() == 0)
            return nodeOptList;

        // New election cycle events
        if ((block.getNum() + chainConfig.getElectionBackwardBlockCount().longValue()) % chainConfig.getConsensusPeriodBlockCount().longValue() == 0
                && event.getEpochMessage().getConsensusEpochRound().longValue() > 1) {
            // Only enter when the consensus round number is greater than 1
            log.info("Elect validators at block height [{}]", block.getNum());
            List<NodeOpt> nodeOpt = onElectionAnalyzer.analyze(event, block);
            nodeOptList.addAll(nodeOpt);
        }

        // New consensus cycle events
        if ((block.getNum() - 1) % chainConfig.getConsensusPeriodBlockCount().longValue() == 0) {
            log.info("Switch the consensus cycle at block height [{}], and the current consensus cycle number is [{}]", block.getNum(), event.getEpochMessage().getConsensusEpochRound());
            Optional<List<NodeOpt>> nodeOpt = onConsensusAnalyzer.analyze(event, block);
            nodeOpt.ifPresent(nodeOptList::addAll);
        }

        // New settlement cycle events
        if ((block.getNum() - 1) % chainConfig.getSettlePeriodBlockCount().longValue() == 0) {
            log.info("Switch the settlement cycle at block height [{}], the current settlement cycle number is [{}]", block.getNum(), event.getEpochMessage().getSettleEpochRound());
            List<NodeOpt> nodeOpt = onSettleAnalyzer.analyze(event, block);
            nodeOptList.addAll(nodeOpt);
        }

        // new block event
        onNewBlockAnalyzer.analyze(event, block);


        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);

        return nodeOptList;
    }

    public List<String> getBubbleInfo(BigInteger bubbleId){

        String bubbleInfo = retryService.getBubbleInfo(bubbleId);
        JSONObject info = JSONObject.parseObject(bubbleInfo);
        JSONObject data = info.getJSONObject("data");
        JSONObject basics = data.getJSONObject("Basics");
        JSONArray microNodes = basics.getJSONArray("MicroNodes");
        List<String> result = new ArrayList<>(microNodes.size());
        for (Object microNode : microNodes) {
            JSONObject node = (JSONObject)microNode;
            result.add(node.getString("StakingAddress"));
        }
        return result;
    }
}
