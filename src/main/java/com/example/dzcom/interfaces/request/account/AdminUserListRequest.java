package com.example.dzcom.interfaces.request.account;

import com.example.dzcom.domain.enums.account.AccountStatus;
import com.example.dzcom.domain.enums.account.KycStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 管理端用户分页查询请求，筛选条件和分页参数统一从请求体接收。
 */
@Schema(description = "管理端用户分页查询请求，支持关键字、状态、KYC、风险等级与分页排序")
public record AdminUserListRequest(
    @Schema(description = "关键字（用户名/邮箱/昵称等模糊匹配）") String keyword,
    @Schema(description = "账户状态过滤") AccountStatus status,
    @Schema(description = "KYC 状态过滤") KycStatus kycStatus,
    @Schema(description = "风险等级过滤（1-5）") @Min(1) @Max(5) Integer riskLevel,
    @Schema(description = "页码（支持 0 用于前端组件兼容，会在后端转换为 1）") @Min(0) Integer page,
    @Schema(description = "每页大小（1-100）") @Min(1) @Max(100) Integer size,
    @Schema(description = "排序字段，后端将检查白名单") String sort,
    @Schema(description = "排序方向，asc 或 desc") String direction
) {
}
