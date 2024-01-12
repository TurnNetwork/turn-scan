package com.turn.browser.service.elasticsearch;

import org.springframework.stereotype.Repository;

/**
 * @Description: Node log operations
 */
@Repository
public class EsNodeOptRepository extends AbstractEsRepository {
    @Override
    public String getIndexName() {
        return config.getNodeOptIndexName();
    }
    @Override
    public String getTemplateFileName() {
        return "nodeopt";
    }
}
