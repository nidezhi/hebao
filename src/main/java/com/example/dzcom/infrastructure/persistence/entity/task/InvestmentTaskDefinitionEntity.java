package com.example.dzcom.infrastructure.persistence.entity.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 投资定时任务定义持久化实体。
 *
 * <p>对应 {@code aiw_investment_task_definition}，是运行时动态调度的配置来源。</p>
 */
@Schema(description = "投资定时任务定义持久化实体")
@TableName("aiw_investment_task_definition")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InvestmentTaskDefinitionEntity {
    /** 任务配置业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "任务配置业务唯一标识")
    private String bizId;
    /** 供接口触发和执行记录引用的稳定任务编码。 */
    @Schema(description = "稳定任务编码", example = "hot-theme-return")
    private String taskCode;
    /** 用于匹配唯一任务处理器的类型编码。 */
    @Schema(description = "任务处理器类型", example = "HOT_THEME_RETURN")
    private String taskType;
    /** Spring 六段 Cron 表达式。 */
    @Schema(description = "Spring Cron 表达式", example = "30 */5 * * * *")
    private String cron;
    /** Cron 计算时区。 */
    @Schema(description = "Cron 时区", example = "Asia/Shanghai")
    private String zone;
    /** 是否参与动态调度注册。 */
    @Schema(description = "是否启用动态调度", example = "true")
    private boolean enabled;
    /** 由具体任务处理器解释的参数 JSON。 */
    @Schema(description = "任务参数 JSON 字符串")
    private String parameters;
    /** 面向管理端的任务用途说明。 */
    @Schema(description = "任务配置说明")
    private String description;
    /** 配置创建时间，北京时间。 */
    @Schema(description = "配置创建时间，北京时间")
    private LocalDateTime createdAt;
    /** 配置最后更新时间，北京时间。 */
    @Schema(description = "配置最后更新时间，北京时间")
    private LocalDateTime updatedAt;
}
