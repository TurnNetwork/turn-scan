package com.turn.browser.cache;

import cn.hutool.core.util.StrUtil;
import com.bubble.abi.solidity.EventEncoder;
import com.bubble.crypto.Credentials;
import com.turn.browser.contract.TexasHoldem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TexasHoldemCache {

    public static final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap();

    public static final Credentials readCredentials = Credentials.create(
            "a43a4bfa7bf527ebf86e177ac1486e4976dd3d09fcdeef38395105bfe56c733c");

    @PostConstruct
    public void initCache() {
        String format = "get{}Events";
        cache.put(EventEncoder.encode(TexasHoldem.CALL_EVENT),
                  StrUtil.format(format, TexasHoldem.CALL_EVENT.getName()));
        cache.put(EventEncoder.encode(TexasHoldem.CHECK_EVENT),
                  StrUtil.format(format, TexasHoldem.CHECK_EVENT.getName()));
        cache.put(EventEncoder.encode(TexasHoldem.CREATETABLE_EVENT),
                  StrUtil.format(format, TexasHoldem.CREATETABLE_EVENT.getName()));
        cache.put(EventEncoder.encode(TexasHoldem.ENDROUND_EVENT),
                  StrUtil.format(format, TexasHoldem.ENDROUND_EVENT.getName()));
        cache.put(EventEncoder.encode(TexasHoldem.FOLD_EVENT),
                  StrUtil.format(format, TexasHoldem.FOLD_EVENT.getName()));
        cache.put(EventEncoder.encode(TexasHoldem.GAMEOVER_EVENT),
                  StrUtil.format(format, TexasHoldem.GAMEOVER_EVENT.getName()));
        cache.put(EventEncoder.encode(TexasHoldem.JOINGAME_EVENT),
                  StrUtil.format(format, TexasHoldem.JOINGAME_EVENT.getName()));
        cache.put(EventEncoder.encode(TexasHoldem.RAISE_EVENT),
                  StrUtil.format(format, TexasHoldem.RAISE_EVENT.getName()));
        cache.put(EventEncoder.encode(TexasHoldem.STARTROUND_EVENT),
                  StrUtil.format(format, TexasHoldem.STARTROUND_EVENT.getName()));
        cache.put(EventEncoder.encode(TexasHoldem.WINNER_EVENT),
                  StrUtil.format(format, TexasHoldem.WINNER_EVENT.getName()));
    }

}
