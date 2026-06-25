package com.example.dzcom.domain.repository.task;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.task.ClosedLoopRun;
import com.example.dzcom.domain.model.task.ClosedLoopStep;

import java.util.List;
import java.util.Optional;

/** 自动投资闭环运行仓储端口。 */
public interface ClosedLoopRunStore {
    /** 保存或更新闭环运行记录。 */
    ClosedLoopRun saveRun(ClosedLoopRun run);

    /** 保存或更新闭环步骤记录。 */
    ClosedLoopStep saveStep(ClosedLoopStep step);

    /** 根据业务 ID 查询闭环运行记录。 */
    Optional<ClosedLoopRun> findRunByBizId(String bizId);

    /** 查询指定运行的步骤记录。 */
    List<ClosedLoopStep> findStepsByRunBizId(String runBizId);

    /** 分页查询闭环运行记录。 */
    PageResult<ClosedLoopRun> searchRuns(ClosedLoopRunSearchCriteria criteria);
}
