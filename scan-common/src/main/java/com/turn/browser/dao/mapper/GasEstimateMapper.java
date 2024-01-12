package com.turn.browser.dao.mapper;

import com.turn.browser.dao.entity.GasEstimate;
import com.turn.browser.dao.entity.GasEstimateExample;
import com.turn.browser.dao.entity.GasEstimateKey;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
public interface GasEstimateMapper {
    long countByExample(GasEstimateExample example);

    int deleteByExample(GasEstimateExample example);

    int deleteByPrimaryKey(GasEstimateKey key);

    int insert(GasEstimate record);

    int insertSelective(GasEstimate record);

    List<GasEstimate> selectByExample(GasEstimateExample example);

    GasEstimate selectByPrimaryKey(GasEstimateKey key);

    int updateByExampleSelective(@Param("record") GasEstimate record, @Param("example") GasEstimateExample example);

    int updateByExample(@Param("record") GasEstimate record, @Param("example") GasEstimateExample example);

    int updateByPrimaryKeySelective(GasEstimate record);

    int updateByPrimaryKey(GasEstimate record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table gas_estimate
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int batchInsert(@Param("list") List<GasEstimate> list);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table gas_estimate
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int batchInsertSelective(@Param("list") List<GasEstimate> list, @Param("selective") GasEstimate.Column ... selective);
}