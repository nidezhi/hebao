package com.example.dzcom.infrastructure.persistence.mapper.risk;

import com.example.dzcom.domain.repository.risk.RiskCheckSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.risk.RiskCheckEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 风险检查 MyBatis Mapper。 */
@Mapper
public interface RiskCheckMapper {
    int insert(RiskCheckEntity entity);

    List<RiskCheckEntity> search(
        @Param("criteria") RiskCheckSearchCriteria criteria,
        @Param("offset") int offset,
        @Param("sortColumn") String sortColumn
    );

    long count(@Param("criteria") RiskCheckSearchCriteria criteria);
}
