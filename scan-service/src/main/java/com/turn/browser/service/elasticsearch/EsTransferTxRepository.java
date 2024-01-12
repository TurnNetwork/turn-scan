package com.turn.browser.service.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * ES processor for processing internal transfer records of contracts
 */
@Repository
@Slf4j
public class EsTransferTxRepository extends AbstractEsRepository {
    @Override
    public String getIndexName() {
        return config.getTransferTxIndexName();
    }
    @Override
    public String getTemplateFileName() {
        return "transfer-tx";
    }
}
