package com.example.dzcom.domain.model.account;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 密码凭据领域对象，集中维护失败次数、锁定时间和凭据版本。 */
@Schema(description = "用户密码凭据领域对象（仅用于域/持久化层，不直接暴露给 API）")
@Builder(toBuilder = true)
public record UserCredential(
    @Schema(description = "凭据业务标识") String bizId,
    @Schema(description = "所属用户业务标识") String userBizId,
    @Schema(description = "密码哈希（不可逆）") String secretHash,
    @Schema(description = "哈希算法", example = "bcrypt") String hashAlgorithm,
    @Schema(description = "凭据版本号，用于使历史会话失效") int credentialVersion,
    @Schema(description = "连续失败次数") int failedAttempts,
    @Schema(description = "临时锁定截止时间（UTC）") LocalDateTime lockedUntil,
    @Schema(description = "密码修改时间（UTC）") LocalDateTime changedAt,
    @Schema(description = "逻辑删除标记（0/1）") int deleted
) {
    /**
     * 记录一次认证失败，并按调用方计算结果更新锁定截止时间。
     *
     * @param lockedUntil lockedUntil 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public UserCredential failed(LocalDateTime lockedUntil) {
        return toBuilder().failedAttempts(failedAttempts + 1).lockedUntil(lockedUntil).build();
    }

    /**
     * 登录成功后清零连续失败次数和临时锁定。
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public UserCredential loginSucceeded() {
        return toBuilder().failedAttempts(0).lockedUntil(null).build();
    }

    /**
     * 修改密码并递增凭据版本，使历史会话全部失效。
     *
     * @param newHash newHash 参数
     * @param now 当前业务时间
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public UserCredential changeSecret(String newHash, LocalDateTime now) {
        return toBuilder()
            .secretHash(newHash)
            .credentialVersion(credentialVersion + 1)
            .failedAttempts(0)
            .lockedUntil(null)
            .changedAt(now)
            .build();
    }

    /**
     * 判断凭据是否仍处于临时锁定窗口。
     *
     * @param now 当前业务时间
     * @return 满足条件时返回 true，否则返回 false
     * @author dz
     * @date 2026-06-14
     */
    public boolean isLocked(LocalDateTime now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }
}
