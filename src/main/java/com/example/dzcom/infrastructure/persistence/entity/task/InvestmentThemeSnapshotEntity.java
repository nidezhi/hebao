package com.example.dzcom.infrastructure.persistence.entity.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 投资主题快照持久化实体。 */
@TableName("aiw_investment_theme_snapshot")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InvestmentThemeSnapshotEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String taskCode;
    private String snapshotType;
    private String themeCode;
    private String themeName;
    private String marketScope;
    private int windowMinutes;
    private int sampleCount;
    private BigDecimal returnRate;
    private BigDecimal momentumScore;
    private BigDecimal heatScore;
    private String topProductBizId;
    private String metrics;
    private LocalDateTime snapshotTime;
    private LocalDateTime createdAt;
}
