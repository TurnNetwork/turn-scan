package com.turn.browser.response.address;


/**
 * The object returned by the query address
 */
public class AddrGameDetailResp {

    private String address;

    private Long roundId;

    private String gameContractAddress;

    private Long bubbleId;

    private String tokenAddress;

    private String tokenSymbol;

    private Integer tokenDecimal;

    private String tokenRpc;

    public String getTokenRpc() {
        return tokenRpc;
    }

    public void setTokenRpc(String tokenRpc) {
        this.tokenRpc = tokenRpc;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public String getTokenSymbol() {
        return tokenSymbol;
    }

    public void setTokenSymbol(String tokenSymbol) {
        this.tokenSymbol = tokenSymbol;
    }

    public Integer getTokenDecimal() {
        return tokenDecimal;
    }

    public void setTokenDecimal(Integer tokenDecimal) {
        this.tokenDecimal = tokenDecimal;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGameContractAddress() {
        return gameContractAddress;
    }

    public void setGameContractAddress(String gameContractAddress) {
        this.gameContractAddress = gameContractAddress;
    }

    public Long getBubbleId() {
        return bubbleId;
    }

    public void setBubbleId(Long bubbleId) {
        this.bubbleId = bubbleId;
    }

    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }
}
