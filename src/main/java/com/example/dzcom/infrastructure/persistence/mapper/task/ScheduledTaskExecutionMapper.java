package com.example.dzcom.infrastructure.persistence.mapper.task;

import com.example.dzcom.domain.repository.task.ScheduledTaskExecutionSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.task.ScheduledTaskExecutionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 定时任务执行记录 MyBatis Mapper。 */
@Mapper
public interface ScheduledTaskExecutionMapper {
    /** 根据 Kafka 事件 ID 查询任务执行记录。 */
    ScheduledTaskExecutionEntity selectByEventId(@Param("eventId") String eventId);

    /** 新增或更新任务执行记录。 */
    int save(ScheduledTaskExecutionEntity entity);

    /** 根据筛选条件分页查询任务执行记录。 */
    List<ScheduledTaskExecutionEntity> search(@Param("criteria") ScheduledTaskExecutionSearchCriteria criteria,
                                              @Param("offset") int offset,
                                              @Param("sortColumn") String sortColumn);

    /** 统计符合筛选条件的任务执行记录数量。 */
    long count(@Param("criteria") ScheduledTaskExecutionSearchCriteria criteria);
}
