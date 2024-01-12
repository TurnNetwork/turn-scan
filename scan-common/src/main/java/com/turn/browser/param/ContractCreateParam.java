package com.turn.browser.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * tyType=1004 Initiate delegation (entrust)
 */
@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class ContractCreateParam extends TxParam{

}
