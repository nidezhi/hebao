package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.enums.account.AccountStatus;
import com.example.dzcom.domain.model.account.User;
import com.example.dzcom.domain.repository.account.UserSearchCriteria;
import com.example.dzcom.domain.repository.account.UserStore;
import com.example.dzcom.infrastructure.persistence.entity.account.UserEntity;
import com.example.dzcom.infrastructure.persistence.mapper.account.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户主表仓储实现，直接负责用户查询、分页、保存和领域转换。
 */
@Repository
@RequiredArgsConstructor
public class UserStoreImpl implements UserStore {
    /** MyBatis 用户执行器。 */
    private final UserMapper mapper;

    /**
     * 保存用户主体。
     *
     * @param value 用户领域对象
     * @return 保存后的用户
     */
    @Override
    public User save(User value) {
        UserEntity existing = mapper.selectById(value.getBizId());
        UserEntity entity = Optional.ofNullable(existing)
            .map(UserEntity::toBuilder)
            .orElseGet(UserEntity::builder)
            .bizId(value.getBizId())
            .userNo(value.getUserNo())
            .status(value.getStatus().code())
            .version(value.getVersion())
            .registeredAt(value.getRegisteredAt())
            .lastLoginAt(value.getLastLoginAt())
            .createdAt(value.getCreatedAt())
            .updatedAt(value.getUpdatedAt())
            .deleted(value.getDeleted())
            .deletedAt(value.getDeletedAt())
            .build();
        mapper.save(entity);
        return toDomain(entity);
    }

    /**
     * 根据业务标识查询未删除用户。
     *
     * @param bizId 用户业务标识
     * @return 用户领域对象
     */
    @Override
    public Optional<User> findByBizId(String bizId) {
        return Optional.ofNullable(mapper.selectActiveByBizId(bizId))
            .map(this::toDomain);
    }

    /**
     * 根据筛选条件分页查询用户。
     *
     * @param criteria 用户筛选和分页条件
     * @return 用户分页结果
     */
    @Override
    public PageResult<User> search(UserSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<User> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        int totalPages = (int) Math.ceil((double) total / criteria.size());
        return PageResult.<User>builder()
            .items(items)
            .total(total)
            .page(criteria.page())
            .size(criteria.size())
            .totalPages(totalPages)
            .build();
    }

    /**
     * 将接口排序字段转换为固定数据库列，避免动态 SQL 注入。
     *
     * @param sort 接口排序字段
     * @return 数据库排序列
     */
    private String resolveSortColumn(String sort) {
        return switch (sort) {
            case "userNo" -> "u.user_no";
            case "status" -> "u.status";
            case "lastLoginAt" -> "u.last_login_at";
            default -> "u.created_at";
        };
    }

    /**
     * 将用户实体转换为领域对象。
     *
     * @param entity 用户实体
     * @return 用户领域对象
     */
    private User toDomain(UserEntity entity) {
        return User.builder()
            .bizId(entity.getBizId())
            .userNo(entity.getUserNo())
            .status(AccountStatus.fromCode(entity.getStatus()))
            .version(entity.getVersion())
            .registeredAt(entity.getRegisteredAt())
            .lastLoginAt(entity.getLastLoginAt())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .deleted(entity.getDeleted())
            .deletedAt(entity.getDeletedAt())
            .build();
    }

}
