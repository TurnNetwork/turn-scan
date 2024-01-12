package com.turn.browser.analyzer.ppos;

import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.dao.custommapper.RestrictingBusinessMapper;
import com.turn.browser.dao.param.ppos.RestrictingCreate;
import com.turn.browser.dao.param.ppos.RestrictingItem;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.param.RestrictingCreateParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: Delegate business parameter converter
 **/
@Slf4j
@Service
public class RestrictingCreateAnalyzer extends PPOSAnalyzer<RestrictingCreate> {

    @Resource
    private RestrictingBusinessMapper restrictingBusinessMapper;

    /**
     * Create a hedging plan (Create a hedging plan)
     *
     * @param event
     * @param tx
     * @return com.turn.browser.dao.param.ppos.RestrictingCreate
     */
    @Override
    public RestrictingCreate analyze(CollectionEvent event, Transaction tx) {
        // Failed transactions do not analyze business data
        if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus())
            return null;

        long startTime = System.currentTimeMillis();

        RestrictingCreateParam txParam = tx.getTxParam(RestrictingCreateParam.class);
        String account = txParam.getAccount();

        List<RestrictingItem> restrictingItems = txParam.getPlans().stream().map(plan -> RestrictingItem.builder()
                .address(account)
                .amount(plan.getAmount())
                .epoch(plan.getEpoch())
                .number(BigInteger.valueOf(tx.getNum()))
                .build()).collect(Collectors.toList());

        RestrictingCreate businessParam = RestrictingCreate.builder()
                .itemList(restrictingItems)
                .build();

        // Locking records into storage
        restrictingBusinessMapper.create(businessParam);

        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);

        return businessParam;
    }

}
