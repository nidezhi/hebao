package com.example.dzcom.infrastructure.persistence.mapper.portfolio;

import com.example.dzcom.domain.repository.portfolio.PortfolioSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.portfolio.PortfolioEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 投资组合 MyBatis Mapper。 */
@Mapper
public interface PortfolioMapper {
    /** 新增或更新投资组合。 */
    int save(PortfolioEntity entity);

    /** 根据业务标识查询未删除组合。 */
    PortfolioEntity selectActiveByBizId(@Param("bizId") String bizId);

    /** 根据条件分页查询组合。 */
    List<PortfolioEntity> search(
        @Param("criteria") PortfolioSearchCriteria criteria,
        @Param("offset") int offset,
        @Param("sortColumn") String sortColumn
    );

    /** 统计符合条件的组合数量。 */
    long count(@Param("criteria") PortfolioSearchCriteria criteria);
}
