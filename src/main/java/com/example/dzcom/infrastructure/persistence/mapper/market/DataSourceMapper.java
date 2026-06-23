package com.example.dzcom.infrastructure.persistence.mapper.market;

import com.example.dzcom.domain.repository.market.DataSourceSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.market.DataSourceEntity;
import com.example.dzcom.infrastructure.persistence.entity.market.DataSourceHealthEntity;
import com.example.dzcom.infrastructure.persistence.entity.market.DataQualitySnapshotEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 数据源治理 MyBatis Mapper。 */
@Mapper
public interface DataSourceMapper {
    /** 新增或更新数据源注册信息。 */
    int save(DataSourceEntity entity);

    /** 新增或更新数据源健康状态。 */
    int saveHealth(DataSourceHealthEntity entity);

    /** 新增数据质量快照。 */
    int insertQualitySnapshot(DataQualitySnapshotEntity entity);

    /** 按数据源编码查询数据源。 */
    DataSourceEntity selectBySourceCode(@Param("sourceCode") String sourceCode);

    /** 按数据源编码查询健康状态。 */
    DataSourceHealthEntity selectHealthBySourceCode(@Param("sourceCode") String sourceCode);

    /** 查询数据质量快照。 */
    List<DataQualitySnapshotEntity> selectQualitySnapshots(
        @Param("sourceCode") String sourceCode,
        @Param("dataType") String dataType,
        @Param("limit") int limit
    );

    /** 分页查询数据源。 */
    List<DataSourceEntity> search(
        @Param("criteria") DataSourceSearchCriteria criteria,
        @Param("offset") int offset,
        @Param("sortColumn") String sortColumn
    );

    /** 统计数据源数量。 */
    long count(@Param("criteria") DataSourceSearchCriteria criteria);
}
