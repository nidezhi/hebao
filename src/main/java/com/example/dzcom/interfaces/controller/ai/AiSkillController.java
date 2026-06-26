package com.example.dzcom.interfaces.controller.ai;

import com.example.dzcom.application.command.ai.SaveAiSkillCommand;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.ai.AiSkillApplicationService;
import com.example.dzcom.interfaces.dto.response.ai.AiSkillResponse;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.request.ai.AiSkillBizIdRequest;
import com.example.dzcom.interfaces.request.ai.AiSkillListRequest;
import com.example.dzcom.interfaces.request.ai.AiSkillStatusRequest;
import com.example.dzcom.interfaces.request.ai.SaveAiSkillRequest;
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

/** AI Skill 维护接口。 */
@RestController
@RequestMapping("/api/ai/skills")
@RequiredArgsConstructor
@Tag(name = "AI Skill管理", description = "维护数据源发现、Prompt 治理等大模型 Skill 版本和生命周期")
public class AiSkillController {
    private final AiSkillApplicationService skills;

    /**
     * 保存 AI Skill。
     *
     * @param request 保存请求
     * @return 保存后的 Skill
     * @author dz
     * @date 2026-06-26
     */
    @PostMapping("/save")
    @Operation(summary = "保存AI Skill", description = "新增或更新 AI Skill 版本、指令、输入输出 Schema 和评估策略。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回保存后的 Skill", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiSkillResponse> save(@Valid @RequestBody SaveAiSkillRequest request) {
        return Result.success(AiSkillResponse.from(skills.save(SaveAiSkillCommand.builder()
            .skillCode(request.skillCode())
            .skillVersion(request.skillVersion())
            .skillName(request.skillName())
            .skillType(request.skillType())
            .status(request.status())
            .instructionContent(request.instructionContent())
            .inputSchema(request.inputSchema())
            .outputSchema(request.outputSchema())
            .evaluationPolicy(request.evaluationPolicy())
            .description(request.description())
            .build())));
    }

    /**
     * 分页查询 AI Skill。
     *
     * @param request 查询请求
     * @return Skill 分页响应
     * @author dz
     * @date 2026-06-26
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询AI Skill", description = "按编码、类型、状态和关键词查询 AI Skill。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回 Skill 分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或排序参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<AiSkillResponse>> list(@Valid @RequestBody AiSkillListRequest request) {
        return Result.success(PageResponse.from(skills.list(
            request.skillCode(),
            request.skillType(),
            request.status(),
            request.keyword(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "updatedAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), AiSkillResponse::from));
    }

    /**
     * 查询 AI Skill 详情。
     *
     * @param request Skill 业务 ID
     * @return Skill 详情
     * @author dz
     * @date 2026-06-26
     */
    @PostMapping("/detail")
    @Operation(summary = "查询AI Skill详情", description = "根据 Skill 业务 ID 查询完整指令、Schema、评估策略和生命周期状态。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回 Skill 详情", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "404", description = "Skill 不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiSkillResponse> detail(@Valid @RequestBody AiSkillBizIdRequest request) {
        return Result.success(AiSkillResponse.from(skills.detail(request.bizId())));
    }

    /**
     * 变更 AI Skill 状态。
     *
     * @param request 状态变更请求
     * @return 变更后的 Skill
     * @author dz
     * @date 2026-06-26
     */
    @PostMapping("/status")
    @Operation(summary = "变更AI Skill状态", description = "将 Skill 状态变更为 DRAFT、VALIDATING、ACTIVE、RETIRED 或 ARCHIVED。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回变更后的 Skill", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "404", description = "Skill 不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<AiSkillResponse> status(@Valid @RequestBody AiSkillStatusRequest request) {
        return Result.success(AiSkillResponse.from(skills.changeStatus(request.bizId(), request.status())));
    }
}
