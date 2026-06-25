package com.example.dzcom.infrastructure.persistence.mapper.ai;

import com.example.dzcom.domain.repository.ai.InvestmentAnalysisReportSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.ai.InvestmentAnalysisReportEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 投资分析报告 MyBatis Mapper。 */
@Mapper
public interface InvestmentAnalysisReportMapper {
    /** 保存投资分析报告。 */
    int insert(InvestmentAnalysisReportEntity entity);

    /** 根据业务标识查询投资分析报告。 */
    InvestmentAnalysisReportEntity selectByBizId(@Param("bizId") String bizId);

    /** 分页查询投资分析报告。 */
    List<InvestmentAnalysisReportEntity> search(@Param("criteria") InvestmentAnalysisReportSearchCriteria criteria,
                                                @Param("offset") int offset,
                                                @Param("sortColumn") String sortColumn);

    /** 统计投资分析报告数量。 */
    long count(@Param("criteria") InvestmentAnalysisReportSearchCriteria criteria);

    /** 查询最近生成的投资分析报告。 */
    List<InvestmentAnalysisReportEntity> selectLatest(@Param("size") int size);
}
