package com.example.dzcom.domain.model.task;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 投资主题收益、动量或资讯热度快照。 */
@Builder
public record InvestmentThemeSnapshot(
    String bizId,
    String taskCode,
    String snapshotType,
    String themeCode,
    String themeName,
    int windowMinutes,
    int sampleCount,
    BigDecimal returnRate,
    BigDecimal momentumScore,
    BigDecimal heatScore,
    String topProductBizId,
    String metrics,
    LocalDateTime snapshotTime,
    LocalDateTime createdAt
) {
}
