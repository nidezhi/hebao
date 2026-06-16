package com.example.dzcom.domain.repository.task;

import java.time.LocalDateTime;

/** 投资主题快照分页筛选条件。 */
public record InvestmentThemeSnapshotSearchCriteria(
    String taskCode,
    String snapshotType,
    String themeCode,
    LocalDateTime snapshotFrom,
    LocalDateTime snapshotTo,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
