package com.turn.browser.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: ES configuration
 */
@Data
@Configuration
@ConfigurationProperties(prefix="spring.elasticsearch.index")
public class EsIndexConfig {
    private String blockIndexName; // block index name
    private String transactionIndexName; //Transaction index name
    private String delegationIndexName; // Delegation index name
    private String nodeOptIndexName; //Node operation log index name
    private String delegationRewardIndexName; // Delegation reward index name
    private String transferTxIndexName; // Main transaction internal transfer transaction index name
    private String erc20TxIndexName; // Main transaction internal erc20 transaction index name (new)
    private String erc721TxIndexName; // Main transaction internal erc721 transaction index name
    private String erc1155TxIndexName; // Main transaction internal erc1155 transaction index name
    private String microNodeOptIndexName; //Node operation log index name
    private String subChainTxIndexName; // sub chain tx index name
}