package com.example.dzcom.infrastructure.persistence.repository.task;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.task.ClosedLoopRun;
import com.example.dzcom.domain.model.task.ClosedLoopStep;
import com.example.dzcom.domain.repository.task.ClosedLoopRunSearchCriteria;
import com.example.dzcom.domain.repository.task.ClosedLoopRunStore;
import com.example.dzcom.infrastructure.persistence.entity.task.ClosedLoopRunEntity;
import com.example.dzcom.infrastructure.persistence.entity.task.ClosedLoopStepEntity;
import com.example.dzcom.infrastructure.persistence.mapper.task.ClosedLoopRunMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** 自动投资闭环运行仓储实现。 */
@Repository
@RequiredArgsConstructor
public class ClosedLoopRunStoreImpl implements ClosedLoopRunStore {
    private final ClosedLoopRunMapper mapper;

    /** 保存或更新闭环运行记录。 */
    @Override
    public ClosedLoopRun saveRun(ClosedLoopRun run) {
        mapper.saveRun(toRunEntity(run));
        return run;
    }

    /** 保存或更新闭环步骤记录。 */
    @Override
    public ClosedLoopStep saveStep(ClosedLoopStep step) {
        mapper.saveStep(toStepEntity(step));
        return step;
    }

    /** 根据业务 ID 查询闭环运行记录。 */
    @Override
    public Optional<ClosedLoopRun> findRunByBizId(String bizId) {
        return Optional.ofNullable(mapper.selectRunByBizId(bizId)).map(this::toRunDomain);
    }

    /** 查询指定运行的步骤记录。 */
    @Override
    public List<ClosedLoopStep> findStepsByRunBizId(String runBizId) {
        return mapper.selectStepsByRunBizId(runBizId).stream()
            .map(this::toStepDomain)
            .toList();
    }

    /** 分页查询闭环运行记录。 */
    @Override
    public PageResult<ClosedLoopRun> searchRuns(ClosedLoopRunSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<ClosedLoopRun> items = mapper.searchRuns(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toRunDomain)
            .toList();
        long total = mapper.countRuns(criteria);
        return PageResult.<ClosedLoopRun>builder()
            .items(items)
            .total(total)
            .page(criteria.page())
            .size(criteria.size())
            .totalPages((int) Math.ceil((double) total / criteria.size()))
            .build();
    }

    /** 将接口排序字段转换为固定数据库列。 */
    private String resolveSortColumn(String sort) {
        return switch (sort) {
            case "runNo" -> "r.run_no";
            case "taskCode" -> "r.task_code";
            case "runStatus" -> "r.run_status";
            case "automationLevel" -> "r.automation_level";
            case "qualityScore" -> "r.quality_score";
            case "completedAt" -> "r.completed_at";
            case "updatedAt" -> "r.updated_at";
            default -> "r.started_at";
        };
    }

    /** 将运行领域对象转换为持久化实体。 */
    private ClosedLoopRunEntity toRunEntity(ClosedLoopRun run) {
        return ClosedLoopRunEntity.builder()
            .bizId(run.bizId())
            .runNo(run.runNo())
            .taskCode(run.taskCode())
            .triggerSource(run.triggerSource())
            .runStatus(run.runStatus())
            .automationLevel(run.automationLevel())
            .marketScope(run.marketScope())
            .themeCode(run.themeCode())
            .mockUserBizId(run.mockUserBizId())
            .portfolioBizId(run.portfolioBizId())
            .reportBizId(run.reportBizId())
            .promptBizId(run.promptBizId())
            .promptCode(run.promptCode())
            .promptVersion(run.promptVersion())
            .backtestBizId(run.backtestBizId())
            .qualityScore(run.qualityScore())
            .gateResult(run.gateResult())
            .summary(run.summary())
            .failureReason(run.failureReason())
            .startedAt(run.startedAt())
            .completedAt(run.completedAt())
            .createdAt(run.createdAt())
            .updatedAt(run.updatedAt())
            .build();
    }

    /** 将步骤领域对象转换为持久化实体。 */
    private ClosedLoopStepEntity toStepEntity(ClosedLoopStep step) {
        return ClosedLoopStepEntity.builder()
            .bizId(step.bizId())
            .runBizId(step.runBizId())
            .stepCode(step.stepCode())
            .stepName(step.stepName())
            .stepOrder(step.stepOrder())
            .stepStatus(step.stepStatus())
            .inputSummary(step.inputSummary())
            .outputSummary(step.outputSummary())
            .failureReason(step.failureReason())
            .startedAt(step.startedAt())
            .completedAt(step.completedAt())
            .createdAt(step.createdAt())
            .updatedAt(step.updatedAt())
            .build();
    }

    /** 将运行持久化实体转换为领域对象。 */
    private ClosedLoopRun toRunDomain(ClosedLoopRunEntity entity) {
        return ClosedLoopRun.builder()
            .bizId(entity.getBizId())
            .runNo(entity.getRunNo())
            .taskCode(entity.getTaskCode())
            .triggerSource(entity.getTriggerSource())
            .runStatus(entity.getRunStatus())
            .automationLevel(entity.getAutomationLevel())
            .marketScope(entity.getMarketScope())
            .themeCode(entity.getThemeCode())
            .mockUserBizId(entity.getMockUserBizId())
            .portfolioBizId(entity.getPortfolioBizId())
            .reportBizId(entity.getReportBizId())
            .promptBizId(entity.getPromptBizId())
            .promptCode(entity.getPromptCode())
            .promptVersion(entity.getPromptVersion())
            .backtestBizId(entity.getBacktestBizId())
            .qualityScore(entity.getQualityScore())
            .gateResult(entity.getGateResult())
            .summary(entity.getSummary())
            .failureReason(entity.getFailureReason())
            .startedAt(entity.getStartedAt())
            .completedAt(entity.getCompletedAt())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /** 将步骤持久化实体转换为领域对象。 */
    private ClosedLoopStep toStepDomain(ClosedLoopStepEntity entity) {
        return ClosedLoopStep.builder()
            .bizId(entity.getBizId())
            .runBizId(entity.getRunBizId())
            .stepCode(entity.getStepCode())
            .stepName(entity.getStepName())
            .stepOrder(entity.getStepOrder())
            .stepStatus(entity.getStepStatus())
            .inputSummary(entity.getInputSummary())
            .outputSummary(entity.getOutputSummary())
            .failureReason(entity.getFailureReason())
            .startedAt(entity.getStartedAt())
            .completedAt(entity.getCompletedAt())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
