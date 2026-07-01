package com.example.dzcom.application.service.ai;

import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.AiModelCallAuditContext;
import com.example.dzcom.application.dto.ai.AiModelCallAuditView;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.domain.model.ai.AiModelCallAudit;
import com.example.dzcom.domain.repository.ai.AiModelCallAuditSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiModelCallAuditStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** AI 模型调用审计应用服务测试。 */
class AiModelCallAuditApplicationServiceTest {
    @Test
    void shouldCreateAndUpdateCallAudit() {
        MemoryAuditStore store = new MemoryAuditStore();
        AiModelCallAuditApplicationService service = new AiModelCallAuditApplicationService(
            store,
            new IncrementalIds(),
            () -> LocalDateTime.of(2026, 7, 1, 10, 0)
        );

        service.start(
            "call-1",
            "AI_STRUCTURED_DATA_COLLECTION",
            modelConfig(),
            "https://example.test/v1/chat/completions",
            "system-hash",
            "user-hash",
            "system",
            "user",
            AiModelCallAuditContext.builder()
                .businessType("TASK_EVENT")
                .businessBizId("event-1")
                .skillCode("AI_STRUCTURED_DATA_COLLECTION_CORE")
                .inputSummary(Map.of("maxNews", 10))
                .build()
        );
        service.succeed("call-1", 200, 1234, "response-hash", "{\"ok\":true}", Map.of("contentLength", 11));

        PageResult<AiModelCallAuditView> page = service.list(
            null, "SUCCEEDED", null, null, null, null, null,
            null, null, null, null, null, null, null, null, null,
            new PageQuery(1, 20, "createdAt", "desc")
        );

        assertEquals(1, page.total());
        AiModelCallAuditView view = page.items().get(0);
        assertEquals("call-1", view.callId());
        assertEquals("SUCCEEDED", view.callStatus());
        assertEquals("TASK_EVENT", view.businessType());
        assertEquals("AI_STRUCTURED_DATA_COLLECTION_CORE", view.skillCode());
        assertEquals(1234, view.durationMs());
        assertTrue(view.inputSummary().contains("maxNews"));
        assertTrue(view.outputSummary().contains("contentLength"));
        assertNotNull(view.requestPreview());
    }

    @Test
    void shouldKeepFullPayloadWhenPreviewIsTruncated() {
        MemoryAuditStore store = new MemoryAuditStore();
        AiModelCallAuditApplicationService service = new AiModelCallAuditApplicationService(
            store,
            new IncrementalIds(),
            () -> LocalDateTime.of(2026, 7, 1, 10, 0)
        );
        String longPrompt = "配置".repeat(4500);
        String longResponse = "{\"result\":\"" + "输出".repeat(4500) + "\"}";

        service.start(
            "call-long",
            "AUTO_INVESTMENT_REPORT_GENERATION",
            modelConfig(),
            "https://example.test/v1/chat/completions",
            "system-hash",
            "user-hash",
            "system",
            longPrompt,
            AiModelCallAuditContext.empty()
        );
        service.succeed("call-long", 200, 2000, "response-hash", longResponse, Map.of());

        AiModelCallAuditView view = service.detail("audit-1");

        assertTrue(view.requestPreview().endsWith("..."));
        assertTrue(view.responsePreview().endsWith("..."));
        assertTrue(view.requestPayload().contains(longPrompt));
        assertEquals(longResponse, view.responsePayload());
    }

    private AiModelRuntimeConfig modelConfig() {
        return AiModelRuntimeConfig.builder()
            .modelCode("openai-compatible-analysis")
            .modelVersion("default-v1")
            .providerCode("OPENAI_COMPATIBLE")
            .baseUrl("https://example.test/v1")
            .remoteModel("qwen-plus")
            .secretRef("OPENAI_API_KEY")
            .apiKey("secret")
            .timeoutSeconds(60)
            .maxTokens(4096)
            .temperature(new BigDecimal("0.2"))
            .mockEnabled(false)
            .build();
    }

    /** 内存审计仓储。 */
    private static final class MemoryAuditStore implements AiModelCallAuditStore {
        private final List<AiModelCallAudit> items = new ArrayList<>();

        @Override
        public AiModelCallAudit save(AiModelCallAudit audit) {
            items.removeIf(item -> item.bizId().equals(audit.bizId()));
            items.add(audit);
            return audit;
        }

        @Override
        public Optional<AiModelCallAudit> findByBizId(String bizId) {
            return items.stream().filter(item -> item.bizId().equals(bizId)).findFirst();
        }

        @Override
        public Optional<AiModelCallAudit> findByCallId(String callId) {
            return items.stream().filter(item -> item.callId().equals(callId)).findFirst();
        }

        @Override
        public PageResult<AiModelCallAudit> search(AiModelCallAuditSearchCriteria criteria) {
            List<AiModelCallAudit> matched = items.stream()
                .filter(item -> criteria.callStatus() == null || criteria.callStatus().equals(item.callStatus()))
                .sorted(Comparator.comparing(AiModelCallAudit::createdAt).reversed())
                .toList();
            return PageResult.<AiModelCallAudit>builder()
                .items(matched)
                .total(matched.size())
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(1)
                .build();
        }
    }

    /** 递增测试 ID 生成器。 */
    private static final class IncrementalIds implements IdGenerator {
        private final AtomicInteger next = new AtomicInteger(1);

        @Override
        public String newBizId() {
            return "audit-" + next.getAndIncrement();
        }

        @Override
        public String newUserNo() {
            return "U" + next.getAndIncrement();
        }
    }
}
