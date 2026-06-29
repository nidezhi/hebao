package com.example.dzcom.interfaces.controller.ai;

import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.interfaces.dto.response.ai.ReviewLoopMetadataResponse;
import com.example.dzcom.interfaces.request.ai.ReviewLoopMetadataRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 复盘闭环结构化元数据接口。 */
@RestController
@RequestMapping("/api/review-loop")
@Tag(name = "复盘闭环元数据", description = "为前端复盘闭环页面提供策略、字典和字段级结构化契约")
public class ReviewLoopMetadataController {

    /** 查询复盘闭环结构化元数据。 */
    @PostMapping("/metadata")
    @Operation(summary = "查询复盘闭环结构化元数据", description = "返回回测、反馈和Prompt评估表单需要的选择器、字典和字段schema。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功，返回复盘闭环结构化元数据", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "请求参数不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<ReviewLoopMetadataResponse> metadata(
        @Valid @RequestBody ReviewLoopMetadataRequest request
    ) {
        return Result.success(ReviewLoopMetadataResponse.defaults());
    }
}
