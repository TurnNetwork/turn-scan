package com.turn.browser.dao.custommapper;

import com.turn.browser.dao.param.BusinessParam;
import org.springframework.transaction.annotation.Transactional;


public interface RestrictingBusinessMapper {

    /**
     * Create Restricting plan
     *
     * @param param
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void create(BusinessParam param);

}