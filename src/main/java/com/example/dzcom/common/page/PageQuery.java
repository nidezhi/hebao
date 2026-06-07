package com.example.dzcom.common.page;

import com.example.dzcom.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.util.Set;

public record PageQuery(int page, int size, String sort, String direction) {

    public PageQuery {
        if (page < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "page 必须大于等于 1");
        }
        if (size < 1 || size > 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "size 必须在 1 到 100 之间");
        }
        direction = "asc".equalsIgnoreCase(direction) ? "asc" : "desc";
    }

    public String safeSort(Set<String> allowed, String defaultSort) {
        return allowed.contains(sort) ? sort : defaultSort;
    }
}
