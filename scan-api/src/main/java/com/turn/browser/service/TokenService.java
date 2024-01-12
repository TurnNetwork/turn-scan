package com.turn.browser.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.turn.browser.bean.CustomToken;
import com.turn.browser.bean.CustomTokenDetail;
import com.turn.browser.bean.CustomTokenInventory;
import com.turn.browser.config.DownFileCommon;
import com.turn.browser.dao.custommapper.CustomTokenInventoryMapper;
import com.turn.browser.dao.custommapper.CustomTokenMapper;
import com.turn.browser.dao.entity.TokenInventory;
import com.turn.browser.dao.entity.TokenInventoryExample;
import com.turn.browser.dao.entity.TokenInventoryKey;
import com.turn.browser.dao.mapper.TokenInventoryMapper;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.TokenTypeEnum;
import com.turn.browser.request.token.QueryTokenDetailReq;
import com.turn.browser.request.token.QueryTokenIdDetailReq;
import com.turn.browser.request.token.QueryTokenIdListReq;
import com.turn.browser.request.token.QueryTokenListReq;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.account.AccountDownload;
import com.turn.browser.response.token.QueryTokenDetailResp;
import com.turn.browser.response.token.QueryTokenIdDetailResp;
import com.turn.browser.response.token.QueryTokenIdListResp;
import com.turn.browser.response.token.QueryTokenListResp;
import com.turn.browser.utils.CommonUtil;
import com.turn.browser.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Token module implementation class
 */
@Slf4j
@Service
public class TokenService {

    @Resource
    private CustomTokenMapper customTokenMapper;

    @Resource
    private CustomTokenInventoryMapper customTokenInventoryMapper;

    @Resource
    private I18nUtil i18n;

    @Resource
    private DownFileCommon downFileCommon;

    @Resource
    private TokenInventoryMapper tokenInventoryMapper;

    public RespPage<QueryTokenListResp> queryTokenList(QueryTokenListReq req) {
        // page params: #{offset}, #{size}
        RespPage<QueryTokenListResp> result = new RespPage<>();
        PageHelper.startPage(req.getPageNo(), req.getPageSize());
        Page<CustomToken> customTokens = customTokenMapper.selectListByType(req.getType());
        List<QueryTokenListResp> data = customTokens.stream().map(customToken -> QueryTokenListResp.fromToken(customToken)).collect(Collectors.toList());
        result.init(customTokens, data);
        return result;
    }

    public QueryTokenDetailResp queryTokenDetail(QueryTokenDetailReq req) {
        CustomTokenDetail customTokenDetail = customTokenMapper.selectDetailByAddress(req.getAddress());
        if (!customTokenDetail.getType().equalsIgnoreCase(TokenTypeEnum.ERC1155.getType())) {
            // If the total supply is 0, then the value is the total inventory.
            int total = 0;
            if (ObjectUtil.isNotNull(customTokenDetail) && CommonUtil.ofNullable(() -> customTokenDetail.getTotalSupply()).orElse("0").equalsIgnoreCase("0")) {
                TokenInventoryExample example = new TokenInventoryExample();
                example.createCriteria().andTokenAddressEqualTo(req.getAddress());
                Page<TokenInventory> totalTokenInventory = tokenInventoryMapper.selectByExample(example);
                total = totalTokenInventory.size();
                customTokenDetail.setTotalSupply(Convert.toStr(total));
            }
        }
        return QueryTokenDetailResp.fromTokenDetail(customTokenDetail);
    }

    /**
     * ARC721 Stock List
     *
     * @param req
     * @return com.turn.browser.response.RespPage<com.turn.browser.response.token.QueryTokenIdListResp>
     */
    public RespPage<QueryTokenIdListResp> queryTokenIdList(QueryTokenIdListReq req) {
        RespPage<QueryTokenIdListResp> result = new RespPage<>();
        PageHelper.startPage(req.getPageNo(), req.getPageSize());
        TokenInventoryExample example = new TokenInventoryExample();
        TokenInventoryExample.Criteria criteria = example.createCriteria();
        //Query the list based on address, contract address, and tokenid
        if (StringUtils.isNotBlank(req.getAddress())) {
            criteria.andOwnerEqualTo(req.getAddress());
        }
        if (StringUtils.isNotBlank(req.getContract())) {
            criteria.andTokenAddressEqualTo(req.getContract());
        }
        if (StringUtils.isNotBlank(req.getTokenId())) {
            criteria.andTokenIdEqualTo(req.getTokenId());
        }
        Page<TokenInventory> tokenInventorys = tokenInventoryMapper.selectByExample(example);
        List<QueryTokenIdListResp> resps = new ArrayList<>();
        tokenInventorys.forEach(tokenInventory -> {
            QueryTokenIdListResp resp = QueryTokenIdListResp.fromToken(tokenInventory);
            resps.add(resp);
        });
        result.init(tokenInventorys, resps);
        return result;
    }

    public QueryTokenIdDetailResp queryTokenIdDetail(QueryTokenIdDetailReq req) {
        TokenInventoryKey tokenInventoryKey = new TokenInventoryKey();
        tokenInventoryKey.setTokenAddress(req.getContract());
        tokenInventoryKey.setTokenId(StrUtil.emptyToDefault(req.getTokenId(), "0"));
        CustomTokenInventory customTokenInventory = customTokenInventoryMapper.selectTokenInventory(tokenInventoryKey);
        return QueryTokenIdDetailResp.copy(customTokenInventory);
    }

    public AccountDownload exportTokenId(String address, String contract, String tokenId, String local, String timeZone) {
        PageHelper.startPage(1, 3000);
        TokenInventoryExample example = new TokenInventoryExample();
        TokenInventoryExample.Criteria criteria = example.createCriteria();
        //Query the list based on address, contract address, and tokenid
        if (StringUtils.isNotBlank(address)) {
            criteria.andOwnerEqualTo(address);
        }
        if (StringUtils.isNotBlank(contract)) {
            criteria.andTokenAddressEqualTo(contract);
        }
        if (StringUtils.isNotBlank(tokenId)) {
            criteria.andTokenIdEqualTo(tokenId);
        }
        Page<TokenInventory> tokenInventorys = tokenInventoryMapper.selectByExample(example);
        String[] headers = {this.i18n.i(I18nEnum.DOWNLOAD_TOKEN_CSV_NAME, local), this.i18n.i(I18nEnum.DOWNLOAD_TOKEN_CSV_TOKEN, local), this.i18n.i(I18nEnum.DOWNLOAD_TOKEN_CSV_ADDRESS,
                                                                                                                                                     local), this.i18n.i(I18nEnum.DOWNLOAD_TOKEN_CSV_TOKEN_ID,
                                                                                                                                                                         local), this.i18n.i(I18nEnum.DOWNLOAD_TOKEN_CSV_TX_COUNT,
                                                                                                                                                                                              local)};
        List<Object[]> rows = new ArrayList<>();
        tokenInventorys.forEach(tokenInventory -> {
            Object[] row = {tokenInventory.getName(), tokenInventory.getTokenAddress(), tokenInventory.getOwner(), tokenInventory.getTokenId(), tokenInventory.getTokenTxQty()};
            rows.add(row);
        });
        return this.downFileCommon.writeDate("Token-Id-" + address + "-" + System.currentTimeMillis() + ".CSV", rows, headers);

    }

}
