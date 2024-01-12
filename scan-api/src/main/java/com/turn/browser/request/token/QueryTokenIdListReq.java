package com.turn.browser.request.token;

import com.turn.browser.request.PageReq;
import lombok.Data;


/**
 * Query token id list request parameters
 */
@Data
public class QueryTokenIdListReq extends PageReq {

    private String contract;//

    private String address;//

    private String tokenId;//
}
