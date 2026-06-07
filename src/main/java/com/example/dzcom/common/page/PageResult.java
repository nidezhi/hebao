package com.example.dzcom.common.page;

import lombok.Builder;

import java.util.List;

/** 与具体分页框架解耦的统一分页结果。 */
@Builder
public record PageResult<T>(
    List<T> items,
    long total,
    int page,
    int size,
    int totalPages
) {
}
