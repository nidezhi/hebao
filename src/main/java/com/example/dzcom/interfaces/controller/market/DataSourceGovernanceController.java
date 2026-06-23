package com.example.dzcom.interfaces.controller.market;

import com.example.dzcom.application.command.market.SaveDataQualitySnapshotCommand;
import com.example.dzcom.application.command.market.SaveDataSourceCommand;
import com.example.dzcom.application.command.market.SaveDataSourceHealthCommand;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.market.DataSourceGovernanceApplicationService;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.dto.response.market.DataQualitySnapshotResponse;
import com.example.dzcom.interfaces.dto.response.market.DataSourceResponse;
import com.example.dzcom.interfaces.request.market.DataQualitySnapshotListRequest;
import com.example.dzcom.interfaces.request.market.DataSourceListRequest;
import com.example.dzcom.interfaces.request.market.SaveDataQualitySnapshotRequest;
import com.example.dzcom.interfaces.request.market.SaveDataSourceHealthRequest;
import com.example.dzcom.interfaces.request.market.SaveDataSourceRequest;
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

/** 数据源治理后台接口。 */
@RestController
@RequestMapping("/api/admin/data-sources")
@RequiredArgsConstructor
@Tag(name = "数据源治理", description = "数据源注册、健康状态、质量快照和前端看板接口")
public class DataSourceGovernanceController {
    private final DataSourceGovernanceApplicationService sources;

    /**
     * 保存数据源注册信息。
     *
     * @param request 保存数据源请求
     * @return 数据源治理响应
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/save")
    @Operation(summary = "保存数据源", description = "新增或更新投资数据源注册信息，包括来源等级、类型、采集频率和启用状态。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "保存成功，返回数据源治理响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<DataSourceResponse> save(@Valid @RequestBody SaveDataSourceRequest request) {
        return Result.success(DataSourceResponse.from(sources.save(SaveDataSourceCommand.builder()
            .sourceCode(request.sourceCode())
            .sourceName(request.sourceName())
            .sourceType(request.sourceType())
            .trustLevel(request.trustLevel())
            .baseUrl(request.baseUrl())
            .enabled(request.enabled())
            .fetchFrequency(request.fetchFrequency())
            .owner(request.owner())
            .description(request.description())
            .build())));
    }

    /**
     * 保存数据源健康状态。
     *
     * @param request 保存健康状态请求
     * @return 数据源治理响应
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/health/save")
    @Operation(summary = "保存数据源健康状态", description = "保存数据源最近成功、失败、成功率、延迟和失败原因，用于后台数据源看板。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "保存成功，返回数据源治理响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数不合法"),
        @ApiResponse(responseCode = "404", description = "数据源不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<DataSourceResponse> saveHealth(@Valid @RequestBody SaveDataSourceHealthRequest request) {
        return Result.success(DataSourceResponse.from(sources.saveHealth(SaveDataSourceHealthCommand.builder()
            .sourceCode(request.sourceCode())
            .lastSuccessAt(request.lastSuccessAt())
            .lastFailureAt(request.lastFailureAt())
            .successRate(request.successRate())
            .avgLatencyMs(request.avgLatencyMs())
            .failureReason(request.failureReason())
            .sampleCount(request.sampleCount())
            .build())));
    }

    /**
     * 保存数据质量快照。
     *
     * @param request 保存质量快照请求
     * @return 数据质量快照响应
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/quality/save")
    @Operation(summary = "保存数据质量快照", description = "保存数据源某类数据的质量分、缺失率、重复率、新鲜度和解释详情。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "保存成功，返回质量快照", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数不合法"),
        @ApiResponse(responseCode = "404", description = "数据源不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<DataQualitySnapshotResponse> saveQuality(
        @Valid @RequestBody SaveDataQualitySnapshotRequest request
    ) {
        return Result.success(DataQualitySnapshotResponse.from(sources.saveQualitySnapshot(
            SaveDataQualitySnapshotCommand.builder()
                .sourceCode(request.sourceCode())
                .dataType(request.dataType())
                .qualityScore(request.qualityScore())
                .missingRate(request.missingRate())
                .duplicateRate(request.duplicateRate())
                .freshnessScore(request.freshnessScore())
                .sampleCount(request.sampleCount())
                .snapshotTime(request.snapshotTime())
                .detail(request.detail())
                .build())));
    }

    /**
     * 分页查询数据源看板。
     *
     * @param request 数据源列表请求
     * @return 数据源分页响应
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询数据源", description = "按关键字、类型、来源等级和启用状态查询数据源，并返回健康状态和最新质量快照。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回数据源分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "分页或筛选参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<DataSourceResponse>> list(@Valid @RequestBody DataSourceListRequest request) {
        return Result.success(PageResponse.from(sources.list(
            request.keyword(),
            request.sourceType(),
            request.trustLevel(),
            request.enabled(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "updatedAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        ), DataSourceResponse::from));
    }

    /**
     * 查询数据质量快照历史。
     *
     * @param request 质量快照查询请求
     * @return 数据质量快照集合
     * @author dz
     * @date 2026-06-23
     */
    @PostMapping("/quality/list")
    @Operation(summary = "查询数据质量快照", description = "查询指定数据源和可选数据类型的质量快照历史，用于前端质量趋势图。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回质量快照集合", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数不合法"),
        @ApiResponse(responseCode = "404", description = "数据源不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<java.util.List<DataQualitySnapshotResponse>> qualityList(
        @Valid @RequestBody DataQualitySnapshotListRequest request
    ) {
        return Result.success(sources.qualitySnapshots(
            request.sourceCode(),
            request.dataType(),
            request.limit()
        ).stream().map(DataQualitySnapshotResponse::from).toList());
    }
}
