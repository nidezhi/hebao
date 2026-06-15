package com.example.dzcom.infrastructure.persistence.repository.market;

import com.example.dzcom.domain.enums.market.QuoteStatus;
import com.example.dzcom.domain.model.market.MarketQuote;
import com.example.dzcom.domain.model.task.ThemeProductPerformance;
import com.example.dzcom.domain.repository.market.MarketQuoteStore;
import com.example.dzcom.infrastructure.persistence.entity.market.MarketQuoteEntity;
import com.example.dzcom.infrastructure.persistence.mapper.market.MarketQuoteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 行情仓储实现，直接维护行情点的保存、最新值查询和历史查询。
 */
@Repository
@RequiredArgsConstructor
public class MarketQuoteStoreImpl implements MarketQuoteStore {
    /** MyBatis 行情执行器。 */
    private final MarketQuoteMapper mapper;

    /**
     * 保存或修正唯一行情点。
     *
     * @param value 行情领域对象
     * @return 保存后的行情
     */
    @Override
    public MarketQuote savePoint(MarketQuote value) {
        Optional<MarketQuoteEntity> existing = findEntity(
            value.productBizId(), value.sourceCode(), value.interval(), value.quoteTime());
        MarketQuoteEntity entity = existing
            .map(MarketQuoteEntity::toBuilder)
            .orElseGet(MarketQuoteEntity::builder)
            .bizId(existing.map(MarketQuoteEntity::getBizId).orElse(value.bizId()))
            .productBizId(value.productBizId())
            .sourceCode(value.sourceCode())
            .quoteInterval(value.interval())
            .quoteTime(value.quoteTime())
            .openPrice(value.openPrice())
            .highPrice(value.highPrice())
            .lowPrice(value.lowPrice())
            .closePrice(value.closePrice())
            .previousClosePrice(value.previousClosePrice())
            .volume(value.volume())
            .turnoverAmount(value.turnoverAmount())
            .quoteStatus(existing.isPresent() && value.status() == QuoteStatus.VALID
                ? QuoteStatus.CORRECTED.code() : value.status().code())
            .receivedAt(value.receivedAt())
            .createdAt(existing.map(MarketQuoteEntity::getCreatedAt).orElse(value.createdAt()))
            .build();
        mapper.save(entity);
        return toDomain(entity);
    }

    /**
     * 查询指定产品、周期和数据源的最新有效行情。
     *
     * @param productBizId 产品业务标识
     * @param interval 行情周期
     * @param sourceCode 数据源代码
     * @return 最新行情
     */
    @Override
    public Optional<MarketQuote> findLatest(String productBizId, String interval, String sourceCode) {
        return Optional.ofNullable(mapper.selectLatest(productBizId, interval, sourceCode))
            .map(this::toDomain);
    }

    /**
     * 查询指定时间范围内的有效行情历史。
     *
     * @param productBizId 产品业务标识
     * @param interval 行情周期
     * @param sourceCode 数据源代码
     * @param from 开始时间
     * @param to 结束时间
     * @param limit 最大返回数量
     * @return 行情历史列表
     */
    @Override
    public List<MarketQuote> findHistory(String productBizId, String interval, String sourceCode,
                                         LocalDateTime from, LocalDateTime to, int limit) {
        return mapper.selectHistory(productBizId, interval, sourceCode, from, to, limit)
            .stream()
            .map(this::toDomain)
            .toList();
    }

    /**
     * 查询一组产品在指定窗口内的收益表现。
     *
     * @param productCodes 产品编码列表
     * @param from 窗口开始时间
     * @param to 窗口结束时间
     * @return 产品收益表现
     */
    @Override
    public List<ThemeProductPerformance> findPerformance(
        List<String> productCodes,
        LocalDateTime from,
        LocalDateTime to
    ) {
        if (productCodes == null || productCodes.isEmpty()) {
            return List.of();
        }
        return mapper.selectPerformance(productCodes, from, to).stream()
            .map(row -> ThemeProductPerformance.builder()
                .productBizId(row.getProductBizId())
                .productCode(row.getProductCode())
                .productName(row.getProductName())
                .startPrice(row.getStartPrice())
                .endPrice(row.getEndPrice())
                .returnRate(row.getReturnRate())
                .build())
            .toList();
    }

    /**
     * 根据行情唯一业务键查询行情实体。
     *
     * @param productBizId 产品业务标识
     * @param sourceCode 数据源代码
     * @param interval 行情周期
     * @param quoteTime 行情时间
     * @return 行情实体
     */
    private Optional<MarketQuoteEntity> findEntity(String productBizId, String sourceCode,
                                                   String interval, LocalDateTime quoteTime) {
        return Optional.ofNullable(mapper.selectByUniqueKey(
            productBizId, sourceCode, interval, quoteTime));
    }

    /**
     * 将行情实体转换为领域对象。
     *
     * @param entity 行情实体
     * @return 行情领域对象
     */
    private MarketQuote toDomain(MarketQuoteEntity entity) {
        return MarketQuote.builder()
            .bizId(entity.getBizId())
            .productBizId(entity.getProductBizId())
            .sourceCode(entity.getSourceCode())
            .interval(entity.getQuoteInterval())
            .quoteTime(entity.getQuoteTime())
            .openPrice(entity.getOpenPrice())
            .highPrice(entity.getHighPrice())
            .lowPrice(entity.getLowPrice())
            .closePrice(entity.getClosePrice())
            .previousClosePrice(entity.getPreviousClosePrice())
            .volume(entity.getVolume())
            .turnoverAmount(entity.getTurnoverAmount())
            .status(QuoteStatus.fromCode(entity.getQuoteStatus()))
            .receivedAt(entity.getReceivedAt())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
