package com.turn.browser.elasticsearch.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Accessors(chain = true)
public class DelegationReward {

    private Long id;

    private String hash;

    private Long bn;

    private String addr;

    private Date time;

    private Date creTime;

    private Date updTime;

    private String extra;

    private String extraClean;

    @Data
    public static class Extra {

        private String nodeName;

        private String nodeId;

        private String reward;

        /********Convenient method for converting string-like values to large floating-point numbers********/
        public BigDecimal decimalReward() {
            return new BigDecimal(this.getReward());
        }

    }

}