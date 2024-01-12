package com.turn.browser.utils;

import com.bubble.parameters.NetworkParameters;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Data
public class NetworkParams {

    @Value("${turn.chainId}")
    private long chainId;

    @Value("${turn.addressPrefix:lat}")
    private String hrp;

    @Value("${turn.valueUnit:TURN}")
    private String unit;

    @PostConstruct
    public void init() {
        NetworkParameters.init(chainId);
    }

}
