package com.example.dzcom.application.command.portfolio;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** 删除模拟投资组合命令。 */
@Builder
@Schema(description = "删除模拟投资组合命令")
public record DeleteMockPortfolioCommand(
    @Schema(description = "模拟组合业务唯一标识")
    String portfolioBizId
) {
}
