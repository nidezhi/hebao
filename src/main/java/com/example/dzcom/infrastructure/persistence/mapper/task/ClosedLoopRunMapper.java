package com.example.dzcom.infrastructure.persistence.mapper.task;

import com.example.dzcom.domain.repository.task.ClosedLoopRunSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.task.ClosedLoopRunEntity;
import com.example.dzcom.infrastructure.persistence.entity.task.ClosedLoopStepEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 自动投资闭环运行 MyBatis Mapper。 */
@Mapper
public interface ClosedLoopRunMapper {
    /** 保存或更新运行记录。 */
    int saveRun(ClosedLoopRunEntity entity);

    /** 保存或更新步骤记录。 */
    int saveStep(ClosedLoopStepEntity entity);

    /** 根据业务 ID 查询运行记录。 */
    ClosedLoopRunEntity selectRunByBizId(@Param("bizId") String bizId);

    /** 查询运行步骤。 */
    List<ClosedLoopStepEntity> selectStepsByRunBizId(@Param("runBizId") String runBizId);

    /** 分页查询运行记录。 */
    List<ClosedLoopRunEntity> searchRuns(
        @Param("criteria") ClosedLoopRunSearchCriteria criteria,
        @Param("offset") int offset,
        @Param("sortColumn") String sortColumn
    );

    /** 统计运行记录。 */
    long countRuns(@Param("criteria") ClosedLoopRunSearchCriteria criteria);
}
