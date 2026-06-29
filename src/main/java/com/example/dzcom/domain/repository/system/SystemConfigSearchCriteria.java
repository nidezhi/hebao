package com.example.dzcom.domain.repository.system;

import io.swagger.v3.oas.annotations.media.Schema;

/** 系统配置分页查询条件。 */
@Schema(description = "系统配置分页查询条件")
public record SystemConfigSearchCriteria(
    @Schema(description = "配置分组编码")
    String configGroup,
    @Schema(description = "配置键名关键字")
    String keyword,
    @Schema(description = "配置生效环境")
    String environment,
    @Schema(description = "配置状态")
    String status,
    @Schema(description = "页码，从 1 开始")
    int page,
    @Schema(description = "每页数量")
    int size,
    @Schema(description = "排序字段")
    String sort,
    @Schema(description = "是否升序")
    boolean ascending
) {
}
