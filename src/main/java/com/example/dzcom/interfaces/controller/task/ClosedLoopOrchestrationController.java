package com.example.dzcom.interfaces.controller.task;

import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.task.ClosedLoopOrchestrationApplicationService;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.dto.response.task.ClosedLoopRunResponse;
import com.example.dzcom.interfaces.request.task.ClosedLoopRunDetailRequest;
import com.example.dzcom.interfaces.request.task.ClosedLoopRunListRequest;
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

/** 自动投资闭环运行和步骤审计查询接口。 */
@RestController
@RequestMapping("/api/investment/closed-loop")
@RequiredArgsConstructor
@Tag(name = "自动投资闭环", description = "查询数据采集、报告、Prompt候选、Mock交易、回测反馈的全自动闭环运行审计")
public class ClosedLoopOrchestrationController {
    private final ClosedLoopOrchestrationApplicationService closedLoops;

    /**
     * 分页查询自动闭环运行记录。
     *
     * @param request 筛选、分页和排序请求
     * @return 闭环运行分页响应
     * @author dz
     * @date 2026-06-25
     */
    @PostMapping("/runs/list")
    @Operation(summary = "分页查询自动投资闭环运行", description = "查询每轮自动闭环运行状态、质量门禁、报告、组合、回测和失败原因，用于前端驾驶舱。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回闭环运行分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<ClosedLoopRunResponse>> list(@Valid @RequestBody ClosedLoopRunListRequest request) {
        return Result.success(PageResponse.from(closedLoops.listRuns(
            request.taskCode(),
            request.runStatus(),
            request.automationLevel(),
            request.marketScope(),
            request.themeCode(),
            request.mockUserBizId(),
            request.startedFrom(),
            request.startedTo(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "startedAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), ClosedLoopRunResponse::from));
    }

    /**
     * 查询自动闭环运行详情和步骤审计。
     *
     * @param request 运行业务 ID
     * @return 闭环运行详情
     * @author dz
     * @date 2026-06-25
     */
    @PostMapping("/runs/detail")
    @Operation(summary = "查询自动投资闭环详情", description = "返回单轮闭环运行详情及步骤输入摘要、输出摘要、失败或阻断原因。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回闭环运行详情", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "404", description = "闭环运行不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<ClosedLoopRunResponse> detail(@Valid @RequestBody ClosedLoopRunDetailRequest request) {
        return Result.success(ClosedLoopRunResponse.from(closedLoops.detail(request.bizId())));
    }
}
