package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.task.InvestmentTaskDefinitionView;
import com.example.dzcom.application.dto.task.InvestmentTaskTriggerResult;
import com.example.dzcom.domain.model.task.InvestmentTaskDefinition;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.model.task.NewsArticleRelation;
import com.example.dzcom.domain.model.task.ScheduledTaskExecution;
import com.example.dzcom.domain.repository.task.InvestmentTaskDefinitionStore;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotSearchCriteria;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.example.dzcom.domain.repository.task.NewsArticleSearchCriteria;
import com.example.dzcom.domain.repository.task.NewsArticleRelationSearchCriteria;
import com.example.dzcom.domain.repository.task.NewsArticleRelationStore;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import com.example.dzcom.domain.repository.task.ScheduledTaskExecutionSearchCriteria;
import com.example.dzcom.domain.repository.task.ScheduledTaskExecutionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 投资任务配置、触发和结果查询用例。 */
@Service
@RequiredArgsConstructor
public class InvestmentTaskManagementService {
    private static final Set<String> EXECUTION_SORTS =
        Set.of("startedAt", "completedAt", "createdAt", "taskCode", "taskType", "status");
    private static final Set<String> ARTICLE_SORTS =
        Set.of("publishTime", "collectedAt", "createdAt", "title", "sourceCode");
    private static final Set<String> SNAPSHOT_SORTS =
        Set.of("snapshotTime", "createdAt", "taskCode", "snapshotType", "themeCode",
            "returnRate", "momentumScore", "heatScore");
    private static final Set<String> RELATION_SORTS =
        Set.of("createdAt", "relationScore", "sourceQualityScore", "themeCode", "productCode");

    private final InvestmentTaskDefinitionStore definitions;
    private final InvestmentTaskTriggerPort triggerPort;
    private final ScheduledTaskExecutionStore executions;
    private final NewsArticleStore articles;
    private final NewsArticleRelationStore relations;
    private final InvestmentThemeSnapshotStore snapshots;
    private final IdGenerator ids;
    private final ClockProvider clock;

    @Value("${ai.wealth.task.running-timeout-minutes:30}")
    private long runningTimeoutMinutes;

    /** 查询当前生效的投资任务配置。 */
    @Transactional(readOnly = true)
    public List<InvestmentTaskDefinitionView> definitions() {
        return definitions.findAll().stream()
            .map(definition -> InvestmentTaskDefinitionView.builder()
                .code(definition.taskCode())
                .type(definition.taskType())
                .cron(definition.cron())
                .zone(definition.zone())
                .enabled(definition.enabled())
                .parameters(new LinkedHashMap<>(definition.parameters()))
                .description(definition.description())
                .build())
            .toList();
    }

    /** 新增或更新投资任务配置。 */
    @Transactional
    public InvestmentTaskDefinitionView saveDefinition(
        String code,
        String type,
        String cron,
        String zone,
        Boolean enabled,
        Map<String, String> parameters,
        String description
    ) {
        LocalDateTime now = clock.now();
        InvestmentTaskDefinition existed = definitions.findByCode(code).orElse(null);
        InvestmentTaskDefinition definition = definitions.save(InvestmentTaskDefinition.builder()
            .bizId(existed == null ? ids.newBizId() : existed.bizId())
            .taskCode(code)
            .taskType(type)
            .cron(cron)
            .zone(zone == null || zone.isBlank() ? "Asia/Shanghai" : zone)
            .enabled(enabled == null || enabled)
            .parameters(parameters == null ? new LinkedHashMap<>() : new LinkedHashMap<>(parameters))
            .description(description)
            .createdAt(existed == null ? now : existed.createdAt())
            .updatedAt(now)
            .build());
        return InvestmentTaskDefinitionView.builder()
            .code(definition.taskCode())
            .type(definition.taskType())
            .cron(definition.cron())
            .zone(definition.zone())
            .enabled(definition.enabled())
            .parameters(definition.parameters())
            .description(definition.description())
            .build();
    }

    /** 手动触发一次配置内的投资任务。 */
    @Transactional
    public InvestmentTaskTriggerResult trigger(String taskCode, Map<String, String> overrides,
                                               String triggerSource) {
        InvestmentTaskDefinition definition = requiredDefinition(taskCode);
        LocalDateTime now = clock.now();
        Map<String, String> parameters = new LinkedHashMap<>(definition.parameters());
        if (overrides != null) {
            parameters.putAll(overrides);
        }
        InvestmentTaskEvent event = InvestmentTaskEvent.builder()
            .eventId(ids.newBizId())
            .taskCode(definition.taskCode())
            .taskType(definition.taskType())
            .triggerSource(triggerSource == null || triggerSource.isBlank() ? "MANUAL" : triggerSource)
            .parameters(parameters)
            .triggeredAt(now)
            .build();
        triggerPort.publish(event);
        return InvestmentTaskTriggerResult.builder()
            .eventId(event.eventId())
            .taskCode(event.taskCode())
            .taskType(event.taskType())
            .triggerSource(event.triggerSource())
            .triggeredAt(event.triggeredAt())
            .build();
    }

