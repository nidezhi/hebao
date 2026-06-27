package com.example.dzcom.application.service.task;

import com.alibaba.fastjson2.JSON;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.task.ClosedLoopRunView;
import com.example.dzcom.application.dto.task.ClosedLoopStepView;
import com.example.dzcom.domain.model.task.ClosedLoopRun;
import com.example.dzcom.domain.model.task.ClosedLoopStep;
import com.example.dzcom.domain.repository.task.ClosedLoopRunSearchCriteria;
import com.example.dzcom.domain.repository.task.ClosedLoopRunStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 自动投资闭环运行记录应用服务。 */
@Service
@RequiredArgsConstructor
public class ClosedLoopOrchestrationApplicationService {
    private static final int FAILURE_REASON_MAX_LENGTH = 12_000;

    private static final Set<String> RUN_SORTS =
        Set.of("startedAt", "completedAt", "updatedAt", "runNo", "taskCode",
            "runStatus", "automationLevel", "qualityScore");

    private final ClosedLoopRunStore runs;
    private final IdGenerator ids;
    private final ClockProvider clock;

    @Value("${ai.wealth.closed-loop.running-timeout-minutes:30}")
    private long runningTimeoutMinutes;

    /**
     * 创建闭环运行记录。
     *
     * @param taskCode 来源任务编码
     * @param triggerSource 触发来源
     * @param automationLevel 自动化等级
     * @param marketScope 市场范围
     * @param themeCode 主题编码
     * @param mockUserBizId 自动 Mock 用户
     * @return 运行记录
     * @author dz
     * @date 2026-06-25
     */
    @Transactional
    public ClosedLoopRun createRun(String taskCode, String triggerSource, String automationLevel,
                                   String marketScope, String themeCode, String mockUserBizId) {
        LocalDateTime now = clock.now();
        ClosedLoopRun run = ClosedLoopRun.builder()
            .bizId(ids.newBizId())
            .runNo("CLR-" + now.toLocalDate().toString().replace("-", "") + "-" + shortId(ids.newBizId()))
            .taskCode(taskCode)
            .triggerSource(defaultIfBlank(triggerSource, "SCHEDULE"))
            .runStatus("RUNNING")
            .automationLevel(defaultIfBlank(automationLevel, "FULL_MOCK"))
            .marketScope(defaultIfBlank(marketScope, TaskParameterParser.CN_MAINLAND))
            .themeCode(trimToNull(themeCode))
            .mockUserBizId(trimToNull(mockUserBizId))
            .gateResult("PENDING")
            .startedAt(now)
            .createdAt(now)
            .updatedAt(now)
            .build();
        return runs.saveRun(run);
    }

    /** 更新闭环运行记录。 */
    @Transactional
    public ClosedLoopRun updateRun(ClosedLoopRun run) {
        return runs.saveRun(run.toBuilder().updatedAt(clock.now()).build());
    }

    /** 标记闭环运行完成。 */
    @Transactional
    public ClosedLoopRun completeRun(ClosedLoopRun run, String status, String gateResult,
                                     String failureReason, Map<String, Object> summary) {
        LocalDateTime now = clock.now();
        return runs.saveRun(run.toBuilder()
            .runStatus(status)
            .gateResult(gateResult)
            .summary(summary == null || summary.isEmpty() ? run.summary() : JSON.toJSONString(summary))
            .failureReason(limit(trimToNull(failureReason), FAILURE_REASON_MAX_LENGTH))
            .completedAt(now)
            .updatedAt(now)
            .build());
    }

    /** 记录成功步骤。 */
    @Transactional
    public ClosedLoopStep succeedStep(ClosedLoopRun run, String stepCode, String stepName, int stepOrder,
                                      Map<String, Object> input, Map<String, Object> output) {
        LocalDateTime now = clock.now();
        return runs.saveStep(ClosedLoopStep.builder()
            .bizId(ids.newBizId())
            .runBizId(run.bizId())
            .stepCode(stepCode)
            .stepName(stepName)
            .stepOrder(stepOrder)
            .stepStatus("SUCCEEDED")
            .inputSummary(toJson(input))
            .outputSummary(toJson(output))
            .startedAt(now)
            .completedAt(now)
            .createdAt(now)
            .updatedAt(now)
            .build());
    }

    /** 记录跳过步骤。 */
    @Transactional
    public ClosedLoopStep skippedStep(ClosedLoopRun run, String stepCode, String stepName, int stepOrder,
                                      String reason, Map<String, Object> input) {
        return terminalStep(run, stepCode, stepName, stepOrder, "SKIPPED", reason, input);
    }

    /** 记录阻断步骤。 */
    @Transactional
    public ClosedLoopStep blockedStep(ClosedLoopRun run, String stepCode, String stepName, int stepOrder,
                                      String reason, Map<String, Object> input) {
        return terminalStep(run, stepCode, stepName, stepOrder, "BLOCKED", reason, input);
    }

