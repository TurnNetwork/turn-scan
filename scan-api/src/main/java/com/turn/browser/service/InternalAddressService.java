package com.turn.browser.service;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.turn.browser.bean.CountBalance;
import com.turn.browser.dao.custommapper.CustomInternalAddressMapper;
import com.turn.browser.dao.entity.InternalAddress;
import com.turn.browser.dao.entity.InternalAddressExample;
import com.turn.browser.request.PageReq;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.address.InternalAddressResp;
import com.bubble.utils.Convert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InternalAddressService {

    @Resource
    private CustomInternalAddressMapper customInternalAddressMapper;

    /**
     * Get a list of foundation accounts
     *
     * @param req
     * @return com.turn.browser.response.RespPage<com.turn.browser.response.address.InternalAddressResp>
     */
    public RespPage<InternalAddressResp> getFoundationInfo(PageReq req) {
        RespPage<InternalAddressResp> respPage = new RespPage<>();
        InternalAddressResp internalAddressResp = new InternalAddressResp();
        List<CountBalance> countBalanceList = customInternalAddressMapper.countBalance();
        CountBalance foundationValue = countBalanceList.stream().filter(v -> v.getType() == 0).findFirst().orElseGet(CountBalance::new);
        BigDecimal free = Convert.fromVon(foundationValue.getFree(), Convert.Unit.KPVON);
        internalAddressResp.setTotalBalance(free);
        BigDecimal lock = Convert.fromVon(foundationValue.getLocked(), Convert.Unit.KPVON);
        internalAddressResp.setTotalRestrictingBalance(lock);
        List<InternalAddressResp> lists = new ArrayList<>();
        lists.add(internalAddressResp);
        InternalAddressExample internalAddressExample = new InternalAddressExample();
        internalAddressExample.createCriteria().andTypeEqualTo(0).andIsShowEqualTo(true);
        internalAddressExample.setOrderByClause(" balance desc,restricting_balance desc");
        PageHelper.startPage(req.getPageNo(), req.getPageSize());
        Page<InternalAddress> internalAddressList = customInternalAddressMapper.selectListByExample(internalAddressExample);
        if (CollUtil.isNotEmpty(internalAddressList)) {
            for (int i = 0; i < internalAddressList.size(); i++) {
                BigDecimal balance = Convert.fromVon(internalAddressList.get(i).getBalance(), Convert.Unit.KPVON);
                internalAddressList.get(i).setBalance(balance);
                BigDecimal restrictingBalance = Convert.fromVon(internalAddressList.get(i).getRestrictingBalance(), Convert.Unit.KPVON);
                internalAddressList.get(i).setRestrictingBalance(restrictingBalance);
            }
        }
        internalAddressResp.setInternalAddressBaseResp(internalAddressList);
        respPage.init(internalAddressList, lists);
        return respPage;
    }

    /**
     * Get a list of foundation accounts
     *
     * @param req:
     * @return: com.turn.browser.response.RespPage<com.turn.browser.response.address.InternalAddrResp>
     */
    public RespPage<String> getInternalAddressList(PageReq req) {
        RespPage<String> respPage = new RespPage<>();
        InternalAddressExample internalAddressExample = new InternalAddressExample();
        internalAddressExample.createCriteria().andTypeEqualTo(0).andIsShowEqualTo(true);
        internalAddressExample.setOrderByClause(" address asc");
        PageHelper.startPage(req.getPageNo(), req.getPageSize());
        Page<InternalAddress> internalAddressList = customInternalAddressMapper.selectListByExample(internalAddressExample);
        List<String> internalAddrList = internalAddressList.stream().map(internalAddress -> internalAddress.getAddress()).collect(Collectors.toList());
        respPage.init(internalAddressList, internalAddrList);
        return respPage;
    }

}
