package com.example.dzcom.application.common.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/** 与具体分页框架解耦的统一分页结果。 */
@Builder
@Schema(description = "统一分页结果")
public record PageResult<T>(
    @Schema(description = "当前页数据列表")
    List<T> items,
    @Schema(description = "数据总条数", example = "128")
    long total,
    @Schema(description = "当前页码，从 1 开始", example = "1")
    int page,
    @Schema(description = "每页条数", example = "20")
    int size,
    @Schema(description = "总页数", example = "7")
    int totalPages
) {
}
