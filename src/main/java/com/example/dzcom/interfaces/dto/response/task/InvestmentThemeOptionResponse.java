package com.example.dzcom.interfaces.dto.response.task;

import com.example.dzcom.domain.model.task.InvestmentThemeOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 投资主题选择器响应。 */
@Builder
@Schema(description = "投资主题选择器响应")
public record InvestmentThemeOptionResponse(
    @Schema(description = "投资主题稳定编码") String themeCode,
    @Schema(description = "投资主题展示名称") String themeName,
    @Schema(description = "前端选择器展示名称") String displayName,
    @Schema(description = "市场范围") String marketScope,
    @Schema(description = "摘要信息") String summary,
    @Schema(description = "最近快照类型") String latestSnapshotType,
    @Schema(description = "最近快照时间") LocalDateTime latestSnapshotTime,
    @Schema(description = "最近样本数量") int sampleCount,
    @Schema(description = "最近收益率") BigDecimal returnRate,
    @Schema(description = "最近动量分数") BigDecimal momentumScore,
    @Schema(description = "最近热度分数") BigDecimal heatScore
) {
    /** 将领域投影转换为接口响应。 */
    public static InvestmentThemeOptionResponse from(InvestmentThemeOption option) {
        String displayName = option.themeName() == null || option.themeName().isBlank()
            ? option.themeCode()
            : option.themeName();
        String summary = option.latestSnapshotTime() == null
            ? option.marketScope()
            : option.marketScope() + " · " + option.latestSnapshotType() + " · " + option.latestSnapshotTime();
        return InvestmentThemeOptionResponse.builder()
            .themeCode(option.themeCode())
            .themeName(option.themeName())
            .displayName(displayName)
            .marketScope(option.marketScope())
            .summary(summary)
            .latestSnapshotType(option.latestSnapshotType())
            .latestSnapshotTime(option.latestSnapshotTime())
            .sampleCount(option.sampleCount())
            .returnRate(option.returnRate())
            .momentumScore(option.momentumScore())
            .heatScore(option.heatScore())
            .build();
    }
}
