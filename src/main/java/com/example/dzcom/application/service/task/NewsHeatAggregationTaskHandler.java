package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.example.dzcom.domain.repository.task.NewsArticleStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 投资资讯主题热度汇总任务。 */
@Service
@RequiredArgsConstructor
public class NewsHeatAggregationTaskHandler implements InvestmentTaskHandler {
    private final NewsArticleStore articles;
    private final InvestmentThemeSnapshotStore snapshots;
    private final IdGenerator ids;
    private final ClockProvider clock;
    private final ObjectMapper objectMapper;

    /** 支持资讯热度汇总任务。 */
    @Override
    public boolean supports(String taskType) {
        return "NEWS_HEAT_AGGREGATION".equals(taskType);
    }

    /** 按主题关键词统计窗口内资讯数量并生成热度快照。 */
    @Override
    @Transactional
    public String execute(InvestmentTaskEvent event) {
        int windowMinutes = TaskParameterParser.positiveInt(
            event.parameters(), "windowMinutes", 1440);
        String marketScope = TaskParameterParser.marketScope(event.parameters());
        LocalDateTime now = clock.now();
        Map<String, List<String>> themes = TaskParameterParser.themes(event.parameters());
        themes.forEach((themeName, keywords) -> {
            long count = articles.countByKeywords(keywords, now.minusMinutes(windowMinutes));
            snapshots.save(InvestmentThemeSnapshot.builder()
                .bizId(ids.newBizId())
                .taskCode(event.taskCode())
                .snapshotType("NEWS_HEAT")
                .themeCode(TaskParameterParser.themeCode(themeName))
                .themeName(themeName)
                .marketScope(marketScope)
                .windowMinutes(windowMinutes)
                .sampleCount(Math.toIntExact(count))
                .heatScore(BigDecimal.valueOf(count))
                .metrics(writeMetrics(keywords, count))
                .snapshotTime(now)
                .createdAt(now)
                .build());
        });
        return "已汇总 " + themes.size() + " 个投资方向的资讯热度";
    }

    /** 序列化热度计算依据。 */
    private String writeMetrics(List<String> keywords, long count) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                "keywords", keywords,
                "articleCount", count
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("资讯热度指标序列化失败", exception);
        }
    }
}
