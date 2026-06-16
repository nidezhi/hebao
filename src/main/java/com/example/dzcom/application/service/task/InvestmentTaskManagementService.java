package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.task.InvestmentTaskDefinitionView;
import com.example.dzcom.application.dto.task.InvestmentTaskTriggerResult;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.model.task.NewsArticle;
import com.example.dzcom.domain.model.task.ScheduledTaskExecution;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotSearchCriteria;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.example.dzcom.domain.repository.task.NewsArticleSearchCriteria;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import com.example.dzcom.domain.repository.task.ScheduledTaskExecutionSearchCriteria;
import com.example.dzcom.domain.repository.task.ScheduledTaskExecutionStore;
import com.example.dzcom.infrastructure.config.task.InvestmentTaskProperties;
import lombok.RequiredArgsConstructor;
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

    private final InvestmentTaskProperties properties;
    private final InvestmentTaskTriggerPort triggerPort;
    private final ScheduledTaskExecutionStore executions;
    private final NewsArticleStore articles;
    private final InvestmentThemeSnapshotStore snapshots;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /** 查询当前生效的投资任务配置。 */
    @Transactional(readOnly = true)
    public List<InvestmentTaskDefinitionView> definitions() {
        return properties.getDefinitions().stream()
            .map(definition -> InvestmentTaskDefinitionView.builder()
                .code(definition.getCode())
                .type(definition.getType())
                .cron(definition.getCron())
                .zone(definition.getZone())
                .enabled(definition.isEnabled())
                .parameters(new LinkedHashMap<>(definition.getParameters()))
                .build())
            .toList();
    }

    /** 手动触发一次配置内的投资任务。 */
    @Transactional
    public InvestmentTaskTriggerResult trigger(String taskCode, Map<String, String> overrides,
                                               String triggerSource) {
        InvestmentTaskProperties.TaskDefinition definition = requiredDefinition(taskCode);
        LocalDateTime now = clock.now();
        Map<String, String> parameters = new LinkedHashMap<>(definition.getParameters());
        if (overrides != null) {
            parameters.putAll(overrides);
        }
        InvestmentTaskEvent event = InvestmentTaskEvent.builder()
            .eventId(ids.newBizId())
            .taskCode(definition.getCode())
            .taskType(definition.getType())
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
    @Transactional(readOnly = true)
    public PageResult<ScheduledTaskExecution> executions(String taskCode, String taskType, String status,
                                                         LocalDateTime startedFrom,
                                                         LocalDateTime startedTo,
                                                         PageQuery pageQuery) {
        return executions.search(new ScheduledTaskExecutionSearchCriteria(
            taskCode, taskType, status, startedFrom, startedTo,
            pageQuery.page(), pageQuery.size(),
            pageQuery.safeSort(EXECUTION_SORTS, "startedAt"),
            "asc".equals(pageQuery.direction())
        ));
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
                                                         String themeCode, LocalDateTime snapshotFrom,
                                                         LocalDateTime snapshotTo,
                                                         PageQuery pageQuery) {
        return snapshots.search(new InvestmentThemeSnapshotSearchCriteria(
            taskCode, snapshotType, themeCode, snapshotFrom, snapshotTo,
            pageQuery.page(), pageQuery.size(),
            pageQuery.safeSort(SNAPSHOT_SORTS, "snapshotTime"),
            "asc".equals(pageQuery.direction())
        ));
    }

    /** 获取配置内的任务定义。 */
    private InvestmentTaskProperties.TaskDefinition requiredDefinition(String taskCode) {
        return properties.getDefinitions().stream()
            .filter(definition -> definition.getCode().equals(taskCode))
            .findFirst()
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "投资任务配置不存在"));
    }
}
