package com.turn.browser.analyzer;

import com.bubble.protocol.core.methods.response.BubbleBlock;
import com.turn.browser.bean.CollectionBlock;
import com.turn.browser.bean.ReceiptResult;
import com.turn.browser.exception.BeanCreateOrUpdateException;
import com.turn.browser.exception.BlankResponseException;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.exception.ContractInvokeException;
import com.turn.browser.utils.HexUtil;
import com.turn.browser.utils.NodeUtil;
import com.bubble.protocol.core.methods.response.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Block Analyzer
 */
@Slf4j
@Component
public class BlockAnalyzer {

    @Transactional(rollbackFor = Exception.class)
    public CollectionBlock analyze(BubbleBlock.Block rawBlock, ReceiptResult receipt) throws
                                                                                      ContractInvokeException,
                                                                                      BlankResponseException,
                                                                                      BeanCreateOrUpdateException {
        String nodeId;
        if (rawBlock.getNumber().longValue() == 0) {
            nodeId = "000000000000000000000000000000000";
        } else {
            nodeId = NodeUtil.getPublicKey(rawBlock);
        }
        nodeId = HexUtil.prefix(nodeId);
        CollectionBlock result = CollectionBlock.newInstance();
        result.setNum(rawBlock.getNumber().longValue())
              .setHash(rawBlock.getHash())
              .setPHash(rawBlock.getParentHash())
              .setSize(rawBlock.getSize().intValue())
              .setTime(new Date(rawBlock.getTimestamp().longValue()))
              .setExtra(rawBlock.getExtraData())
              .setMiner(rawBlock.getMiner())
              .setNodeId(nodeId)
              .setTxFee("0")
              .setGasLimit(rawBlock.getGasLimit().toString())
              .setGasUsed(rawBlock.getGasUsed().toString());
        result.setSeq(new AtomicLong(result.getNum() * 100000));
        if (rawBlock.getTransactions().isEmpty()) {
            return result;
        }

        if (receipt.getResult().isEmpty()) {
            throw new BusinessException("block [" + result.getNum() + "]have[" + rawBlock.getTransactions()
                                                                                     .size() + "] transactions,but the query cannot find the receipt!");
        }

        result.setReceiptMap(receipt.getMap());

        // Analyze transactions
        List<BubbleBlock.TransactionResult> transactionResults = rawBlock.getTransactions();

        List<Transaction> originTransactions = new ArrayList<>();
        if (receipt.getResult() != null && !receipt.getResult().isEmpty()) {
            for (BubbleBlock.TransactionResult tr : transactionResults) {
                BubbleBlock.TransactionObject to = (BubbleBlock.TransactionObject) tr.get();
                Transaction rawTransaction = to.get();
                originTransactions.add(rawTransaction);
            }
        }
        result.setOriginTransactions(originTransactions);

        return result;
    }

}
