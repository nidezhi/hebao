package com.example.dzcom.infrastructure.persistence.mapper.task;

import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.task.InvestmentThemeSnapshotEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 投资主题快照 MyBatis Mapper。 */
@Mapper
public interface InvestmentThemeSnapshotMapper {
    /** 新增主题快照。 */
    int insert(InvestmentThemeSnapshotEntity entity);

    /** 根据筛选条件分页查询主题快照。 */
    List<InvestmentThemeSnapshotEntity> search(
        @Param("criteria") InvestmentThemeSnapshotSearchCriteria criteria,
        @Param("offset") int offset,
        @Param("sortColumn") String sortColumn
    );

    /** 统计符合筛选条件的主题快照数量。 */
    long count(@Param("criteria") InvestmentThemeSnapshotSearchCriteria criteria);
}
