package com.example.dzcom.infrastructure.persistence.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/** 用户昵称、头像、语言、时区和扩展展示信息持久化实体。 */
@Schema(description = "用户展示资料持久化实体")
@TableName("aiw_user_profile")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfileEntity {
    /** 用户资料业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "用户资料业务唯一标识")
    private String bizId;
    /** 关联用户业务标识。 */
    @Schema(description = "关联用户业务标识")
    private String userBizId;
    /** 用户展示昵称。 */
    @Schema(description = "用户昵称")
    private String nickname;
    /** 用户头像资源地址。 */
    @Schema(description = "用户头像地址")
    private String avatarUrl;
    /** 用户界面语言和地区编码。 */
    @Schema(description = "语言地区编码", example = "zh-CN")
    private String locale;
    /** 用户首选时区。 */
    @Schema(description = "用户首选时区", example = "Asia/Shanghai")
    private String timezone;
    /** 不包含敏感信息的展示资料扩展 JSON。 */
    @Schema(description = "展示资料扩展 JSON 字符串")
    private String profileExt;
    /** 资料创建时间，北京时间。 */
    @Schema(description = "记录创建时间，北京时间")
    private LocalDateTime createdAt;
    /** 资料最后更新时间，北京时间。 */
    @Schema(description = "记录最后更新时间，北京时间")
    private LocalDateTime updatedAt;
    /** 逻辑删除标记。 */
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标记：0-有效，1-删除")
    private int deleted;
}
