package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.model.task.ThemeProductPerformance;
import com.example.dzcom.domain.repository.market.MarketQuoteStore;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** 热门投资方向实时收益任务。 */
@Service
@RequiredArgsConstructor
public class HotThemeReturnTaskHandler implements InvestmentTaskHandler {
    private final MarketQuoteStore quotes;
    private final InvestmentThemeSnapshotStore snapshots;
    private final IdGenerator ids;
    private final ClockProvider clock;
    private final ObjectMapper objectMapper;

    /** 支持热门主题收益任务。 */
    @Override
    public boolean supports(String taskType) {
        return "HOT_THEME_RETURN".equals(taskType);
    }

    /** 计算每个配置主题在回看窗口内的平均收益并保存快照。 */
    @Override
    @Transactional
    public String execute(InvestmentTaskEvent event) {
        int windowMinutes = TaskParameterParser.positiveInt(
            event.parameters(), "windowMinutes", 1440);
        LocalDateTime now = clock.now();
        Map<String, List<String>> themes = TaskParameterParser.themes(event.parameters());
        themes.forEach((themeName, productCodes) ->
            saveSnapshot(event.taskCode(), "RETURN", themeName, windowMinutes,
                quotes.findPerformance(productCodes, now.minusMinutes(windowMinutes), now), now));
        return "已生成 " + themes.size() + " 个热门投资方向收益快照";
    }

    /** 保存单个主题收益快照。 */
    private void saveSnapshot(
        String taskCode,
        String snapshotType,
        String themeName,
        int windowMinutes,
        List<ThemeProductPerformance> performances,
        LocalDateTime now
    ) {
        BigDecimal average = performances.stream()
            .map(ThemeProductPerformance::returnRate)
            .filter(value -> value != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!performances.isEmpty()) {
            average = average.divide(
                BigDecimal.valueOf(performances.size()), 8, RoundingMode.HALF_UP);
        }
        ThemeProductPerformance top = performances.stream()
            .filter(item -> item.returnRate() != null)
            .max(Comparator.comparing(ThemeProductPerformance::returnRate))
            .orElse(null);
        snapshots.save(InvestmentThemeSnapshot.builder()
            .bizId(ids.newBizId())
            .taskCode(taskCode)
            .snapshotType(snapshotType)
            .themeCode(TaskParameterParser.themeCode(themeName))
            .themeName(themeName)
            .windowMinutes(windowMinutes)
            .sampleCount(performances.size())
            .returnRate(average)
            .topProductBizId(top == null ? null : top.productBizId())
            .metrics(writeMetrics(performances))
            .snapshotTime(now)
            .createdAt(now)
            .build());
    }

    /** 将可解释样本明细序列化为 JSON。 */
    private String writeMetrics(List<ThemeProductPerformance> performances) {
        try {
            return objectMapper.writeValueAsString(performances);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("主题收益指标序列化失败", exception);
        }
    }
}
