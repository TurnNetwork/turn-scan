package com.turn.browser.dao.mapper;

import com.turn.browser.dao.entity.TokenExpand;
import com.turn.browser.dao.entity.TokenExpandExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TokenExpandMapper {
    long countByExample(TokenExpandExample example);

    int deleteByExample(TokenExpandExample example);

    int deleteByPrimaryKey(String address);

    int insert(TokenExpand record);

    int insertSelective(TokenExpand record);

    List<TokenExpand> selectByExampleWithBLOBs(TokenExpandExample example);

    List<TokenExpand> selectByExample(TokenExpandExample example);

    TokenExpand selectByPrimaryKey(String address);

    int updateByExampleSelective(@Param("record") TokenExpand record, @Param("example") TokenExpandExample example);

    int updateByExampleWithBLOBs(@Param("record") TokenExpand record, @Param("example") TokenExpandExample example);

    int updateByExample(@Param("record") TokenExpand record, @Param("example") TokenExpandExample example);

    int updateByPrimaryKeySelective(TokenExpand record);

    int updateByPrimaryKeyWithBLOBs(TokenExpand record);

    int updateByPrimaryKey(TokenExpand record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table token_expand
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int batchInsert(@Param("list") List<TokenExpand> list);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table token_expand
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int batchInsertSelective(@Param("list") List<TokenExpand> list, @Param("selective") TokenExpand.Column ... selective);
}