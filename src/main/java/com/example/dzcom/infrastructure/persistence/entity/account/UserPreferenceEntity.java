package com.example.dzcom.infrastructure.persistence.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "持久化：用户偏好表（aiw_user_preference）")
@TableName("aiw_user_preference")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserPreferenceEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "业务唯一标识")
    private String bizId;
    @Schema(description = "所属用户业务标识")
    private String userBizId;
    @Schema(description = "偏好键")
    private String preferenceKey;
    @Schema(description = "值类型")
    private String valueType;
    @Schema(description = "偏好值（JSON 文本）")
    private String preferenceValue;
    @Schema(description = "创建时间（北京时间）")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间（北京时间）")
    private LocalDateTime updatedAt;
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标记（0/1）")
    private int deleted;
}
