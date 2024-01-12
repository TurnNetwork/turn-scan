package com.turn.browser.constant;

import java.math.BigDecimal;

/**
 * Unify const definition
 */
public class Browser {
	private Browser(){}

	/** Query the maximum number of caches */
	public static final Integer MAX_NUM = 500000;

	/**http header*/
	public static final String HTTP = "http://";

	/**https header*/
	public static final String HTTPS = "https://";

	/** pre*/
	public static final String WALLET_PRX = "0x";

	/** Operation list separator*/
	public static final String OPT_SPILT = "\\|";

	/** Operation list separator*/
	public static final String PEAD_SPILT = "|";

	/**http delimiter*/
	public static final String HTTP_SPILT = ",";

	/** pip prefix*/
	public static final String PIP_NAME = "PIP-";


	/**Default proposal title*/
	public static final String INQUIRY = "inquiry";

	/**WEB timeout*/
	public static final Integer WEB_TIME_OUT = 30000;

	/**
	 * The name of turn conversion
	 */
	public static final String EXTRA_TURN_PARAM = "stakeThreshold,operatingThreshold,minimumRelease";

	/**
	 * The name of the percentage conversion
	 */
	public static final String EXTRA_PECENT_PARAM = "slashFractionDuplicateSign,duplicateSignReportReward";

	/**
	 * Divided parameter
	 */
	public static final BigDecimal PERCENTAGE = new BigDecimal(100);

	/**
	 * Set the ERC20 key
	 */
	public static final String ERC_BALANCE_KEY = "erc20";

	/**
	 * erc20 parameter separator
	 */
	public static final String ERC_SPILT = "#";

}