package com.example.dzcom.infrastructure.persistence.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;


/**
 * 用户主体持久化实体。
 *
 * <p>该表只承载稳定的用户身份、生命周期状态和审计时间，不保存用户名、
 * 邮箱、手机号、密码或风险画像。其他账户数据通过 {@code bizId} 进行逻辑关联，
 * 不配置持久化对象关系和数据库外键。</p>
 */
@Schema(description = "持久化：用户主体表实体")
@TableName("aiw_user")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserEntity {
    /** 对外和跨模块使用的用户业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "业务唯一标识")
    private String bizId;

    /** 面向运营、客服和用户展示的稳定用户编号，创建后不可修改。 */
    @Schema(description = "平台用户编号")
    private String userNo;

    /** 账户状态编码：0-禁用，1-正常，2-锁定。 */
    @Schema(description = "账户状态编码")
    private int status;

    /** 乐观锁版本，防止并发更新覆盖。 */
    @Schema(description = "乐观锁版本")
    private int version;

    /** 用户完成注册的 UTC 时间。 */
    @Schema(description = "注册时间（UTC）")
    private LocalDateTime registeredAt;

    /** 最近一次成功登录的 UTC 时间。 */
    @Schema(description = "最近登录时间（UTC）")
    private LocalDateTime lastLoginAt;

    /** 记录创建时间。 */
    @Schema(description = "记录创建时间（UTC）")
    private LocalDateTime createdAt;

    /** 记录最后更新时间。 */
    @Schema(description = "记录最后更新时间（UTC）")
    private LocalDateTime updatedAt;

    /** 创建该用户的操作者业务标识或系统标识。 */
    @Schema(description = "创建者标识")
    private String createdBy;

    /** 最后修改该用户的操作者业务标识或系统标识。 */
    @Schema(description = "最后修改者标识")
    private String updatedBy;

    /** 逻辑删除标记，删除后常规查询必须排除。 */
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标记（0/1）")
    private int deleted;

    /** 执行逻辑删除的 UTC 时间。 */
    @Schema(description = "删除时间（UTC）")
    private LocalDateTime deletedAt;
}
