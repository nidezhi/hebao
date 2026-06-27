package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.market.DataQualitySnapshot;
import com.example.dzcom.domain.model.market.DataSource;
import com.example.dzcom.domain.model.market.DataSourceHealth;
import com.example.dzcom.domain.repository.market.DataSourceStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 真实数据采集任务的共享支撑逻辑。 */
@Component
@RequiredArgsConstructor
public class RealDataTaskSupport {
    private final DataSourceStore sources;
    private final IdGenerator ids;
    private final ObjectMapper objectMapper;

    /** 从任务参数解析唯一产品代码集合。 */
    public List<String> productCodes(Map<String, String> parameters) {
        LinkedHashSet<String> codes = new LinkedHashSet<>();
        TaskParameterParser.themes(parameters).values().forEach(codes::addAll);
        TaskParameterParser.list(parameters, "productCodes").forEach(codes::add);
        return codes.stream()
            .map(value -> value.trim().toUpperCase(Locale.ROOT))
            .filter(value -> !value.isBlank())
            .toList();
    }

    /** 从任务参数解析关键词集合。 */
    public List<String> keywords(Map<String, String> parameters) {
        List<String> result = new ArrayList<>(TaskParameterParser.list(parameters, "keywords"));
        if (result.isEmpty()) {
            result.addAll(List.of("AI", "人工智能", "算力", "大模型", "半导体", "芯片", "集成电路", "黄金", "金价", "贵金属"));
        }
        return result.stream().map(String::trim).filter(value -> !value.isBlank()).toList();
    }

    /** 保存或刷新数据源注册信息。 */
    public void ensureSource(
        String sourceCode,
        String sourceName,
        String sourceType,
        String trustLevel,
        String baseUrl,
        String fetchFrequency,
        String description,
        LocalDateTime now
    ) {
        DataSource existing = sources.findBySourceCode(sourceCode).orElse(null);
        sources.save(DataSource.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .sourceCode(sourceCode)
            .sourceName(sourceName)
            .sourceType(sourceType)
            .trustLevel(trustLevel)
            .baseUrl(baseUrl)
            .enabled(true)
            .fetchFrequency(fetchFrequency)
            .owner("DETERMINISTIC_COLLECTOR")
            .description(description)
            .createdAt(existing == null ? now : existing.createdAt())
            .updatedAt(now)
            .createdBy(existing == null ? "REAL_DATA_COLLECTOR" : existing.createdBy())
            .updatedBy("REAL_DATA_COLLECTOR")
            .build());
    }

    /** 保存数据源健康状态。 */
    public void saveHealth(String sourceCode, int sampleCount, String failureReason, LocalDateTime now) {
        DataSourceHealth existing = sources.findHealthBySourceCode(sourceCode).orElse(null);
        boolean success = sampleCount > 0;
        sources.saveHealth(DataSourceHealth.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .sourceCode(sourceCode)
            .lastSuccessAt(success ? now : existing == null ? null : existing.lastSuccessAt())
            .lastFailureAt(success ? existing == null ? null : existing.lastFailureAt() : now)
            .successRate(success ? BigDecimal.ONE : BigDecimal.ZERO)
            .avgLatencyMs(null)
            .failureReason(success ? null : limit(failureReason, 1000))
            .sampleCount(sampleCount)
            .updatedAt(now)
            .build());
    }

    /** 保存数据质量快照。 */
    public void saveQuality(
        String sourceCode,
        String dataType,
        int expectedCount,
        int sampleCount,
        int missingCount,
        int duplicateCount,
        BigDecimal freshnessScore,
        Map<String, Object> detail,
        LocalDateTime now
    ) {
        BigDecimal missingRate = ratio(missingCount, Math.max(expectedCount, 1));
        BigDecimal duplicateRate = ratio(duplicateCount, Math.max(sampleCount + duplicateCount, 1));
        BigDecimal qualityScore = sampleCount <= 0
            ? BigDecimal.ZERO
            : BigDecimal.ONE
                .subtract(missingRate).max(BigDecimal.ZERO)
                .multiply(BigDecimal.ONE.subtract(duplicateRate).max(BigDecimal.ZERO))
                .multiply(freshnessScore == null ? BigDecimal.ONE : freshnessScore)
                .setScale(4, RoundingMode.HALF_UP);
        sources.saveQualitySnapshot(DataQualitySnapshot.builder()
            .bizId(ids.newBizId())
            .sourceCode(sourceCode)
            .dataType(dataType)
            .qualityScore(qualityScore)
            .missingRate(missingRate)
            .duplicateRate(duplicateRate)
            .freshnessScore(freshnessScore == null ? BigDecimal.ONE : freshnessScore)
            .sampleCount(sampleCount)
            .snapshotTime(now)
            .detail(json(detail))
            .createdAt(now)
            .build());
    }

    public BigDecimal ratio(int numerator, int denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
            .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP)
            .max(BigDecimal.ZERO)
            .min(BigDecimal.ONE);
    }

    public String json(Map<String, Object> detail) {
        try {
            return objectMapper.writeValueAsString(detail == null ? Map.of() : detail);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    public String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    public Map<String, Object> detail(Object... values) {
        Map<String, Object> detail = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            detail.put(String.valueOf(values[i]), values[i + 1]);
        }
        return detail;
    }
}
