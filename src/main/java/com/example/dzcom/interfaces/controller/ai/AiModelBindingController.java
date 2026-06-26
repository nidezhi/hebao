package com.example.dzcom.interfaces.controller.ai;

import com.example.dzcom.application.command.ai.SaveAiModelBindingCommand;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.ai.AiModelBindingApplicationService;
import com.example.dzcom.interfaces.dto.response.ai.AiModelBindingResponse;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.request.ai.AiModelBindingDetailRequest;
import com.example.dzcom.interfaces.request.ai.AiModelBindingListRequest;
import com.example.dzcom.interfaces.request.ai.SaveAiModelBindingRequest;
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

/** AI 模型挂靠配置接口。 */
@RestController
@RequestMapping("/api/ai/model-bindings")
@RequiredArgsConstructor
@Tag(name = "AI模型挂靠配置", description = "为数据源发现、自动报告、闭环编排和 Prompt 治理等场景绑定模型")
public class AiModelBindingController {
    private final AiModelBindingApplicationService bindings;

    /**
     * 保存 AI 模型挂靠配置。
     *
     * @param request 保存请求
     * @return 保存后的配置
     * @author dz
     * @date 2026-06-26
     */
    @PostMapping("/save")
    @Operation(summary = "保存AI模型挂靠配置", description = "按业务场景和环境保存模型挂靠配置，前端可统一配置所有需要挂靠大模型的节点。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "保存成功", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数不合法或模型未启用"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiModelBindingResponse> save(@Valid @RequestBody SaveAiModelBindingRequest request) {
        return Result.success(AiModelBindingResponse.from(bindings.save(SaveAiModelBindingCommand.builder()
            .scenarioCode(request.scenarioCode())
            .scenarioName(request.scenarioName())
            .modelCode(request.modelCode())
            .providerCode(request.providerCode())
            .environment(request.environment())
            .enabled(request.enabled())
            .config(request.config())
            .description(request.description())
            .build())));
    }

    /**
     * 分页查询 AI 模型挂靠配置。
     *
     * @param request 查询请求
     * @return 分页响应
     * @author dz
     * @date 2026-06-26
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询AI模型挂靠配置", description = "按场景、模型、提供方、环境和启用状态查询模型挂靠配置。")
    public Result<PageResponse<AiModelBindingResponse>> list(@Valid @RequestBody AiModelBindingListRequest request) {
        return Result.success(PageResponse.from(bindings.list(
            request.scenarioCode(),
            request.modelCode(),
            request.providerCode(),
            request.environment(),
            request.enabled(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "updatedAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), AiModelBindingResponse::from));
    }

    /**
     * 查询 AI 模型挂靠配置详情。
     *
     * @param request 详情请求
     * @return 配置详情
     * @author dz
     * @date 2026-06-26
     */
    @PostMapping("/detail")
    @Operation(summary = "查询AI模型挂靠配置详情", description = "根据场景编码和环境查询模型挂靠配置。")
    public Result<AiModelBindingResponse> detail(@Valid @RequestBody AiModelBindingDetailRequest request) {
        return Result.success(AiModelBindingResponse.from(bindings.detail(
            request.scenarioCode(),
            request.environment()
        )));
    }
}
