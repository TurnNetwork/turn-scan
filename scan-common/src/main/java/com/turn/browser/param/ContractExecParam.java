package com.turn.browser.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * tyType=1004
 */
@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class ContractExecParam extends TxParam{

}
