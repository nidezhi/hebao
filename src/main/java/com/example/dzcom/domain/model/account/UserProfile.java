package com.example.dzcom.domain.model.account;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

/** 用户展示资料领域对象。 */
@Schema(description = "用户展示资料领域对象")
@Builder(toBuilder = true)
public record UserProfile(
    @Schema(description = "资料业务标识") String bizId,
    @Schema(description = "所属用户业务标识") String userBizId,
    @Schema(description = "昵称") String nickname,
    @Schema(description = "头像 URL") String avatarUrl,
    @Schema(description = "语言/区域设置", example = "zh_CN") String locale,
    @Schema(description = "时区", example = "Asia/Shanghai") String timezone,
    @Schema(description = "逻辑删除标记（0/1）") int deleted
) {
}
