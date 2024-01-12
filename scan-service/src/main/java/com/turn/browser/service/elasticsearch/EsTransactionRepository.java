package com.turn.browser.service.elasticsearch;

import org.springframework.stereotype.Repository;

/**
 * @Description: Trading operations
 */
@Repository
public class EsTransactionRepository extends AbstractEsRepository {
    @Override
    public String getIndexName() {
        return config.getTransactionIndexName();
    }
    @Override
    public String getTemplateFileName() {
        return "transaction";
    }
}
