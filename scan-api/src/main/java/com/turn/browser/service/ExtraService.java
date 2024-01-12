package com.turn.browser.service;

import com.turn.browser.constant.Browser;
import com.turn.browser.dao.entity.Config;
import com.turn.browser.dao.mapper.ConfigMapper;
import com.turn.browser.response.extra.ConfigDetail;
import com.turn.browser.response.extra.ModuleConfig;
import com.turn.browser.response.extra.QueryConfigResp;
import com.bubble.utils.Convert;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


@Service
public class ExtraService {

    @Resource
    private ConfigMapper configMapper;

    private static String stakingValue = "staking";

    private static String slashingValue = "slashing";

    private static String blockValue = "block";

    private static String rewardValue = "reward";

    private static String restrictingValue = "restricting";

    public QueryConfigResp queryConfig() {
        QueryConfigResp queryConfigResp = new QueryConfigResp();
        List<ModuleConfig> moduleConfigs = new ArrayList<>();
        List<Config> configs = configMapper.selectByExample(null);
        /**
         * Set the configuration of the staking module
         */
        ModuleConfig stakingModuleConfig = new ModuleConfig();
        stakingModuleConfig.setModule(stakingValue);
        /**
         * Set the configuration of the penalty module
         */
        ModuleConfig slashingModuleConfig = new ModuleConfig();
        slashingModuleConfig.setModule(slashingValue);
        /**
         * Set the configuration of the block module
         */
        ModuleConfig blockModuleConfig = new ModuleConfig();
        blockModuleConfig.setModule(blockValue);
        /**
         * Set the configuration of the revenue proportion module
         */
        ModuleConfig rewardModuleConfig = new ModuleConfig();
        rewardModuleConfig.setModule(rewardValue);
        /**
         * Set the configuration of the M parameter module
         */
        ModuleConfig restrictingModuleConfig = new ModuleConfig();
        restrictingModuleConfig.setModule(restrictingValue);

        List<ConfigDetail> stakingConfigDetails = new ArrayList<>();
        List<ConfigDetail> slashingConfigDetails = new ArrayList<>();
        List<ConfigDetail> blockConfigDetails = new ArrayList<>();
        List<ConfigDetail> rewardConfigDetails = new ArrayList<>();
        List<ConfigDetail> restrictingConfigDetails = new ArrayList<>();
        String maxEvidenceAge = "";
        String unStakeFreezeDuration = "";
        String zeroProduceCumulativeTime = "";
        String zeroProduceNumberThreshold = "";
        for (Config config : configs) {
            ConfigDetail configDetail = new ConfigDetail();
            /**
             * Regular expression split configuration, where $1=starting opening and closing intervals. $2=Start interval. $2=End range. $4=End opening and closing range.
             */
            String regex = ".*([\\(\\[])(.*),(.*)([\\)\\]]).*";//()表示分组
            String res1 = config.getRangeDesc().replaceAll(regex, "$1");
            String res2 = config.getRangeDesc().replaceAll(regex, "$2");
            String res3 = config.getRangeDesc().replaceAll(regex, "$3");
            String res4 = config.getRangeDesc().replaceAll(regex, "$4");
            configDetail.setStart(res1);
            configDetail.setEnd(res4);
            BeanUtils.copyProperties(config, configDetail);
            /**
             * turn conversion unit
             */
            if (Browser.EXTRA_TURN_PARAM.contains(config.getName())) {
                if (StringUtils.isNotBlank(res2)) {
                    configDetail.setStartValue(Convert.fromVon(res2.trim(), Convert.Unit.KPVON).setScale(18, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
                }
                if (StringUtils.isNotBlank(res3)) {
                    configDetail.setEndValue(Convert.fromVon(res3.trim(), Convert.Unit.KPVON).setScale(18, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
                }
                if (StringUtils.isNotBlank(config.getValue())) {
                    configDetail.setValue(Convert.fromVon(config.getValue(), Convert.Unit.KPVON).setScale(18, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
                }
                if (StringUtils.isNotBlank(config.getInitValue())) {
                    configDetail.setInitValue(Convert.fromVon(config.getInitValue(), Convert.Unit.KPVON).setScale(18, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
                }
            } else {
                configDetail.setStartValue(res2.trim());
                configDetail.setEndValue(res3.trim());
            }
            /**
             * Percent conversion
             */
            if (Browser.EXTRA_PECENT_PARAM.contains(config.getName())) {
                configDetail.setValue(new BigDecimal(config.getValue()).setScale(18, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
                configDetail.setInitValue(new BigDecimal(config.getInitValue()).setScale(18, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
            }
            switch (config.getModule()) {
                case "staking":
                    stakingConfigDetails.add(configDetail);
                    break;
                case "slashing":
                    slashingConfigDetails.add(configDetail);
                    break;
                case "block":
                    blockConfigDetails.add(configDetail);
                    break;
                case "reward":
                    rewardConfigDetails.add(configDetail);
                    break;
                case "restricting":
                    restrictingConfigDetails.add(configDetail);
                    break;
                default:
                    break;
            }

            configDetail.setName(config.getName());
            /**
             * Set upper and lower blocks for pledge and evidence
             */
            switch (config.getName()) {
                case "unStakeFreezeDuration":
                    unStakeFreezeDuration = config.getValue();
                    break;
                case "maxEvidenceAge":
                    maxEvidenceAge = config.getValue();
                    break;
                case "zeroProduceCumulativeTime":
                    zeroProduceCumulativeTime = config.getValue();
                    break;
                case "zeroProduceNumberThreshold":
                    zeroProduceNumberThreshold = config.getValue();
                    break;

                default:
                    break;
            }
        }
        /**
         * Set the pledge interval value
         */
        for (ConfigDetail stakingConfig : stakingConfigDetails) {
            if ("unStakeFreezeDuration".equalsIgnoreCase(stakingConfig.getName())) {
                stakingConfig.setStartValue(maxEvidenceAge);
                break;
            }
        }
        /**
         * Set the interval value for penalty evidence
         */
        for (ConfigDetail slashingConfig : slashingConfigDetails) {
            if ("maxEvidenceAge".equals(slashingConfig.getName())) {
                slashingConfig.setEndValue(unStakeFreezeDuration);
            }
            if ("zeroProduceCumulativeTime".equals(slashingConfig.getName())) {
                slashingConfig.setStartValue(zeroProduceNumberThreshold);
            }
            if ("zeroProduceNumberThreshold".equals(slashingConfig.getName())) {
                slashingConfig.setEndValue(zeroProduceCumulativeTime);
            }
            if ("zeroProduceFreezeDuration".equals(slashingConfig.getName())) {
                slashingConfig.setEndValue(unStakeFreezeDuration);
            }
        }
        /**
         * Set up details for different modules
         */
        stakingModuleConfig.setDetail(stakingConfigDetails);
        slashingModuleConfig.setDetail(slashingConfigDetails);
        blockModuleConfig.setDetail(blockConfigDetails);
        rewardModuleConfig.setDetail(rewardConfigDetails);
        restrictingModuleConfig.setDetail(restrictingConfigDetails);
        moduleConfigs.add(stakingModuleConfig);
        moduleConfigs.add(slashingModuleConfig);
        moduleConfigs.add(blockModuleConfig);
        moduleConfigs.add(rewardModuleConfig);
        moduleConfigs.add(restrictingModuleConfig);
        queryConfigResp.setConfig(moduleConfigs);
        return queryConfigResp;
    }

}
