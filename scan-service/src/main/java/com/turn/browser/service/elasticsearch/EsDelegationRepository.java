package com.turn.browser.service.elasticsearch;

import org.springframework.stereotype.Repository;

/**
 * @Description: Trading operations
 */
@Repository
public class EsDelegationRepository extends AbstractEsRepository {
    @Override
    public String getIndexName() {
        return config.getDelegationIndexName();
    }
    @Override
    public String getTemplateFileName() {
        return "delegate";
    }
}
