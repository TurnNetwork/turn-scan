package com.turn.browser.service.elasticsearch;

import org.springframework.stereotype.Repository;


@Repository
public class EsMicroNodeOptRepository extends AbstractEsRepository {
    @Override
    public String getIndexName() {
        return config.getMicroNodeOptIndexName();
    }
    @Override
    public String getTemplateFileName() {
        return "micronodeopt";
    }
}
