package com.example.dzcom.application.service.market;

import com.example.dzcom.application.assembler.market.MarketQuoteViewAssembler;
import com.example.dzcom.application.command.market.SaveMarketQuoteCommand;
import com.example.dzcom.application.dto.market.MarketQuoteView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.common.exception.BusinessException;
import com.example.dzcom.common.service.ClockProvider;
import com.example.dzcom.common.service.IdGenerator;
import com.example.dzcom.domain.enums.market.QuoteStatus;
import com.example.dzcom.domain.model.market.MarketQuote;
import com.example.dzcom.domain.repository.market.MarketQuoteStore;
import com.example.dzcom.domain.repository.product.ProductStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * 行情写入与查询用例。
 *
 * <p>当前 MVP 使用同步写入和 MySQL 查询。应用接口保持存储无关，未来迁移到时序库时，
 * Controller 和领域规则无需感知底层变化。</p>
 */
@Service
@RequiredArgsConstructor
public class MarketQuoteApplicationService {
    private final MarketQuoteStore quotes;
    private final ProductStore products;
    private final MarketQuoteViewAssembler assembler;
    private final CurrentOperatorProvider currentOperator;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /**
     * 管理员或受信行情采集入口写入一个行情点。当前尚未单设数据源角色，因此要求 ADMIN。
     *
     * @param command 应用用例命令
     * @return 方法执行后的结果
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public MarketQuoteView save(SaveMarketQuoteCommand command) {
        requireAdmin();
        if (products.findByBizId(command.productBizId()).isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "行情关联产品不存在");
        }
        LocalDateTime now = clock.now();
        MarketQuote quote = MarketQuote.builder()
            .bizId(ids.newBizId())
            .productBizId(command.productBizId())
            .sourceCode(normalize(command.sourceCode()))
            .interval(normalize(command.interval()))
            .quoteTime(command.quoteTime())
            .openPrice(command.openPrice())
            .highPrice(command.highPrice())
            .lowPrice(command.lowPrice())
            .closePrice(command.closePrice())
            .previousClosePrice(command.previousClosePrice())
            .volume(command.volume())
            .turnoverAmount(command.turnoverAmount())
            .status(command.status() == null ? QuoteStatus.VALID : command.status())
            .receivedAt(now)
            .createdAt(now)
            .build();
        return assembler.assemble(quotes.savePoint(quote));
    }

    /**
     * 查询指定产品和周期的最新有效行情，sourceCode 为空时跨数据源取最新点。
     *
     * @param productBizId 业务对象的唯一标识
     * @param interval interval 参数
     * @param sourceCode sourceCode 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    @Transactional(readOnly = true)
    public MarketQuoteView latest(String productBizId, String interval, String sourceCode) {
        requireProduct(productBizId);
        return quotes.findLatest(productBizId, normalize(interval), normalizeNullable(sourceCode))
            .map(assembler::assemble)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "暂无有效行情"));
    }

    /**
     * 查询升序历史行情。 限制最大返回 1000 点，防止把高频行情接口误用为无边界数据导出。
     *
     * @param productBizId 业务对象的唯一标识
     * @param interval interval 参数
     * @param sourceCode sourceCode 参数
     * @param from from 参数
     * @param to to 参数
     * @param limit 结果数量限制
     * @return 方法执行后的结果
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    @Transactional(readOnly = true)
    public List<MarketQuoteView> history(String productBizId, String interval, String sourceCode,
                                         LocalDateTime from, LocalDateTime to, int limit) {
        requireProduct(productBizId);
        if (from == null || to == null || to.isBefore(from)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "行情时间范围无效");
        }
        if (limit < 1 || limit > 1000) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "limit 必须在 1 到 1000 之间");
        }
        return quotes.findHistory(productBizId, normalize(interval), normalizeNullable(sourceCode),
                from, to, limit)
            .stream().map(assembler::assemble).toList();
    }

    /**
     * 执行 require product 处理。
     *
     * @param productBizId 业务对象的唯一标识
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private void requireProduct(String productBizId) {
        if (products.findByBizId(productBizId).isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "产品不存在");
        }
    }

    /**
     * 执行 require admin 处理。
     *
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private void requireAdmin() {
        CurrentOperator operator = currentOperator.required();
        if (!operator.hasRole("ADMIN")) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }
    }

    /**
     * 规范化输入值并返回统一格式。
     *
     * @param value 待处理的数据值
     * @return 方法执行后的结果
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "编码不能为空");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 规范化输入值并返回统一格式。
     *
     * @param value 待处理的数据值
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : normalize(value);
    }
}