    /** 分页查询任务执行记录。 */
    @Transactional
    public PageResult<ScheduledTaskExecution> executions(String taskCode, String taskType, String status,
                                                         LocalDateTime startedFrom,
                                                         LocalDateTime startedTo,
                                                         PageQuery pageQuery) {
        PageResult<ScheduledTaskExecution> page = executions.search(new ScheduledTaskExecutionSearchCriteria(
            taskCode, taskType, status, startedFrom, startedTo,
            pageQuery.page(), pageQuery.size(),
            pageQuery.safeSort(EXECUTION_SORTS, "startedAt"),
            "asc".equals(pageQuery.direction())
        ));
        return PageResult.<ScheduledTaskExecution>builder()
            .items(page.items().stream().map(this::refreshStaleRunningExecution).toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    /** 把明显卡死的 RUNNING 执行转成失败态，方便前端和运维定位。 */
    private ScheduledTaskExecution refreshStaleRunningExecution(ScheduledTaskExecution execution) {
        if (!"RUNNING".equals(execution.status()) || execution.startedAt() == null || runningTimeoutMinutes <= 0) {
            return execution;
        }
        LocalDateTime timeoutAt = execution.startedAt().plusMinutes(runningTimeoutMinutes);
        if (!clock.now().isAfter(timeoutAt)) {
            return execution;
        }
        String reason = "任务执行超时: taskCode=" + execution.taskCode()
            + ", taskType=" + execution.taskType()
            + ", startedAt=" + execution.startedAt()
            + ", timeoutMinutes=" + runningTimeoutMinutes
            + ", 可能是远程模型、Kafka消费或外部采集接口未返回";
        return executions.save(execution.toBuilder()
            .status("FAILED")
            .failureReason(reason)
            .completedAt(clock.now())
            .build());
    }

    /** 分页查询已采集的投资资讯。 */
    @Transactional(readOnly = true)
    public PageResult<NewsArticle> articles(String keyword, String articleType, String sourceCode,
                                            String languageCode, LocalDateTime publishFrom,
                                            LocalDateTime publishTo, PageQuery pageQuery) {
        return articles.search(new NewsArticleSearchCriteria(
            keyword, articleType, sourceCode, languageCode, publishFrom, publishTo,
            pageQuery.page(), pageQuery.size(),
            pageQuery.safeSort(ARTICLE_SORTS, "publishTime"),
            "asc".equals(pageQuery.direction())
        ));
    }

    /** 分页查询投资方向收益、动量和资讯热度快照。 */
    @Transactional(readOnly = true)
    public PageResult<InvestmentThemeSnapshot> snapshots(String taskCode, String snapshotType,
                                                         String themeCode, String marketScope,
                                                         LocalDateTime snapshotFrom,
                                                         LocalDateTime snapshotTo,
                                                         PageQuery pageQuery) {
        return snapshots.search(new InvestmentThemeSnapshotSearchCriteria(
            taskCode, snapshotType, themeCode,
            marketScope == null || marketScope.isBlank() ? TaskParameterParser.CN_MAINLAND : marketScope,
            snapshotFrom, snapshotTo,
            pageQuery.page(), pageQuery.size(),
            pageQuery.safeSort(SNAPSHOT_SORTS, "snapshotTime"),
            "asc".equals(pageQuery.direction())
        ));
    }

    /**
     * 分页查询资讯与主题、产品的显式关联。
     *
     * @param articleBizId 资讯业务 ID 筛选条件
     * @param themeCode 投资主题编码筛选条件
     * @param productCode 产品代码筛选条件
     * @param relationType 关联类型筛选条件
     * @param pageQuery 分页和排序参数
     * @return 资讯主题产品关联分页结果
     * @author dz
     * @date 2026-06-21
     */
    @Transactional(readOnly = true)
    public PageResult<NewsArticleRelation> articleRelations(
        String articleBizId,
        String themeCode,
        String productCode,
        String relationType,
        PageQuery pageQuery
    ) {
        return relations.search(new NewsArticleRelationSearchCriteria(
            articleBizId,
            themeCode,
            productCode,
            relationType,
            pageQuery.page(),
            pageQuery.size(),
            pageQuery.safeSort(RELATION_SORTS, "createdAt"),
            "asc".equals(pageQuery.direction())
        ));
    }

    /** 获取配置内的任务定义。 */
    private InvestmentTaskDefinition requiredDefinition(String taskCode) {
        return definitions.findByCode(taskCode)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "投资任务配置不存在"));
    }
}
