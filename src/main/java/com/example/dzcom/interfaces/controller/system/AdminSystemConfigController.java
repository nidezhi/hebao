package com.example.dzcom.interfaces.controller.system;

import com.example.dzcom.application.command.system.SaveSystemConfigCommand;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.system.SystemConfigApplicationService;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.dto.response.system.SystemConfigResponse;
import com.example.dzcom.interfaces.request.system.SaveSystemConfigRequest;
import com.example.dzcom.interfaces.request.system.SystemConfigListRequest;
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

/** 非敏感系统配置管理接口。 */
@RestController
@RequestMapping("/api/admin/system-configs")
@RequiredArgsConstructor
@Tag(name = "系统配置管理", description = "维护非敏感系统配置，自动闭环默认项等运行参数以数据库配置表为准")
public class AdminSystemConfigController {
    private final SystemConfigApplicationService configs;

    /**
     * 分页查询系统配置。
     *
     * @param request 查询请求
     * @return 系统配置分页响应
     * @author dz
     * @date 2026-06-29
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询系统配置", description = "按配置分组、键名、环境和状态查询非敏感系统配置，供前端配置页结构化展示。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回系统配置分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页、排序或筛选参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<SystemConfigResponse>> list(@Valid @RequestBody SystemConfigListRequest request) {
        return Result.success(PageResponse.from(configs.list(
            request.configGroup(),
            request.keyword(),
            request.environment(),
            request.status(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "updatedAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), SystemConfigResponse::from));
    }

    /**
     * 保存系统配置。
     *
     * @param request 保存请求
     * @return 保存后的配置
     * @author dz
     * @date 2026-06-29
     */
    @PostMapping("/save")
    @Operation(summary = "保存系统配置", description = "新增或更新非敏感系统配置；前端提交结构化 configValue，后端根据 valueType 规范化为 JSON 列。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "保存成功，返回系统配置响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "配置分组、键名、类型或值不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<SystemConfigResponse> save(@Valid @RequestBody SaveSystemConfigRequest request) {
        return Result.success(SystemConfigResponse.from(configs.save(SaveSystemConfigCommand.builder()
            .configGroup(request.configGroup())
            .configKey(request.configKey())
            .environment(request.environment())
            .valueType(request.valueType())
            .configValue(request.configValue())
            .description(request.description())
            .status(request.status())
            .build())));
    }
}
