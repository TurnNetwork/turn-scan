package com.turn.browser.service.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * ERC20 transaction record ES operation class
 */
@Repository
@Slf4j
public class EsErc20TxRepository extends AbstractEsRepository {
    @Override
    public String getIndexName() {
        return config.getErc20TxIndexName();
    }
    @Override
    public String getTemplateFileName(){return "erc20-tx";}
}
