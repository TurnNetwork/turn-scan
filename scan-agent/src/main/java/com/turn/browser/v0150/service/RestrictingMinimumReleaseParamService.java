package com.turn.browser.v0150.service;

import com.bubble.contracts.dpos.dto.resp.GovernParam;
import com.turn.browser.client.TurnClient;
import com.turn.browser.dao.entity.Config;
import com.turn.browser.dao.entity.ConfigExample;
import com.turn.browser.dao.mapper.ConfigMapper;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.enums.ModifiableGovernParamEnum;
import com.turn.browser.v0150.V0150Config;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * 治理参数调整（添加、中途调整）服务
 */
@Service
public class RestrictingMinimumReleaseParamService {

    //Alaya version special processing version upgrade, call the getActiveVersion() method of proposalContract during the settlement cycle to see if the return value is consistent with the value of this field.
    // If consistent, check whether there is a lock minimum release amount configuration in the database. If it does not exist, insert this data.
    @Resource
    private V0150Config v0150Config;
    @Resource
    private ConfigMapper configMapper;
    @Resource
    private TurnClient turnClient;

    public void checkRestrictingMinimumReleaseParam(Block block) throws Exception {
        ConfigExample example = new ConfigExample();
        String moduleName = ModifiableGovernParamEnum.RESTRICTING_MINIMUM_RELEASE.getModule();
        String paramName = ModifiableGovernParamEnum.RESTRICTING_MINIMUM_RELEASE.getName();
        example.createCriteria().andNameEqualTo(paramName);
        List<Config> configs = configMapper.selectByExample(example);

        if(configs.isEmpty()){
            // Check that the effective version on the chain is greater than or equal to the version specified in the configuration file, then insert the minimum lock release amount parameter
            BigInteger chainVersion = turnClient.getProposalContract().getActiveVersion().send().getData();
            if(chainVersion.compareTo(v0150Config.getRestrictingMinimumReleaseActiveVersion())>=0) return;

            // If minimumRelease does not exist, query the parameter insertion of the specified module from the chain.
            String restrictingMinimumRelease="80000000000000000000";
            String restrictingMinimumReleaseDesc = "minimum restricting amount to be released in each epoch, range: [80000000000000000000, 100000000000000000000000]";
            List<GovernParam> governParamList = turnClient
                    .getProposalContract()
                    .getParamList(moduleName)
                    .send()
                    .getData();
            if(governParamList!=null&&!governParamList.isEmpty()){
                for (GovernParam e : governParamList) {
                    if(
                            ModifiableGovernParamEnum.RESTRICTING_MINIMUM_RELEASE.getModule().equals(e.getParamItem().getModule())&&
                                    ModifiableGovernParamEnum.RESTRICTING_MINIMUM_RELEASE.getName().equals(e.getParamItem().getName())
                    ){
                        restrictingMinimumRelease = e.getParamValue().getValue();
                        restrictingMinimumReleaseDesc = e.getParamItem().getDesc();
                    }
                }
            }
            Config config = new Config();
            config.setActiveBlock(block.getNum());
            config.setModule(moduleName);
            config.setName(paramName);
            config.setInitValue(restrictingMinimumRelease);
            config.setStaleValue(restrictingMinimumRelease);
            config.setValue(restrictingMinimumRelease);
            config.setRangeDesc(restrictingMinimumReleaseDesc);
            Date date = new Date();
            config.setCreateTime(date);
            config.setUpdateTime(date);
            configMapper.insert(config);
        }
    }
}
