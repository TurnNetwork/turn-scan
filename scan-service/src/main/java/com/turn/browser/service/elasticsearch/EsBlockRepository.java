package com.turn.browser.service.elasticsearch;

import org.springframework.stereotype.Repository;

/**
 * @Description: Block operations
 */
@Repository
public class EsBlockRepository extends AbstractEsRepository {
    @Override
    public String getIndexName() {
        return config.getBlockIndexName();
    }
    @Override
    public String getTemplateFileName() {
        return "block";
    }
}
