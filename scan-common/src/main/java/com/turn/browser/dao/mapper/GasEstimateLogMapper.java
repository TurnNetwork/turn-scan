package com.turn.browser.dao.mapper;

import com.turn.browser.dao.entity.GasEstimateLog;
import com.turn.browser.dao.entity.GasEstimateLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface GasEstimateLogMapper {
    long countByExample(GasEstimateLogExample example);

    int deleteByExample(GasEstimateLogExample example);

    int deleteByPrimaryKey(Long seq);

    int insert(GasEstimateLog record);

    int insertSelective(GasEstimateLog record);

    List<GasEstimateLog> selectByExampleWithBLOBs(GasEstimateLogExample example);

    List<GasEstimateLog> selectByExample(GasEstimateLogExample example);

    GasEstimateLog selectByPrimaryKey(Long seq);

    int updateByExampleSelective(@Param("record") GasEstimateLog record, @Param("example") GasEstimateLogExample example);

    int updateByExampleWithBLOBs(@Param("record") GasEstimateLog record, @Param("example") GasEstimateLogExample example);

    int updateByExample(@Param("record") GasEstimateLog record, @Param("example") GasEstimateLogExample example);

    int updateByPrimaryKeySelective(GasEstimateLog record);

    int updateByPrimaryKeyWithBLOBs(GasEstimateLog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table gas_estimate_log
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int batchInsert(@Param("list") List<GasEstimateLog> list);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table gas_estimate_log
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int batchInsertSelective(@Param("list") List<GasEstimateLog> list, @Param("selective") GasEstimateLog.Column ... selective);
}