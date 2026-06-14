package com.example.dzcom.application.common.page;

import com.example.dzcom.application.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.util.Set;

/**
 * 与具体分页框架解耦的公共分页请求。
 *
 * <p>项目对外响应仍使用从 1 开始的页码。考虑到部分前端表格和 Spring Data
 * 默认从 0 开始请求第一页，入参 {@code page=0} 会在边界层兼容转换为第一页；
 * {@code page>=1} 保持原有的一基页码语义，负数仍视为无效参数。</p>
 */
public record PageQuery(int page, int size, String sort, String direction) {

    public PageQuery {
        if (page < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "page 不能小于 0");
        }
        // 兼容前端组件常用的零基第一页，同时保持应用层和响应结果统一使用一基页码。
        page = page == 0 ? 1 : page;
        if (size < 1 || size > 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "size 必须在 1 到 100 之间");
        }
        direction = "asc".equalsIgnoreCase(direction) ? "asc" : "desc";
    }

    /**
     * 只允许应用服务声明过的排序字段进入持久化层，避免任意属性排序。
     *
     * @param allowed allowed 参数
     * @param defaultSort defaultSort 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public String safeSort(Set<String> allowed, String defaultSort) {
        return allowed.contains(sort) ? sort : defaultSort;
    }
}
