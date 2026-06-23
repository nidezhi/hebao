package com.example.dzcom.interfaces.request.market;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 数据质量快照列表请求。 */
@Schema(description = "数据质量快照列表请求")
public record DataQualitySnapshotListRequest(
    @Schema(description = "数据源稳定编码")
    @NotBlank
    String sourceCode,
    @Schema(description = "数据类型，可为空")
    String dataType,
    @Schema(description = "返回数量上限，默认20，最大200")
    Integer limit
) {
}
