package com.example.dzcom.domain.repository.market;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.market.DataSource;
import com.example.dzcom.domain.model.market.DataSourceHealth;
import com.example.dzcom.domain.model.market.DataQualitySnapshot;

import java.util.List;
import java.util.Optional;

/** 数据源治理仓储端口。 */
public interface DataSourceStore {
    /**
     * 新增或更新数据源注册信息。
     *
     * @param source 数据源注册信息
     * @return 保存后的数据源
     * @author dz
     * @date 2026-06-23
     */
    DataSource save(DataSource source);

    /**
     * 保存数据源健康状态。
     *
     * @param health 数据源健康状态
     * @return 保存后的健康状态
     * @author dz
     * @date 2026-06-23
     */
    DataSourceHealth saveHealth(DataSourceHealth health);

    /**
     * 保存数据质量快照。
     *
     * @param snapshot 数据质量快照
     * @return 保存后的数据质量快照
     * @author dz
     * @date 2026-06-23
     */
    DataQualitySnapshot saveQualitySnapshot(DataQualitySnapshot snapshot);

    /**
     * 按数据源编码查询数据源。
     *
     * @param sourceCode 数据源稳定编码
     * @return 数据源注册信息
     * @author dz
     * @date 2026-06-23
     */
    Optional<DataSource> findBySourceCode(String sourceCode);

    /**
     * 按数据源编码查询健康状态。
     *
     * @param sourceCode 数据源稳定编码
     * @return 数据源健康状态
     * @author dz
     * @date 2026-06-23
     */
    Optional<DataSourceHealth> findHealthBySourceCode(String sourceCode);

    /**
     * 查询指定数据源的质量快照。
     *
     * @param sourceCode 数据源稳定编码
     * @param dataType 数据类型，可为空
     * @param limit 返回数量上限
     * @return 按快照时间倒序排列的质量快照
     * @author dz
     * @date 2026-06-23
     */
    List<DataQualitySnapshot> findQualitySnapshots(String sourceCode, String dataType, int limit);

    /**
     * 分页查询数据源注册信息。
     *
     * @param criteria 查询条件
     * @return 数据源分页结果
     * @author dz
     * @date 2026-06-23
     */
    PageResult<DataSource> search(DataSourceSearchCriteria criteria);
}
