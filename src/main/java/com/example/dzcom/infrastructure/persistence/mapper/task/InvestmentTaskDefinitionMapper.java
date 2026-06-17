package com.example.dzcom.infrastructure.persistence.mapper.task;

import com.example.dzcom.infrastructure.persistence.entity.task.InvestmentTaskDefinitionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 投资定时任务定义 MyBatis Mapper。 */
@Mapper
public interface InvestmentTaskDefinitionMapper {
    /** 查询全部任务定义。 */
    List<InvestmentTaskDefinitionEntity> selectAll();

    /** 根据任务编码查询任务定义。 */
    InvestmentTaskDefinitionEntity selectByCode(@Param("taskCode") String taskCode);

    /** 新增或更新任务定义。 */
    int save(InvestmentTaskDefinitionEntity entity);
}
