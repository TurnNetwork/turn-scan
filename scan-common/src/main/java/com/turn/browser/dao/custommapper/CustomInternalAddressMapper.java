package com.turn.browser.dao.custommapper;

import com.github.pagehelper.Page;
import com.turn.browser.bean.CountBalance;
import com.turn.browser.dao.entity.InternalAddress;
import com.turn.browser.dao.entity.InternalAddressExample;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface CustomInternalAddressMapper {

    int batchInsertOrUpdateSelective(@Param("list") Collection<InternalAddress> list, @Param("selective") InternalAddress.Column... selective);

    /**
     * Query the statistical balance
     *
     * @param
     * @return java.util.List<com.turn.browser.bean.CountBalance>
     */
    List<CountBalance> countBalance();

    /**
     * Query list based on conditions
     *
     * @param example
     * @return
     * @method selectByExample
     */
    Page<InternalAddress> selectListByExample(InternalAddressExample example);

}