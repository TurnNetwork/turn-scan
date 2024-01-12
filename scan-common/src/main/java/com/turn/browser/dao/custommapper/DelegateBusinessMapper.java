package com.turn.browser.dao.custommapper;

import com.turn.browser.dao.param.BusinessParam;
import org.springframework.transaction.annotation.Transactional;


public interface DelegateBusinessMapper {
    /**
     * Initiate commission
     * @param param
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void create (BusinessParam param);

    /**
     * Exit the commission
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void exit (BusinessParam param);

    /**
     * // TODO: Write business logic SQL for receiving entrusted rewards and warehousing them.
     * Receive commission rewards
     * @param param
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void claim(BusinessParam param);
}