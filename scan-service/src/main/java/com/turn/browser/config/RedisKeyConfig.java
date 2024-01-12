package com.turn.browser.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix="spring.redis.key")
public class RedisKeyConfig {
	private Long maxItem; // Maximum number of cached records
	private String blocks; // blocks
	private String transactions; // transactions
	private String networkStat; // network statistics
	private String transferTx; //The contract calls the internal transfer transaction
	private String erc20Tx; // erc20 transaction
	private String erc721Tx; // erc721 transaction
	private String erc1155Tx; // erc1155 transaction
	private String addrGames; // Games the user participates in
	private String bubbleInfo; // bubble information
	private String bubblePreRelease; // bubble to be released
	private String bubbleRelease; // bubble release
}
