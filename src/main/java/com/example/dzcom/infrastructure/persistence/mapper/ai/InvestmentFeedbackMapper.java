package com.example.dzcom.infrastructure.persistence.mapper.ai;

import com.example.dzcom.domain.repository.ai.InvestmentFeedbackSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.ai.InvestmentFeedbackEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 投资反馈 MyBatis Mapper。 */
@Mapper
public interface InvestmentFeedbackMapper {
    int insert(InvestmentFeedbackEntity entity);

    InvestmentFeedbackEntity selectByBizId(@Param("bizId") String bizId);

    List<InvestmentFeedbackEntity> search(
        @Param("criteria") InvestmentFeedbackSearchCriteria criteria,
        @Param("offset") int offset,
        @Param("sortColumn") String sortColumn
    );

    long count(@Param("criteria") InvestmentFeedbackSearchCriteria criteria);
}
