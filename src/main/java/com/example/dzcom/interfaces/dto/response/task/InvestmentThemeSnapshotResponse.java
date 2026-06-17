package com.example.dzcom.interfaces.dto.response.task;

import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 投资方向收益、动量和热度快照响应。 */
@Builder
@Schema(description = "投资方向收益、动量和热度快照响应")
public record InvestmentThemeSnapshotResponse(
    @Schema(description = "快照业务 ID") String bizId,
    @Schema(description = "生成该快照的任务编码", example = "hot-theme-return") String taskCode,
    @Schema(description = "快照类型：RETURN/MOMENTUM/HEAT", example = "RETURN") String snapshotType,
    @Schema(description = "投资主题编码", example = "AI") String themeCode,
    @Schema(description = "投资主题名称", example = "AI人工智能") String themeName,
    @Schema(description = "市场范围，默认仅中国大陆", example = "CN_MAINLAND") String marketScope,
    @Schema(description = "统计窗口分钟数", example = "1440") int windowMinutes,
    @Schema(description = "样本数量", example = "12") int sampleCount,
    @Schema(description = "窗口收益率，小数形式；0.052 表示 5.2%") BigDecimal returnRate,
    @Schema(description = "动量分数") BigDecimal momentumScore,
    @Schema(description = "资讯热度分数") BigDecimal heatScore,
    @Schema(description = "窗口内表现最好的产品业务 ID") String topProductBizId,
    @Schema(description = "扩展指标 JSON 字符串") String metrics,
    @Schema(description = "快照时间") LocalDateTime snapshotTime,
    @Schema(description = "创建时间") LocalDateTime createdAt
) {
    /** 将领域对象转换为接口响应。 */
    public static InvestmentThemeSnapshotResponse from(InvestmentThemeSnapshot snapshot) {
        return InvestmentThemeSnapshotResponse.builder()
            .bizId(snapshot.bizId())
            .taskCode(snapshot.taskCode())
            .snapshotType(snapshot.snapshotType())
            .themeCode(snapshot.themeCode())
            .themeName(snapshot.themeName())
            .marketScope(snapshot.marketScope())
            .windowMinutes(snapshot.windowMinutes())
            .sampleCount(snapshot.sampleCount())
            .returnRate(snapshot.returnRate())
            .momentumScore(snapshot.momentumScore())
            .heatScore(snapshot.heatScore())
            .topProductBizId(snapshot.topProductBizId())
            .metrics(snapshot.metrics())
            .snapshotTime(snapshot.snapshotTime())
            .createdAt(snapshot.createdAt())
            .build();
    }
}
