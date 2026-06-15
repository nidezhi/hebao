package com.example.dzcom.interfaces.dto.response.common;

import com.example.dzcom.application.common.page.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.function.Function;

/**
 * 接口层统一分页响应。
 *
 * @param <T> 接口响应数据类型
 */
@Builder
@Schema(description = "接口统一分页响应")
public record PageResponse<T>(
    @Schema(description = "当前页数据列表") List<T> items,
    @Schema(description = "数据总条数", example = "128") long total,
    @Schema(description = "当前页码，从 1 开始", example = "1") int page,
    @Schema(description = "每页条数", example = "20") int size,
    @Schema(description = "总页数", example = "7") int totalPages
) {

    /**
     * 将应用层分页结果转换为接口层分页响应。
     *
     * @param source 应用层分页结果
     * @param mapper 数据项转换器
     * @param <S> 应用层数据类型
     * @param <T> 接口层数据类型
     * @return 接口层分页响应
     * @author dz
     * @date 2026-06-15
     */
    public static <S, T> PageResponse<T> from(PageResult<S> source, Function<S, T> mapper) {
        return PageResponse.<T>builder()
            .items(source.items().stream().map(mapper).toList())
            .total(source.total())
            .page(source.page())
            .size(source.size())
            .totalPages(source.totalPages())
            .build();
    }
}
