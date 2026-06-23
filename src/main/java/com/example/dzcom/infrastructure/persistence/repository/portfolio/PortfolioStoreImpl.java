package com.example.dzcom.infrastructure.persistence.repository.portfolio;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.portfolio.Portfolio;
import com.example.dzcom.domain.repository.portfolio.PortfolioSearchCriteria;
import com.example.dzcom.domain.repository.portfolio.PortfolioStore;
import com.example.dzcom.infrastructure.persistence.entity.portfolio.PortfolioEntity;
import com.example.dzcom.infrastructure.persistence.mapper.portfolio.PortfolioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** 投资组合仓储实现。 */
@Repository
@RequiredArgsConstructor
public class PortfolioStoreImpl implements PortfolioStore {
    private final PortfolioMapper mapper;

    /**
     * 保存投资组合。
     *
     * @param portfolio 投资组合领域对象
     * @return 保存后的投资组合
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public Portfolio save(Portfolio portfolio) {
        PortfolioEntity entity = toEntity(portfolio);
        mapper.save(entity);
        return toDomain(entity);
    }

    /**
     * 根据业务标识查询未删除组合。
     *
     * @param bizId 组合业务唯一标识
     * @return 组合领域对象
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public Optional<Portfolio> findByBizId(String bizId) {
        return Optional.ofNullable(mapper.selectActiveByBizId(bizId))
            .map(this::toDomain);
    }

    /**
     * 分页查询当前用户的模拟组合。
     *
     * @param criteria 组合查询条件
     * @return 组合分页结果
     * @author dz
     * @date 2026-06-23
     */
    @Override
    public PageResult<Portfolio> search(PortfolioSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<Portfolio> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<Portfolio>builder()
            .items(items)
            .total(total)
            .page(criteria.page())
            .size(criteria.size())
            .totalPages((int) Math.ceil((double) total / criteria.size()))
            .build();
    }

    /**
     * 将接口排序字段转换为数据库列。
     *
     * @param sort 排序字段
     * @return 数据库列
     * @author dz
     * @date 2026-06-23
     */
    private String resolveSortColumn(String sort) {
        return switch (sort) {
            case "updatedAt" -> "p.updated_at";
            case "portfolioNo" -> "p.portfolio_no";
            case "portfolioName" -> "p.portfolio_name";
            default -> "p.created_at";
        };
    }

    /**
     * 将组合领域对象转换为持久化实体。
     *
     * @param portfolio 组合领域对象
     * @return 组合持久化实体
     * @author dz
     * @date 2026-06-23
     */
    private PortfolioEntity toEntity(Portfolio portfolio) {
        return PortfolioEntity.builder()
            .bizId(portfolio.bizId())
            .portfolioNo(portfolio.portfolioNo())
            .ownerUserBizId(portfolio.ownerUserBizId())
            .portfolioName(portfolio.portfolioName())
            .portfolioType(portfolio.portfolioType())
            .baseCurrency(portfolio.baseCurrency())
            .status(portfolio.status())
            .version(portfolio.version())
            .createdAt(portfolio.createdAt())
            .updatedAt(portfolio.updatedAt())
            .createdBy(portfolio.createdBy())
            .updatedBy(portfolio.updatedBy())
            .deleted(portfolio.deleted())
            .deletedAt(portfolio.deletedAt())
            .build();
    }

    /**
     * 将持久化实体转换为组合领域对象。
     *
     * @param entity 组合持久化实体
     * @return 组合领域对象
     * @author dz
     * @date 2026-06-23
     */
    private Portfolio toDomain(PortfolioEntity entity) {
        return Portfolio.builder()
            .bizId(entity.getBizId())
            .portfolioNo(entity.getPortfolioNo())
            .ownerUserBizId(entity.getOwnerUserBizId())
            .portfolioName(entity.getPortfolioName())
            .portfolioType(entity.getPortfolioType())
            .baseCurrency(entity.getBaseCurrency())
            .status(entity.getStatus())
            .version(entity.getVersion())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .deleted(entity.getDeleted())
            .deletedAt(entity.getDeletedAt())
            .build();
    }
}
