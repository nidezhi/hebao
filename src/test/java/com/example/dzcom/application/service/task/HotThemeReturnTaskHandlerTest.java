package com.example.dzcom.application.service.task;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;
import com.example.dzcom.domain.model.task.ThemeProductPerformance;
import com.example.dzcom.domain.repository.market.MarketQuoteStore;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotStore;
import com.example.dzcom.domain.repository.task.InvestmentThemeSnapshotSearchCriteria;
import com.example.dzcom.domain.model.market.MarketQuote;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 热门投资方向收益任务测试。 */
class HotThemeReturnTaskHandlerTest {

    /** 任务应按主题计算平均收益并保存快照。 */
    @Test
    void shouldCalculateAndSaveThemeReturn() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 15, 12, 0);
        List<ThemeProductPerformance> performances = List.of(
            ThemeProductPerformance.builder()
                .productBizId("p1").productCode("AAPL")
                .returnRate(new BigDecimal("0.10")).build(),
            ThemeProductPerformance.builder()
                .productBizId("p2").productCode("NVDA")
                .returnRate(new BigDecimal("0.20")).build()
        );
        CapturingSnapshotStore snapshots = new CapturingSnapshotStore();
        HotThemeReturnTaskHandler handler = new HotThemeReturnTaskHandler(
            new FixedPerformanceStore(performances),
            snapshots,
            new FixedIdGenerator(),
            () -> now,
            JsonMapper.builder().findAndAddModules().build()
        );

        String result = handler.execute(InvestmentTaskEvent.builder()
            .taskCode("hot-theme-return")
            .taskType("HOT_THEME_RETURN")
            .parameters(Map.of(
                "windowMinutes", "60",
                "themes", "AI人工智能=AAPL,NVDA"
            ))
            .build());

        assertEquals("已生成 1 个热门投资方向收益快照", result);
        assertEquals(0, new BigDecimal("0.15000000")
            .compareTo(snapshots.saved.returnRate()));
        assertEquals(2, snapshots.saved.sampleCount());
        assertEquals("p2", snapshots.saved.topProductBizId());
    }

    /** 固定收益数据的行情仓储假实现。 */
    private record FixedPerformanceStore(
        List<ThemeProductPerformance> performances
    ) implements MarketQuoteStore {
        @Override
        public MarketQuote savePoint(MarketQuote quote) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<MarketQuote> findLatest(
            String productBizId,
            String interval,
            String sourceCode
        ) {
            return Optional.empty();
        }

        @Override
        public List<MarketQuote> findHistory(
            String productBizId,
            String interval,
            String sourceCode,
            LocalDateTime from,
            LocalDateTime to,
            int limit
        ) {
            return List.of();
        }

        @Override
        public List<ThemeProductPerformance> findPerformance(
            List<String> productCodes,
            LocalDateTime from,
            LocalDateTime to
        ) {
            return performances;
        }
    }

    /** 捕获保存结果的快照仓储假实现。 */
    private static final class CapturingSnapshotStore
        implements InvestmentThemeSnapshotStore {
        private InvestmentThemeSnapshot saved;

        @Override
        public InvestmentThemeSnapshot save(InvestmentThemeSnapshot snapshot) {
            saved = snapshot;
            return snapshot;
        }

        @Override
        public PageResult<InvestmentThemeSnapshot> search(
            InvestmentThemeSnapshotSearchCriteria criteria
        ) {
            return PageResult.<InvestmentThemeSnapshot>builder()
                .items(List.of())
                .total(0)
                .page(criteria.page())
                .size(criteria.size())
                .totalPages(0)
                .build();
        }
    }

    /** 固定业务 ID 的生成器假实现。 */
    private static final class FixedIdGenerator implements IdGenerator {
        @Override
        public String newBizId() {
            return "snapshot-1";
        }

        @Override
        public String newUserNo() {
            throw new UnsupportedOperationException();
        }
    }
}
