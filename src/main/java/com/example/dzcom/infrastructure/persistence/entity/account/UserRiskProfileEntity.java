package com.example.dzcom.infrastructure.persistence.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/** 用户 KYC 状态、投资风险承受等级和测评信息持久化实体。 */
@Schema(description = "用户 KYC 与投资风险画像持久化实体")
@TableName("aiw_user_risk_profile")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserRiskProfileEntity {
    /** 风险画像业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "风险画像业务唯一标识")
    private String bizId;
    /** 关联用户业务标识。 */
    @Schema(description = "关联用户业务标识")
    private String userBizId;
    /** KYC 状态数据库编码。 */
    @Schema(description = "KYC 状态数据库编码")
    private int kycStatus;
    /** 用户风险等级，范围 1-5。 */
    @Schema(description = "风险承受等级，范围 1-5", example = "3")
    private int riskLevel;
    /** 风险测评问卷或算法版本。 */
    @Schema(description = "风险测评版本")
    private String assessmentVersion;
    /** 最近一次风险测评时间，北京时间。 */
    @Schema(description = "最近风险测评时间，北京时间")
    private LocalDateTime assessedAt;
    /** KYC 最近审核时间，北京时间。 */
    @Schema(description = "KYC 最近审核时间，北京时间")
    private LocalDateTime kycReviewedAt;
    /** 风险画像扩展 JSON。 */
    @Schema(description = "风险画像扩展 JSON 字符串")
    private String extData;
    /** 画像创建时间，北京时间。 */
    @Schema(description = "记录创建时间，北京时间")
    private LocalDateTime createdAt;
    /** 画像最后更新时间，北京时间。 */
    @Schema(description = "记录最后更新时间，北京时间")
    private LocalDateTime updatedAt;
    /** 逻辑删除标记。 */
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标记：0-有效，1-删除")
    private int deleted;
}
