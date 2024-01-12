package com.turn.browser.dao.mapper;

import com.turn.browser.dao.entity.BlockNode;
import com.turn.browser.dao.entity.BlockNodeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BlockNodeMapper {
    long countByExample(BlockNodeExample example);

    int deleteByExample(BlockNodeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BlockNode record);

    int insertSelective(BlockNode record);

    List<BlockNode> selectByExample(BlockNodeExample example);

    BlockNode selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BlockNode record, @Param("example") BlockNodeExample example);

    int updateByExample(@Param("record") BlockNode record, @Param("example") BlockNodeExample example);

    int updateByPrimaryKeySelective(BlockNode record);

    int updateByPrimaryKey(BlockNode record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table block_node
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int batchInsert(@Param("list") List<BlockNode> list);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table block_node
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int batchInsertSelective(@Param("list") List<BlockNode> list, @Param("selective") BlockNode.Column ... selective);
}