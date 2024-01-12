package com.turn.browser.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.bubble.contracts.dpos.RestrictingPlanContract;
import com.bubble.contracts.dpos.dto.CallResponse;
import com.bubble.contracts.dpos.dto.resp.RestrictingItem;
import com.bubble.contracts.dpos.dto.resp.Reward;
import com.bubble.protocol.core.DefaultBlockParameterName;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.turn.browser.bean.CustomAddressDetail;
import com.turn.browser.bean.DlLock;
import com.turn.browser.bean.LockDelegate;
import com.turn.browser.bean.RestrictingBalance;
import com.turn.browser.cache.AddrGameCacheDto;
import com.turn.browser.client.TurnClient;
import com.turn.browser.client.SpecialApi;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.CustomAddressMapper;
import com.turn.browser.dao.custommapper.CustomRpPlanMapper;
import com.turn.browser.dao.entity.*;
import com.turn.browser.dao.mapper.AddrGameMapper;
import com.turn.browser.dao.mapper.RpPlanMapper;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RetEnum;
import com.turn.browser.enums.TokenTypeEnum;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.request.address.QueryAddrGameListReq;
import com.turn.browser.request.address.QueryDetailRequest;
import com.turn.browser.request.address.QueryRPPlanDetailRequest;
import com.turn.browser.response.BaseResp;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.address.AddrGameDetailResp;
import com.turn.browser.response.address.DetailsRPPlanResp;
import com.turn.browser.response.address.QueryDetailResp;
import com.turn.browser.response.address.QueryRPPlanDetailResp;
import com.turn.browser.response.microNode.AliveMicroNodeListResp;
import com.turn.browser.service.elasticsearch.EsBlockRepository;
import com.turn.browser.utils.ConvertUtil;
import com.turn.browser.utils.I18nUtil;
import com.turn.browser.v0152.bean.AddrGameDetailDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Specific logic implementation method for address participation in the game
 */
@Service
public class AddrGameService {

    private final Logger logger = LoggerFactory.getLogger(AddrGameService.class);

    @Resource
    private AddrGameCacheService addrGameCacheService;

    @Resource
    private I18nUtil i18n;

    /**
     * query address participation details
     *
     * @param req
     * @return com.turn.browser.response.address.QueryDetailResp
     */
    public BaseResp<List<AddrGameDetailResp>> getList(QueryAddrGameListReq req) {
        BaseResp<List<AddrGameDetailResp>> baseResp = new BaseResp<>();
        // If you query the 0 address, return directly
        if (StrUtil.isNotBlank(req.getAddress()) && com.turn.browser.utils.AddressUtil.isAddrZero(req.getAddress())) {
            return baseResp;
        }
        List<AddrGameDetailDto> addrGameList = addrGameCacheService.getAddrGameCache(req.getAddress());

        List<AddrGameDetailResp> lists = new LinkedList<>();
        for (AddrGameDetailDto addrGameDetailDto : addrGameList) {
            AddrGameDetailResp resp = new AddrGameDetailResp();
            BeanUtils.copyProperties(addrGameDetailDto, resp);
            lists.add(resp);
        }
        return BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS),lists);
    }

}
