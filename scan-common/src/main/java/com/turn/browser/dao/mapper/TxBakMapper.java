package com.turn.browser.dao.mapper;

import com.turn.browser.dao.entity.TxBak;
import com.turn.browser.dao.entity.TxBakExample;
import com.turn.browser.dao.entity.TxBakWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TxBakMapper {
    long countByExample(TxBakExample example);

    int deleteByExample(TxBakExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TxBakWithBLOBs record);

    int insertSelective(TxBakWithBLOBs record);

    List<TxBakWithBLOBs> selectByExampleWithBLOBs(TxBakExample example);

    List<TxBak> selectByExample(TxBakExample example);

    TxBakWithBLOBs selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TxBakWithBLOBs record, @Param("example") TxBakExample example);

    int updateByExampleWithBLOBs(@Param("record") TxBakWithBLOBs record, @Param("example") TxBakExample example);

    int updateByExample(@Param("record") TxBak record, @Param("example") TxBakExample example);

    int updateByPrimaryKeySelective(TxBakWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(TxBakWithBLOBs record);

    int updateByPrimaryKey(TxBak record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tx_bak
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int batchInsert(@Param("list") List<TxBakWithBLOBs> list);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tx_bak
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int batchInsertSelective(@Param("list") List<TxBakWithBLOBs> list, @Param("selective") TxBakWithBLOBs.Column ... selective);
}