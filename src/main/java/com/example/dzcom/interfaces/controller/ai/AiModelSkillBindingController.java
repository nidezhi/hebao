package com.example.dzcom.interfaces.controller.ai;

import com.example.dzcom.application.command.ai.SaveAiModelSkillBindingCommand;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.ai.AiModelSkillBindingApplicationService;
import com.example.dzcom.interfaces.dto.response.ai.AiModelSkillBindingResponse;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.request.ai.AiModelSkillBindingBizIdRequest;
import com.example.dzcom.interfaces.request.ai.AiModelSkillBindingListRequest;
import com.example.dzcom.interfaces.request.ai.AiModelSkillsRequest;
import com.example.dzcom.interfaces.request.ai.SaveAiModelSkillBindingRequest;
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

import java.util.List;

/** AI 模型 Skill 绑定维护接口。 */
@RestController
@RequestMapping("/api/ai/model-skills")
@RequiredArgsConstructor
@Tag(name = "AI模型Skill绑定", description = "维护模型实例与数据源发现、Prompt 治理等 Skill 版本的关联")
public class AiModelSkillBindingController {
    private final AiModelSkillBindingApplicationService bindings;

    /**
     * 保存模型 Skill 绑定。
     *
     * @param request 保存请求
     * @return 保存后的绑定
     * @author dz
     * @date 2026-06-26
     */
    @PostMapping("/save")
    @Operation(summary = "保存模型Skill绑定", description = "将模型实例绑定到指定 Skill 版本和业务场景。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回保存后的绑定", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "404", description = "模型或 Skill 不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiModelSkillBindingResponse> save(@Valid @RequestBody SaveAiModelSkillBindingRequest request) {
        return Result.success(AiModelSkillBindingResponse.from(bindings.save(SaveAiModelSkillBindingCommand.builder()
            .modelBizId(request.modelBizId())
            .skillBizId(request.skillBizId())
            .scenarioCode(request.scenarioCode())
            .priority(request.priority())
            .enabled(request.enabled())
            .config(request.config())
            .description(request.description())
            .build())));
    }

    /**
     * 分页查询模型 Skill 绑定。
     *
     * @param request 查询请求
     * @return 绑定分页响应
     * @author dz
     * @date 2026-06-26
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询模型Skill绑定", description = "按模型、Skill、业务场景和启用状态查询绑定。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回绑定分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<AiModelSkillBindingResponse>> list(
        @Valid @RequestBody AiModelSkillBindingListRequest request
    ) {
        return Result.success(PageResponse.from(bindings.list(
            request.modelBizId(),
            request.modelCode(),
            request.skillCode(),
            request.scenarioCode(),
            request.enabled(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "updatedAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), AiModelSkillBindingResponse::from));
    }

    /**
     * 查询模型 Skill 绑定详情。
     *
     * @param request 绑定业务 ID
     * @return 绑定详情
     * @author dz
     * @date 2026-06-26
     */
    @PostMapping("/detail")
    @Operation(summary = "查询模型Skill绑定详情", description = "根据绑定业务 ID 查询模型、Skill、场景和配置。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回绑定详情", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "404", description = "绑定不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiModelSkillBindingResponse> detail(
        @Valid @RequestBody AiModelSkillBindingBizIdRequest request
    ) {
        return Result.success(AiModelSkillBindingResponse.from(bindings.detail(request.bizId())));
    }

    /**
     * 查询模型已启用 Skill。
     *
     * @param request 模型业务 ID
     * @return 启用 Skill 绑定列表
     * @author dz
     * @date 2026-06-26
     */
    @PostMapping("/by-model")
    @Operation(summary = "查询模型已启用Skill", description = "查询指定模型实例当前启用的 Skill 绑定，用于模型详情和复盘页展示。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回启用绑定列表", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<List<AiModelSkillBindingResponse>> byModel(@Valid @RequestBody AiModelSkillsRequest request) {
        return Result.success(bindings.enabledByModel(request.modelBizId()).stream()
            .map(AiModelSkillBindingResponse::from)
            .toList());
    }
}
