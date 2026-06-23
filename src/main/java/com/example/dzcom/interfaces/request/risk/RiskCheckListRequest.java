package com.example.dzcom.interfaces.request.risk;

import io.swagger.v3.oas.annotations.media.Schema;

/** 风险检查分页查询请求。 */
@Schema(description = "风险检查分页查询请求")
public record RiskCheckListRequest(
    @Schema(description = "业务类型：ORDER/REPORT/PORTFOLIO")
    String businessType,
    @Schema(description = "业务对象业务标识")
    String businessBizId,
    @Schema(description = "关联用户业务标识")
    String userBizId,
    @Schema(description = "检查结论：PASS/REVIEW/REJECT/ERROR")
    String checkResult,
    @Schema(description = "风险等级：LOW/MEDIUM/HIGH/CRITICAL")
    String riskLevel,
    @Schema(description = "原因编码")
    String reasonCode,
    @Schema(description = "页码，从 1 开始；传 0 会兼容为第一页", example = "1")
    Integer page,
    @Schema(description = "每页条数，1-100", example = "20")
    Integer size,
    @Schema(description = "排序字段：checkedAt/businessType/checkResult/riskLevel/reasonCode", example = "checkedAt")
    String sort,
    @Schema(description = "排序方向：asc/desc", example = "desc")
    String direction
) {
}
