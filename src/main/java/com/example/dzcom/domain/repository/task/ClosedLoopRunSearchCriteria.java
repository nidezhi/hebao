package com.example.dzcom.domain.repository.task;

import java.time.LocalDateTime;

/** 自动投资闭环运行分页查询条件。 */
public record ClosedLoopRunSearchCriteria(
    String taskCode,
    String runStatus,
    String automationLevel,
    String marketScope,
    String themeCode,
    String mockUserBizId,
    LocalDateTime startedFrom,
    LocalDateTime startedTo,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