    /** 记录失败步骤。 */
    @Transactional
    public ClosedLoopStep failedStep(ClosedLoopRun run, String stepCode, String stepName, int stepOrder,
                                    String reason, Map<String, Object> input) {
        return terminalStep(run, stepCode, stepName, stepOrder, "FAILED", reason, input);
    }

    /** 分页查询闭环运行。 */
    @Transactional
    public PageResult<ClosedLoopRunView> listRuns(String taskCode, String runStatus, String automationLevel,
                                                  String marketScope, String themeCode, String mockUserBizId,
                                                  LocalDateTime startedFrom, LocalDateTime startedTo,
                                                  PageQuery query) {
        PageResult<ClosedLoopRun> page = runs.searchRuns(new ClosedLoopRunSearchCriteria(
            trimToNull(taskCode),
            trimToNull(runStatus),
            trimToNull(automationLevel),
            trimToNull(marketScope),
            trimToNull(themeCode),
            trimToNull(mockUserBizId),
            startedFrom,
            startedTo,
            query.page(),
            query.size(),
            query.safeSort(RUN_SORTS, "startedAt"),
            "asc".equals(query.direction())
        ));
        return PageResult.<ClosedLoopRunView>builder()
            .items(page.items().stream().map(run -> toRunView(refreshStaleRunningRun(run), List.of())).toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    /** 查询闭环运行详情。 */
    @Transactional
    public ClosedLoopRunView detail(String bizId) {
        ClosedLoopRun run = runs.findRunByBizId(normalizeText(bizId, "闭环运行ID不能为空"))
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "闭环运行不存在"));
        run = refreshStaleRunningRun(run);
        return toRunView(run, runs.findStepsByRunBizId(run.bizId()));
    }

    /** 把明显卡死的 RUNNING 运行转成可见失败，避免驾驶舱永远停在第一步。 */
    private ClosedLoopRun refreshStaleRunningRun(ClosedLoopRun run) {
        if (!"RUNNING".equals(run.runStatus()) || run.startedAt() == null || runningTimeoutMinutes <= 0) {
            return run;
        }
        LocalDateTime timeoutAt = run.startedAt().plusMinutes(runningTimeoutMinutes);
        if (!clock.now().isAfter(timeoutAt)) {
            return run;
        }
        String reason = "自动闭环运行超时: startedAt=" + run.startedAt()
            + ", timeoutMinutes=" + runningTimeoutMinutes
            + ", 当前运行可能因远程模型、Kafka消费或采集子任务未返回而卡住，请查看任务执行记录和应用日志";
        failedStep(run, "RUNNING_TIMEOUT", "运行超时保护", 998, reason, Map.of(
            "startedAt", run.startedAt(),
            "timeoutMinutes", runningTimeoutMinutes
        ));
        return completeRun(run, "FAILED", "BLOCK", reason, Map.of(
            "failureReason", reason,
            "displaySeverity", "ERROR",
            "blocking", true
        ));
    }

    /** 记录终态步骤。 */
    private ClosedLoopStep terminalStep(ClosedLoopRun run, String stepCode, String stepName, int stepOrder,
                                        String status, String reason, Map<String, Object> input) {
        LocalDateTime now = clock.now();
        return runs.saveStep(ClosedLoopStep.builder()
            .bizId(ids.newBizId())
            .runBizId(run.bizId())
            .stepCode(stepCode)
            .stepName(stepName)
            .stepOrder(stepOrder)
            .stepStatus(status)
            .inputSummary(toJson(input))
            .failureReason(limit(trimToNull(reason), FAILURE_REASON_MAX_LENGTH))
            .startedAt(now)
            .completedAt(now)
            .createdAt(now)
            .updatedAt(now)
            .build());
    }

    /** 转换运行视图。 */
    private ClosedLoopRunView toRunView(ClosedLoopRun run, List<ClosedLoopStep> steps) {
        return ClosedLoopRunView.builder()
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
            .steps(steps.stream().map(this::toStepView).toList())
            .build();
    }

    /** 转换步骤视图。 */
    private ClosedLoopStepView toStepView(ClosedLoopStep step) {
        return ClosedLoopStepView.builder()
            .bizId(step.bizId())
            .stepCode(step.stepCode())
            .stepName(step.stepName())
            .stepOrder(step.stepOrder())
            .stepStatus(step.stepStatus())
            .inputSummary(step.inputSummary())
            .outputSummary(step.outputSummary())
            .failureReason(step.failureReason())
            .startedAt(step.startedAt())
            .completedAt(step.completedAt())
            .build();
    }

    /** 转 JSON。 */
    private String toJson(Map<String, Object> value) {
        return value == null || value.isEmpty() ? null : JSON.toJSONString(value);
    }

    /** 默认文本。 */
    private String defaultIfBlank(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    /** 必填文本。 */
    private String normalizeText(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return trimmed;
    }

    /** 去空白。 */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** 限制审计文本长度，避免极端异常信息撑爆数据库 TEXT 列。 */
    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /** 生成面向前端展示的短 ID 片段。 */
    private String shortId(String value) {
        String normalized = value == null ? "" : value.replace("-", "");
        return normalized.length() <= 8 ? normalized : normalized.substring(0, 8);
    }
}
