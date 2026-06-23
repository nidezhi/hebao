package com.example.dzcom.interfaces.controller.risk;

import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.risk.RiskAuditApplicationService;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.dto.response.risk.RiskCheckResponse;
import com.example.dzcom.interfaces.request.risk.RiskCheckListRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 风控审计查询接口。 */
@RestController
@RequestMapping("/api/risk/checks")
@RequiredArgsConstructor
@Tag(name = "风控审计", description = "风险检查结果、拦截原因和前端审计查询接口")
public class RiskAuditController {
    private final RiskAuditApplicationService riskAudits;

    /**
     * 分页查询风险检查记录。
     *
     * @param request 风险检查查询请求
     * @return 风险检查分页响应
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询风险检查", description = "按业务类型、业务对象、用户、结论、风险等级和原因编码查询风控拦截记录。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回风险检查分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<RiskCheckResponse>> list(@Valid @RequestBody RiskCheckListRequest request) {
        return Result.success(PageResponse.from(riskAudits.list(
            request.businessType(),
            request.businessBizId(),
            request.userBizId(),
            request.checkResult(),
            request.riskLevel(),
            request.reasonCode(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "checkedAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), RiskCheckResponse::from));
    }
}
