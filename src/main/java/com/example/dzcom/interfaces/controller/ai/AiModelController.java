package com.example.dzcom.interfaces.controller.ai;

import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.ai.AiModelApplicationService;
import com.example.dzcom.interfaces.dto.response.ai.AiModelResponse;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.request.ai.AiModelBizIdRequest;
import com.example.dzcom.interfaces.request.ai.AiModelListRequest;
import com.example.dzcom.interfaces.request.ai.AiModelStatusRequest;
import com.example.dzcom.interfaces.request.ai.SaveAiModelRequest;
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

/** AI 模型注册和配置管理接口。 */
@RestController
@RequestMapping("/api/ai/models")
@RequiredArgsConstructor
@Tag(name = "AI模型管理", description = "AI 模型版本注册、参数配置、状态变更和查询接口")
public class AiModelController {
    private final AiModelApplicationService models;

    /**
     * 保存 AI 模型配置。
     *
     * @param request 模型配置请求
     * @return 保存后的模型
     * @author dz
     * @date 2026-06-17
     */
    @PostMapping("/save")
    @Operation(summary = "保存AI模型配置", description = "按模型编码和版本新增或更新 AI 模型配置，包含提供方、制品地址、模型参数和评估指标。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回保存后的模型配置", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiModelResponse> save(@Valid @RequestBody SaveAiModelRequest request) {
        return Result.success(AiModelResponse.from(models.save(
            request.modelCode(),
            request.modelVersion(),
            request.modelName(),
            request.modelType(),
            request.provider(),
            request.artifactUri(),
            request.modelConfig(),
            request.metrics(),
            request.status()
        )));
    }

    /**
     * 分页查询 AI 模型。
     *
     * @param request 查询条件
     * @return 模型分页结果
     * @author dz
     * @date 2026-06-17
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询AI模型", description = "按模型编码、类型、提供方和状态查询 AI 模型注册信息。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回模型分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<AiModelResponse>> list(@Valid @RequestBody AiModelListRequest request) {
        return Result.success(PageResponse.from(models.list(
            request.modelCode(),
            request.modelType(),
            request.provider(),
            request.status(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "updatedAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), AiModelResponse::from));
    }

    /**
     * 查询 AI 模型详情。
     *
     * @param request 模型业务 ID
     * @return 模型详情
     * @author dz
     * @date 2026-06-17
     */
    @PostMapping("/detail")
    @Operation(summary = "查询AI模型详情", description = "根据模型业务 ID 查询模型版本、配置、指标和状态。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回模型详情", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "404", description = "AI模型不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiModelResponse> detail(@Valid @RequestBody AiModelBizIdRequest request) {
        return Result.success(AiModelResponse.from(
            models.detail(request.bizId()),
            models.enabledSkills(request.bizId()).stream()
                .map(com.example.dzcom.interfaces.dto.response.ai.AiModelSkillBindingResponse::from)
                .toList()
        ));
    }

    /**
     * 变更 AI 模型状态。
     *
     * @param request 状态变更请求
     * @return 变更后的模型
     * @author dz
     * @date 2026-06-17
     */
    @PostMapping("/status")
    @Operation(summary = "变更AI模型状态", description = "将 AI 模型状态变更为 DRAFT、VALIDATING、ACTIVE、INACTIVE 或 ARCHIVED。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回变更后的模型", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "404", description = "AI模型不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiModelResponse> status(@Valid @RequestBody AiModelStatusRequest request) {
        return Result.success(AiModelResponse.from(models.changeStatus(
            request.bizId(), request.status())));
    }

    /**
     * 归档 AI 模型。
     *
     * @param request 模型业务 ID
     * @return 归档后的模型
     * @author dz
     * @date 2026-06-17
     */
    @PostMapping("/archive")
    @Operation(summary = "归档AI模型", description = "逻辑删除 AI 模型，将状态置为 ARCHIVED 并记录停用时间。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回归档后的模型", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "404", description = "AI模型不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiModelResponse> archive(@Valid @RequestBody AiModelBizIdRequest request) {
        return Result.success(AiModelResponse.from(models.archive(request.bizId())));
    }
}
