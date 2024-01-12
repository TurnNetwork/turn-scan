package com.turn.browser.v0150;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;

/**
 * v0.15.0.0版本配置
 */

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix="v0150")
public class V0150Config {
    // Effective version of the minimum lock-up amount parameter
    private BigInteger restrictingMinimumReleaseActiveVersion;
    // Adjustment effective version
    private BigInteger adjustmentActiveVersion;
    // Adjustment proposal ID
    private String adjustmentPipId;
    // Adjustment log output file (absolute path)
    private String adjustLogFilePath;
}