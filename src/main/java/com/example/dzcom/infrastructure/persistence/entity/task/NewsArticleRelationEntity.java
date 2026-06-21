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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 资讯、投资主题和产品显式关联持久化实体。 */
@Schema(description = "资讯、投资主题和产品显式关联持久化实体")
@TableName("aiw_news_article_relation")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsArticleRelationEntity {
    /** 关联记录业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "关联记录业务唯一标识")
    private String bizId;
    /** 资讯业务唯一标识。 */
    @Schema(description = "资讯业务唯一标识")
    private String articleBizId;
    /** 投资主题稳定编码。 */
    @Schema(description = "投资主题稳定编码")
    private String themeCode;
    /** 投资主题展示名称。 */
    @Schema(description = "投资主题展示名称")
    private String themeName;
    /** 关联产品代码。 */
    @Schema(description = "关联产品代码")
    private String productCode;
    /** 关联类型。 */
    @Schema(description = "关联类型：KEYWORD_MATCH/MANUAL/MODEL_EXTRACTED")
    private String relationType;
    /** 命中的关键词 JSON。 */
    @Schema(description = "命中的关键词 JSON")
    private String matchedKeywords;
    /** 数据源质量分。 */
    @Schema(description = "数据源质量分")
    private BigDecimal sourceQualityScore;
    /** 综合关联分。 */
    @Schema(description = "综合关联分")
    private BigDecimal relationScore;
    /** 关联证据摘要。 */
    @Schema(description = "关联证据摘要")
    private String evidence;
    /** 记录创建时间，北京时间。 */
    @Schema(description = "记录创建时间，北京时间")
    private LocalDateTime createdAt;
}
