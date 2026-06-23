package com.example.dzcom.infrastructure.persistence.mapper.ai;

import com.example.dzcom.domain.repository.ai.BacktestResultSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.ai.BacktestResultEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 回测结果 MyBatis Mapper。 */
@Mapper
public interface BacktestResultMapper {
    int save(BacktestResultEntity entity);

    BacktestResultEntity selectByBizId(@Param("bizId") String bizId);

    List<BacktestResultEntity> search(
        @Param("criteria") BacktestResultSearchCriteria criteria,
        @Param("offset") int offset,
        @Param("sortColumn") String sortColumn
    );

    long count(@Param("criteria") BacktestResultSearchCriteria criteria);
}
