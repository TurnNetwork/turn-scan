package com.turn.browser.bean;

import java.math.BigDecimal;


import lombok.Data;

/**
 * Address delegate return object
 */
@Data
public class DelegationAddress {

	private String nodeId;
	
	private String nodeName;
	
	private BigDecimal delegateHes;
	
	private BigDecimal delegateLocked;
	
	private BigDecimal delegateReleased;
	
}
