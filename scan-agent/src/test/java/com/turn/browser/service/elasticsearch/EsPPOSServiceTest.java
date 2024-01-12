package com.turn.browser.service.elasticsearch;

import com.turn.browser.AgentTestBase;
import com.turn.browser.elasticsearch.dto.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EsPPOSServiceTest extends AgentTestBase {
    @Mock
    private EsTransactionRepository ESTransactionRepository;
    @Spy
    private EsTransactionService target;

    @Before
    public void setup(){
        ReflectionTestUtils.setField(target, "ESTransactionRepository", ESTransactionRepository);
    }

    /**
     * 根据区块号获取激励池余额
     */
    @Test(expected = Exception.class)
    public void save() throws IOException {
        target.save(Collections.emptySet());
        Set<Transaction> data = new HashSet<>();
        data.add(new Transaction());
        target.save(data);
        doThrow(new RuntimeException("")).when(ESTransactionRepository).bulkAddOrUpdate(anyMap());
        target.save(data);
    }
}
