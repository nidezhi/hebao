package com.example.dzcom.infrastructure.persistence.mapper.task;

import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.task.InvestmentThemeSnapshotEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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

    /** 查询去重后的投资主题选项。 */
    List<Map<String, Object>> searchThemeOptions(
        @Param("keyword") String keyword,
        @Param("marketScope") String marketScope,
        @Param("offset") int offset,
        @Param("size") int size
    );

    /** 统计去重后的投资主题选项数量。 */
    long countThemeOptions(@Param("keyword") String keyword, @Param("marketScope") String marketScope);
}
