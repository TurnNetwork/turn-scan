package com.turn.browser.service.redis;

/**
 * redis key
 *
 * @date 2021/5/21
 */
public enum RedisKeyEnum {

    Block("Block"),
    Transaction("Transaction"),
    Statistic("Statistic"),
    Erc20Tx("Erc20Tx"),
    Erc721Tx("Erc721Tx"),
    Erc1155Tx("Erc1155Tx");

    private String key;

    RedisKeyEnum(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
