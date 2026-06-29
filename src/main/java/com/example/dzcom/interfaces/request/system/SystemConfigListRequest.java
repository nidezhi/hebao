package com.example.dzcom.interfaces.request.system;

import io.swagger.v3.oas.annotations.media.Schema;

/** 系统配置列表请求。 */
@Schema(description = "系统配置列表请求")
public record SystemConfigListRequest(
    @Schema(description = "配置分组编码")
    String configGroup,
    @Schema(description = "配置键名或说明关键字")
    String keyword,
    @Schema(description = "配置生效环境")
    String environment,
    @Schema(description = "配置状态：ENABLED/DISABLED")
    String status,
    @Schema(description = "页码，从 1 开始")
    Integer page,
    @Schema(description = "每页数量")
    Integer size,
    @Schema(description = "排序字段：updatedAt/configGroup/configKey/environment/valueType/status")
    String sort,
    @Schema(description = "排序方向：asc/desc")
    String direction
) {
}
