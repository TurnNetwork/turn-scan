package com.turn.browser.decoder;

import com.turn.browser.elasticsearch.dto.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Ordinary contract (EVM||WASM) transaction analysis tool:
 * Get the contract type and parameters based on tx input
 * EVM contract does not have MAGIC_NUM
 * WASM contract has MAGIC_NUM
 */
@Slf4j
public class TxInputDecodeUtil {
    private TxInputDecodeUtil(){}
    public static TxInputDecodeResult decode(String txInput) {
        TxInputDecodeResult result = new TxInputDecodeResult();
        result.setTypeEnum(Transaction.TypeEnum.EVM_CONTRACT_CREATE);
        try {
            if (StringUtils.isNotEmpty(txInput) && !txInput.equals("0x")) {
            	if(txInput.length() > 9) {
            		String prefix = txInput.substring(0, 10);
                    /**
                     * If the first eight digits are equal, it is considered a wasm contract
                     */
                    if("0x0061736d".equals(prefix)) {
                        result.setTypeEnum(Transaction.TypeEnum.WASM_CONTRACT_CREATE);
                    }
                }

            }
        } catch (Exception e) {
            log.error("Error in parsing the ordinary contract transaction input, the system will identify the current transaction as EVM contract creation by default:",e);
        }
        return result;
    }
}
