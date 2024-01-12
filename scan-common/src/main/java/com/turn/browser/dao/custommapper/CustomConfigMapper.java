package com.turn.browser.dao.custommapper;

import com.turn.browser.dao.entity.Config;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CustomConfigMapper {
    /**
     * Configuration value rotation: the old value of value overwrites stale_value, and the new value in the parameter overwrites value
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void rotateConfig(@Param("list") List<Config> configList);
}