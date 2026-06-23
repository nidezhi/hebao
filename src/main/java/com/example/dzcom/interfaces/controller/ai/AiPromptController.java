package com.example.dzcom.interfaces.controller.ai;

import com.example.dzcom.application.command.ai.AiPromptOutputSchemaCommand;
import com.example.dzcom.application.command.ai.AiPromptPreviewCommand;
import com.example.dzcom.application.command.ai.AiPromptVariableCommand;
import com.example.dzcom.application.command.ai.SaveAiPromptTemplateCommand;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.ai.AiPromptApplicationService;
import com.example.dzcom.interfaces.dto.response.ai.AiPromptPreviewResponse;
import com.example.dzcom.interfaces.dto.response.ai.AiPromptTemplateResponse;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.request.ai.AiPromptBizIdRequest;
import com.example.dzcom.interfaces.request.ai.AiPromptListRequest;
import com.example.dzcom.interfaces.request.ai.AiPromptPreviewRequest;
import com.example.dzcom.interfaces.request.ai.AiPromptStatusRequest;
import com.example.dzcom.interfaces.request.ai.SaveAiPromptTemplateRequest;
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

/** AI Prompt 版本化治理接口。 */
@RestController
@RequestMapping("/api/ai/prompts")
@RequiredArgsConstructor
@Tag(name = "AI Prompt治理", description = "AI Prompt 模板版本、变量、输出 Schema、状态和本地预览接口")
public class AiPromptController {
    private final AiPromptApplicationService prompts;

    /**
     * 保存 Prompt 模板版本。
     *
     * @param request 保存 Prompt 请求
     * @return 保存后的 Prompt 模板响应
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/save")
    @Operation(summary = "保存AI Prompt模板", description = "新增或更新 Prompt 模板版本，并整体替换变量定义和输出 JSON Schema。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "保存成功，返回 Prompt 模板响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数、状态、场景、变量或 Schema 不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiPromptTemplateResponse> save(@Valid @RequestBody SaveAiPromptTemplateRequest request) {
        return Result.success(AiPromptTemplateResponse.from(prompts.save(SaveAiPromptTemplateCommand.builder()
            .promptCode(request.promptCode())
            .promptVersion(request.promptVersion())
            .scenario(request.scenario())
            .templateName(request.templateName())
            .templateContent(request.templateContent())
            .status(request.status())
            .description(request.description())
            .variables(request.variables() == null ? java.util.List.of() : request.variables().stream()
                .map(item -> AiPromptVariableCommand.builder()
                    .variableName(item.variableName())
                    .sourcePath(item.sourcePath())
                    .required(item.required())
                    .description(item.description())
                    .build())
                .toList())
            .outputSchemas(request.outputSchemas() == null ? java.util.List.of() : request.outputSchemas().stream()
                .map(item -> AiPromptOutputSchemaCommand.builder()
                    .schemaVersion(item.schemaVersion())
                    .schemaJson(item.schemaJson())
                    .build())
                .toList())
            .build())));
    }

    /**
     * 分页查询 Prompt 模板。
     *
     * @param request Prompt 分页查询请求
     * @return Prompt 模板分页响应
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询AI Prompt", description = "按 Prompt 编码、使用场景和状态查询 Prompt 模板版本。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回 Prompt 模板分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页、排序或筛选参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<AiPromptTemplateResponse>> list(@Valid @RequestBody AiPromptListRequest request) {
        return Result.success(PageResponse.from(prompts.list(
            request.promptCode(),
            request.scenario(),
            request.status(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "updatedAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), AiPromptTemplateResponse::from));
    }

    /**
     * 查询 Prompt 模板详情。
     *
     * @param request Prompt 业务 ID 请求
     * @return Prompt 模板详情响应
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/detail")
    @Operation(summary = "查询AI Prompt详情", description = "根据 Prompt 业务 ID 查询模板、变量定义和输出 Schema。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回 Prompt 模板详情", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "404", description = "Prompt模板不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiPromptTemplateResponse> detail(@Valid @RequestBody AiPromptBizIdRequest request) {
        return Result.success(AiPromptTemplateResponse.from(prompts.detail(request.bizId())));
    }

    /**
     * 变更 Prompt 状态。
     *
     * @param request Prompt 状态变更请求
     * @return 状态变更后的 Prompt 模板响应
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/status")
    @Operation(summary = "变更AI Prompt状态", description = "将 Prompt 状态变更为 DRAFT、VALIDATING、ACTIVE 或 RETIRED。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "变更成功，返回 Prompt 模板响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "状态不合法"),
        @ApiResponse(responseCode = "404", description = "Prompt模板不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiPromptTemplateResponse> status(@Valid @RequestBody AiPromptStatusRequest request) {
        return Result.success(AiPromptTemplateResponse.from(prompts.changeStatus(
            request.bizId(), request.status())));
    }

    /**
     * 本地预览 Prompt。
     *
     * @param request Prompt 预览请求
     * @return 渲染后的 Prompt 和缺失变量
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/preview")
    @Operation(summary = "预览AI Prompt", description = "按业务 ID 或编码版本本地渲染 Prompt，只替换变量并校验必填项，不触发真实模型调用。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "预览成功，返回渲染结果和缺失变量", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Prompt定位参数不足"),
        @ApiResponse(responseCode = "404", description = "Prompt模板不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiPromptPreviewResponse> preview(@Valid @RequestBody AiPromptPreviewRequest request) {
        return Result.success(AiPromptPreviewResponse.from(prompts.preview(AiPromptPreviewCommand.builder()
            .promptBizId(request.promptBizId())
            .promptCode(request.promptCode())
            .promptVersion(request.promptVersion())
            .variables(request.variables())
            .build())));
    }
}
