package com.turn.browser.param;

import com.bubble.contracts.dpos.dto.req.SettlementInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SettleBubbleParam extends TxParam {

    private BigInteger bubbleId;

    private String L2SettleTxHash;

    private SettlementInfo settlementInfo;

}
