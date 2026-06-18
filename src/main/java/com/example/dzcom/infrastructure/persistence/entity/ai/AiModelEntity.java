package com.example.dzcom.infrastructure.persistence.entity.ai;

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
 * AI 模型及版本注册持久化实体。
 *
 * <p>对应数据库表 {@code aiw_ai_model}，保存模型身份、提供方、脱敏配置、
 * 评估指标和生命周期状态。模型编码与版本共同构成业务唯一版本。</p>
 */
@Schema(description = "AI 模型及版本注册持久化实体")
@TableName("aiw_ai_model")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiModelEntity {
    /** 对外使用的模型业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "模型业务唯一标识")
    private String bizId;
    /** 跨版本稳定的模型编码。 */
    @Schema(description = "模型稳定编码", example = "investment-analysis")
    private String modelCode;
    /** 当前模型版本号。 */
    @Schema(description = "模型版本号", example = "v1")
    private String modelVersion;
    /** 面向管理端展示的模型名称。 */
    @Schema(description = "模型名称", example = "中国大陆投资分析模型")
    private String modelName;
    /** 模型业务类型。 */
    @Schema(description = "模型类型：SIGNAL/RISK/RECOMMENDATION/NLP/ANALYSIS", example = "ANALYSIS")
    private String modelType;
    /** 模型服务提供方或运行平台编码。 */
    @Schema(description = "模型提供方或运行平台", example = "LOCAL_RULE")
    private String provider;
    /** 模型制品、提示词或外部配置位置。 */
    @Schema(description = "模型制品、提示词或配置地址")
    private String artifactUri;
    /** 不包含密钥和敏感信息的模型配置 JSON。 */
    @Schema(description = "脱敏后的模型配置 JSON 字符串")
    private String modelConfig;
    /** 模型离线验证和评估指标 JSON。 */
    @Schema(description = "模型离线评估指标 JSON 字符串")
    private String metrics;
    /** 模型生命周期状态。 */
    @Schema(description = "生命周期状态：DRAFT/VALIDATING/ACTIVE/INACTIVE/ARCHIVED", example = "ACTIVE")
    private String status;
    /** 模型首次正式启用的北京时间。 */
    @Schema(description = "模型首次正式启用时间，北京时间")
    private LocalDateTime activatedAt;
    /** 模型停用或归档的北京时间。 */
    @Schema(description = "模型停用或归档时间，北京时间")
    private LocalDateTime retiredAt;
    /** 数据创建时间，北京时间。 */
    @Schema(description = "记录创建时间，北京时间")
    private LocalDateTime createdAt;
    /** 数据最后更新时间，北京时间。 */
    @Schema(description = "记录最后更新时间，北京时间")
    private LocalDateTime updatedAt;
}
