package com.turn.browser.analyzer.ppos;

import com.turn.browser.AgentTestBase;
import com.turn.browser.bean.CollectionTransaction;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.dao.custommapper.RestrictingBusinessMapper;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;



/**
 * @Description: 创建锁仓计划转换器测试类
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class RestrictingCreateConverterTest extends AgentTestBase {

    @Mock
    private RestrictingBusinessMapper restrictingBusinessMapper;
    @InjectMocks
    @Spy
    private RestrictingCreateAnalyzer target;

    @Before
    public void setup()throws Exception{
    }

    @Test
    public void convert(){
        Block block = blockList.get(0);
        CollectionEvent collectionEvent = new CollectionEvent();
        collectionEvent.setBlock(block);
        Transaction tx = new Transaction();
        for(CollectionTransaction collectionTransaction : transactionList){
            if(collectionTransaction.getTypeEnum().equals(Transaction.TypeEnum.RESTRICTING_CREATE)){
                tx = collectionTransaction;
            }
        }
        target.analyze(collectionEvent,tx);
    }
}