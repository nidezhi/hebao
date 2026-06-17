package com.example.dzcom.infrastructure.persistence.entity.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 投资分析报告持久化实体。 */
@TableName("aiw_investment_analysis_report")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InvestmentAnalysisReportEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String requestId;
    private String providerCode;
    private String modelCode;
    private String marketScope;
    private String themeCode;
    private String themeName;
    private String status;
    private String investmentSummary;
    private String trend;
    private String investmentPlan;
    private String simulatedReturn;
    private String chartPayload;
    private String promptSnapshot;
    private String failureReason;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;
}
