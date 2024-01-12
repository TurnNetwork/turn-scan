package com.turn.browser.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.turn.browser.bean.AddressErcQty;
import com.turn.browser.bean.TokenQty;
import com.turn.browser.dao.custommapper.CustomAddressMapper;
import com.turn.browser.dao.custommapper.CustomTokenMapper;
import com.turn.browser.dao.entity.*;
import com.turn.browser.dao.mapper.PointLogMapper;
import com.turn.browser.dao.mapper.TxErc1155BakMapper;
import com.turn.browser.dao.mapper.TxErc20BakMapper;
import com.turn.browser.dao.mapper.TxErc721BakMapper;
import com.turn.browser.elasticsearch.dto.ErcTx;
import com.turn.browser.utils.AddressUtil;
import com.turn.browser.utils.TaskUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UpdateTokenQtyTask {

    @Resource
    private CustomTokenMapper customTokenMapper;

    @Resource
    private CustomAddressMapper customAddressMapper;

    @Resource
    private PointLogMapper pointLogMapper;

    @Resource
    private TxErc20BakMapper txErc20BakMapper;

    @Resource
    private TxErc721BakMapper txErc721BakMapper;

    @Resource
    private TxErc1155BakMapper txErc1155BakMapper;

    /**
     * Update the number of erc transactions
     * Executed every 5 minutes
     *
     * @param :
     * @return: void
     */
    @XxlJob("updateTokenQtyJobHandler")
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void updateTokenQty() throws Exception {
        try {
            int pageSize = Convert.toInt(XxlJobHelper.getJobParam(), 500);
            Map<String, TokenQty> tokenMap = new HashMap<>();
            Map<String, AddressErcQty> addressMap = new HashMap<>();
            PointLog erc20PointLog = pointLogMapper.selectByPrimaryKey(3);
            long oldErc20Position = Convert.toLong(erc20PointLog.getPosition());
            TaskUtil.console("The current page number is [{}], and the erc20 breakpoint is [{}]", pageSize, oldErc20Position);
            TxErc20BakExample txErc20BakExample = new TxErc20BakExample();
            txErc20BakExample.setOrderByClause("id asc limit " + pageSize);
            txErc20BakExample.createCriteria().andIdGreaterThan(oldErc20Position);
            List<TxErc20Bak> erc20List = txErc20BakMapper.selectByExample(txErc20BakExample);
            if (CollUtil.isNotEmpty(erc20List)) {
                TaskUtil.console("Found erc20 transactions [{}]", erc20List.size());
                Map<String, List<ErcTx>> erc20Map = erc20List.stream().collect(Collectors.groupingBy(ErcTx::getContract));
                //Cumulative number of erc20 transactions of token
                for (Map.Entry<String, List<ErcTx>> entry : erc20Map.entrySet()) {
                    TokenQty tokenQty = getTokenQty(tokenMap, entry.getKey());
                    tokenQty.setErc20TxQty(entry.getValue().size());
                }
                //The number of erc20 transactions of the accumulated address
                for (ErcTx ercTx : erc20List) {
                    AddressErcQty fromAddressErcQty = getAddressErcQty(addressMap, ercTx.getFrom());
                    AddressErcQty toAddressErcQty = getAddressErcQty(addressMap, ercTx.getTo());
                    if (ercTx.getFrom().equalsIgnoreCase(ercTx.getTo()) && !AddressUtil.isAddrZero(ercTx.getFrom())) {
                        fromAddressErcQty.setErc20TxQty(fromAddressErcQty.getErc20TxQty() + 1);
                        TaskUtil.console("The from[{}] and to[{}] addresses of this erc20 transaction [{}] are consistent, and the number of erc transactions is only counted once", ercTx.getHash(), ercTx.getFrom(), ercTx.getTo ());
                    } else {
                        if (!AddressUtil.isAddrZero(fromAddressErcQty.getAddress())) {
                            fromAddressErcQty.setErc20TxQty(fromAddressErcQty.getErc20TxQty() + 1);
                        } else {
                            TaskUtil.console("Under this erc20 transaction [{}], the zero address [{}] does not count the number of transactions", ercTx.getHash(), fromAddressErcQty.getAddress());
                        }
                        if (!AddressUtil.isAddrZero(toAddressErcQty.getAddress())) {
                            toAddressErcQty.setErc20TxQty(toAddressErcQty.getErc20TxQty() + 1);
                        } else {
                            TaskUtil.console("Under this erc20 transaction [{}], the zero address [{}] does not count the number of transactions", ercTx.getHash(), toAddressErcQty.getAddress());
                        }
                    }
                }
                //Record the largest seq
                erc20PointLog.setPosition(CollUtil.getLast(erc20List).getId().toString());
            } else {
                TaskUtil.console("The current erc20 breakpoint [{}] does not find the erc20 transaction", oldErc20Position);
            }
            PointLog erc721PointLog = pointLogMapper.selectByPrimaryKey(4);
            long oldErc721Position = Convert.toLong(erc721PointLog.getPosition());
            TaskUtil.console("The current page number is [{}], erc721 breakpoint is [{}]", pageSize, oldErc721Position);
            TxErc721BakExample txErc721BakExample = new TxErc721BakExample();
            txErc721BakExample.setOrderByClause("id asc limit " + pageSize);
            txErc721BakExample.createCriteria().andIdGreaterThan(oldErc721Position);
            List<TxErc721Bak> erc721List = txErc721BakMapper.selectByExample(txErc721BakExample);
            if (CollUtil.isNotEmpty(erc721List)) {
                TaskUtil.console("Found erc721 transactions [{}]", erc721List.size());
                Map<String, List<ErcTx>> erc721Map = erc721List.stream().collect(Collectors.groupingBy(ErcTx::getContract));
                //Cumulative number of erc721 transactions of token
                for (Map.Entry<String, List<ErcTx>> entry : erc721Map.entrySet()) {
                    TokenQty tokenQty = getTokenQty(tokenMap, entry.getKey());
                    tokenQty.setErc721TxQty(entry.getValue().size());
                }
                ///Cumulative number of erc721 transactions for the address
                for (ErcTx ercTx : erc721List) {
                    AddressErcQty fromAddressErcQty = getAddressErcQty(addressMap, ercTx.getFrom());
                    AddressErcQty toAddressErcQty = getAddressErcQty(addressMap, ercTx.getTo());
                    if (ercTx.getFrom().equalsIgnoreCase(ercTx.getTo()) && !AddressUtil.isAddrZero(ercTx.getFrom())) {
                        fromAddressErcQty.setErc721TxQty(fromAddressErcQty.getErc721TxQty() + 1);
                        TaskUtil.console("The from[{}] and to[{}] addresses of this erc721 transaction [{}] are consistent, and the number of erc transactions is only counted once", ercTx.getHash(), ercTx.getFrom(), ercTx.getTo ());
                    } else {
                        if (!AddressUtil.isAddrZero(fromAddressErcQty.getAddress())) {
                            fromAddressErcQty.setErc721TxQty(fromAddressErcQty.getErc721TxQty() + 1);
                        } else {
                            TaskUtil.console("Under this erc721 transaction [{}], the zero address [{}] does not count the number of transactions", ercTx.getHash(), fromAddressErcQty.getAddress());
                        }
                        if (!AddressUtil.isAddrZero(toAddressErcQty.getAddress())) {
                            toAddressErcQty.setErc721TxQty(toAddressErcQty.getErc721TxQty() + 1);
                        } else {
                            TaskUtil.console("Under this erc721 transaction [{}], the zero address [{}] does not count the number of transactions", ercTx.getHash(), toAddressErcQty.getAddress());
                        }
                    }
                }
                //Record the largest seq
                erc721PointLog.setPosition(CollUtil.getLast(erc721List).getId().toString());
            } else {
                TaskUtil.console("The current erc721 breakpoint [{}] did not find the erc721 transaction", oldErc721Position);
            }
            // 1155
            PointLog erc1155PointLog = pointLogMapper.selectByPrimaryKey(10);
            long oldErc1155Position = Convert.toLong(erc1155PointLog.getPosition());
            TaskUtil.console("The current page number is [{}], and the erc1155 breakpoint is [{}]", pageSize, oldErc1155Position);
            TxErc1155BakExample txErc1155BakExample = new TxErc1155BakExample();
            txErc1155BakExample.setOrderByClause("id");
            txErc1155BakExample.createCriteria().andIdGreaterThan(oldErc1155Position).andIdLessThanOrEqualTo(oldErc1155Position + pageSize);
            List<TxErc1155Bak> erc1155List = txErc1155BakMapper.selectByExample(txErc1155BakExample);
            if (CollUtil.isNotEmpty(erc1155List)) {
                TaskUtil.console("Found 1155 transactions[{}]", erc1155List.size());
                Map<String, List<ErcTx>> erc1155Map = erc1155List.stream().collect(Collectors.groupingBy(ErcTx::getContract));
                //Cumulative number of erc1155 transactions of token
                for (Map.Entry<String, List<ErcTx>> entry : erc1155Map.entrySet()) {
                    TokenQty tokenQty = getTokenQty(tokenMap, entry.getKey());
                    tokenQty.setErc1155TxQty(entry.getValue().size());
                }
                //The cumulative number of erc1155 transactions for the address
                for (ErcTx ercTx : erc1155List) {
                    AddressErcQty fromAddressErcQty = getAddressErcQty(addressMap, ercTx.getFrom());
                    AddressErcQty toAddressErcQty = getAddressErcQty(addressMap, ercTx.getTo());
                    if (ercTx.getFrom().equalsIgnoreCase(ercTx.getTo()) && !AddressUtil.isAddrZero(ercTx.getFrom())) {
                        fromAddressErcQty.setErc1155TxQty(fromAddressErcQty.getErc1155TxQty() + 1);
                        TaskUtil.console("The from[{}] and to[{}] addresses of this erc1155 transaction [{}] are consistent, and the number of erc transactions is only counted once", ercTx.getHash(), ercTx.getFrom(), ercTx.getTo ());
                    } else {
                        if (!AddressUtil.isAddrZero(fromAddressErcQty.getAddress())) {
                            fromAddressErcQty.setErc1155TxQty(fromAddressErcQty.getErc1155TxQty() + 1);
                        } else {
                            TaskUtil.console("Under this erc1155 transaction [{}], the zero address [{}] does not count the number of transactions", ercTx.getHash(), fromAddressErcQty.getAddress());
                        }
                        if (!AddressUtil.isAddrZero(toAddressErcQty.getAddress())) {
                            toAddressErcQty.setErc1155TxQty(toAddressErcQty.getErc1155TxQty() + 1);
                        } else {
                            TaskUtil.console("Under this erc1155 transaction [{}], the zero address [{}] does not count the number of transactions", ercTx.getHash(), toAddressErcQty.getAddress());
                        }
                    }
                }
                //Record the largest seq
                erc1155PointLog.setPosition(CollUtil.getLast(erc1155List).getId().toString());
            } else {
                TaskUtil.console("The current erc1155 breakpoint [{}] did not find the erc1155 transaction", oldErc1155Position);
            }
            if (CollUtil.isNotEmpty(tokenMap.values())) {
                for (Map.Entry<String, TokenQty> entry : tokenMap.entrySet()) {
                    entry.getValue().setTokenTxQty(entry.getValue().getErc20TxQty() + entry.getValue().getErc721TxQty() + entry.getValue().getErc1155TxQty());
                }
                List<TokenQty> list = CollUtil.newArrayList(tokenMap.values());
                customTokenMapper.batchUpdateTokenQty(list);
                TaskUtil.console("Update the number of erc transactions in the token table, the number of tokens involved is [{}], and the modified data is {}", list.size(), JSONUtil.toJsonStr(list));
            }
            if (CollUtil.isNotEmpty(addressMap.values())) {
                List<AddressErcQty> list = CollUtil.newArrayList(addressMap.values());
                customAddressMapper.batchUpdateAddressErcQty(list);
                TaskUtil.console("Update the number of erc transactions in the address table, the number of addresses involved is [{}], and the modified data is {}", list.size(), JSONUtil.toJsonStr(list));
            }
            if (CollUtil.isNotEmpty(erc20List)) {
                pointLogMapper.updateByPrimaryKeySelective(erc20PointLog);
                TaskUtil.console("Update the number of erc transactions, the erc20 breakpoint is [{}]->[{}]", oldErc20Position, erc20PointLog.getPosition());
            }
            if (CollUtil.isNotEmpty(erc721List)) {
                pointLogMapper.updateByPrimaryKeySelective(erc721PointLog);
                TaskUtil.console("Update the number of erc transactions, erc721 breakpoint is [{}]->[{}]", oldErc721Position, erc721PointLog.getPosition());
            }
            if (CollUtil.isNotEmpty(erc1155List)) {
                pointLogMapper.updateByPrimaryKeySelective(erc1155PointLog);
                TaskUtil.console("Update the number of erc transactions, erc1155 breakpoint is [{}]->[{}]", oldErc1155Position, erc1155PointLog.getPosition());
            }
            XxlJobHelper.handleSuccess("Update erc transaction number successfully");
        } catch (Exception e) {
            log.error("Abnormal update of erc transaction number", e);
            throw e;
        }
    }

    /**
     * GetAddressErcQty
     *
     * @param addressMap:
     * @param address:
     * @return: com.turn.browser.bean.AddressErcQty
     */
    private AddressErcQty getAddressErcQty(Map<String, AddressErcQty> addressMap, String address) {
        if (addressMap.containsKey(address)) {
            return addressMap.get(address);
        } else {
            AddressErcQty addressErcQty = AddressErcQty.builder().address(address).erc20TxQty(0).erc721TxQty(0).erc1155TxQty(0).build();
            addressMap.put(address, addressErcQty);
            return addressErcQty;
        }
    }

    /**
     * GetTokenQty
     *
     * @param tokenMap:
     * @param contract:
     * @return: com.turn.browser.bean.TokenQty
     */
    private TokenQty getTokenQty(Map<String, TokenQty> tokenMap, String contract) {
        if (tokenMap.containsKey(contract)) {
            return tokenMap.get(contract);
        } else {
            TokenQty tokenQty = TokenQty.builder().contract(contract).tokenTxQty(0).erc20TxQty(0).erc721TxQty(0).erc1155TxQty(0).build();
            tokenMap.put(contract, tokenQty);
            return tokenQty;
        }
    }

}
