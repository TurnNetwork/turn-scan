package com.turn.browser.v0152.analyzer;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.turn.browser.bean.ErcToken;
import com.turn.browser.dao.entity.Token;
import com.turn.browser.dao.mapper.TokenMapper;
import com.turn.browser.enums.ErcTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ErcCache {

    Map<String, ErcToken> tokenCache = new ConcurrentHashMap<>();

    Set<String> erc20AddressCache = new ConcurrentHashSet<>();

    Set<String> erc721AddressCache = new ConcurrentHashSet<>();

    Set<String> erc1155AddressCache = new ConcurrentHashSet<>();

    @Resource
    private TokenMapper tokenMapper;

    /**
     * Initialize token address to cache
     */
    public void init() {
        log.info("Initialize token address to cache");
        List<Token> tokens = tokenMapper.selectByExample(null);
        tokens.forEach(token -> {
            ErcToken et = new ErcToken();
            BeanUtils.copyProperties(token, et);
            ErcTypeEnum typeEnum = ErcTypeEnum.valueOf(token.getType().toUpperCase());
            et.setTypeEnum(typeEnum);
            tokenCache.put(et.getAddress(), et);
            switch (typeEnum) {
                case ERC20:
                    erc20AddressCache.add(token.getAddress());
                    break;
                case ERC721:
                    erc721AddressCache.add(token.getAddress());
                    break;
                case ERC1155:
                    erc1155AddressCache.add(token.getAddress());
                    break;
            }
        });
    }

    public Map<String, ErcToken> getTokenCache() {
        return Collections.unmodifiableMap(tokenCache);
    }

    public Collection<String> getErc20AddressCache() {
        return Collections.unmodifiableCollection(erc20AddressCache);
    }

    public Collection<String> getErc721AddressCache() {
        return Collections.unmodifiableCollection(erc721AddressCache);
    }

    public Collection<String> getErc1155AddressCache() {
        return Collections.unmodifiableCollection(erc1155AddressCache);
    }

}
