package com.turn.browser.service.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * ERC20 transaction record ES operation class
 */
@Repository
@Slf4j
public class EsErc721TxRepository extends AbstractEsRepository {
    @Override
    public String getIndexName() {
        return config.getErc721TxIndexName();
    }
    @Override
    public String getTemplateFileName() {
        return "erc721-tx";
    }
}
