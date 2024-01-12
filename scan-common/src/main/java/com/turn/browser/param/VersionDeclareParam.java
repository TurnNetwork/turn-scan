package com.turn.browser.param;

import com.turn.browser.utils.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * txType=2004
 */
@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class VersionDeclareParam extends TxParam{

    /**
     * The declared node can only be a validator/candidate
     */
    private String activeNode;
    public void setActiveNode(String activeNode){
        this.activeNode= HexUtil.prefix(activeNode);
    }

    /**
     * Declared version
     */
    private Integer version;

    /**
     * Node name
     */
    private String nodeName;

    /**
     * Declared version signature
     */
    private String versionSigns;
}
