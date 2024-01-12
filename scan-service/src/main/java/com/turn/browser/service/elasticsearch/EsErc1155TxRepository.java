package com.turn.browser.service.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * ERC1155 transaction record ES operation class
 */
@Repository
@Slf4j
public class EsErc1155TxRepository extends AbstractEsRepository {
    @Override
    public String getIndexName() {
        return config.getErc1155TxIndexName();
    }
    @Override
    public String getTemplateFileName(){return "erc1155-tx";}
}
