package com.turn.browser.service.elasticsearch;

import org.springframework.stereotype.Repository;

/**
 * @Description: Delegate reward operations
 */
@Repository
public class EsDelegationRewardRepository extends AbstractEsRepository {
    @Override
    public String getIndexName() {
        return config.getDelegationRewardIndexName();
    }

    @Override
    public String getTemplateFileName() {
        return "delegate-reward";
    }
}
