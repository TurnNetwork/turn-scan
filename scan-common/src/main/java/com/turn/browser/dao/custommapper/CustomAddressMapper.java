package com.turn.browser.dao.custommapper;

import com.turn.browser.bean.AddressErcQty;
import com.turn.browser.bean.AddressQty;
import com.turn.browser.bean.CustomAddressDetail;
import com.turn.browser.bean.RecoveredDelegationAmount;
import com.turn.browser.dao.entity.Address;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CustomAddressMapper {

    CustomAddressDetail findAddressDetail(String address);

    /**
     * Batch update based on address
     */
    int batchUpdateByAddress(@Param("list") List<RecoveredDelegationAmount> list);

    /**
     * Find burn contracts under different types
     *
     * @param type:
     * @return: java.util.List<java.lang.String>
     */
    List<String> findContractDestroy(@Param("type") Integer type);

    /**
     * The updated address has received the commission reward
     *
     * @param address:
     * @param amount:
     * @return: void
     */
    void updateAddressHaveReward(@Param("address") String address, @Param("amount") BigDecimal amount);

    /**
     * Batch update the number of erc transactions in the address table
     *
     * @param list:
     * @return: int
     */
    int batchUpdateAddressErcQty(@Param("list") List<AddressErcQty> list);

    /**
     * Number of transactions for updating address table in batches
     *
     * @param list:
     * @return: int
     */
    int batchUpdateAddressQty(@Param("list") List<AddressQty> list);

    /**
     * Batch update address information
     *
     * @param list:
     * @return: int
     */
    int batchUpdateAddressInfo(@Param("list") List<Address> list);

}
