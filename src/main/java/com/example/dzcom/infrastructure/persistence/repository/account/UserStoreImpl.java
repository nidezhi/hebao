package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.common.page.PageResult;
import com.example.dzcom.domain.enums.account.AccountStatus;
import com.example.dzcom.domain.model.account.User;
import com.example.dzcom.domain.repository.account.UserSearchCriteria;
import com.example.dzcom.domain.repository.account.UserStore;
import com.example.dzcom.infrastructure.persistence.entity.account.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
    /** 用户筛选查询的公共条件。 */
    private static final String SEARCH_CONDITION = """
        from UserEntity u
        where u.deleted = 0
          and (:status is null or u.status = :status)
          and (:keyword is null or lower(u.userNo) like lower(concat('%', :keyword, '%'))
               or exists (select i.bizId from UserIdentityEntity i
                          where i.userBizId = u.bizId and i.deleted = 0
                            and lower(i.identityValue) like lower(concat('%', :keyword, '%'))))
          and (:kycStatus is null or exists (select r.bizId from UserRiskProfileEntity r
                                             where r.userBizId = u.bizId and r.deleted = 0
                                               and r.kycStatus = :kycStatus))
          and (:riskLevel is null or exists (select r.bizId from UserRiskProfileEntity r
                                             where r.userBizId = u.bizId and r.deleted = 0
                                               and r.riskLevel = :riskLevel))
        """;

    /** JPA 实体管理器。 */
    private final EntityManager entityManager;

    /**
     * 保存用户主体。
     *
     * @param value 用户领域对象
     * @return 保存后的用户
     */
    @Override
    public User save(User value) {
        UserEntity existing = entityManager.find(UserEntity.class, value.getBizId());
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
        return toDomain(entityManager.merge(entity));
    }

    /**
     * 根据业务标识查询未删除用户。
     *
     * @param bizId 用户业务标识
     * @return 用户领域对象
     */
    @Override
    public Optional<User> findByBizId(String bizId) {
        return entityManager.createQuery("""
                select u from UserEntity u
                where u.bizId = :bizId and u.deleted = 0
                """, UserEntity.class)
            .setParameter("bizId", bizId)
            .getResultStream()
            .findFirst()
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
        String direction = criteria.ascending() ? "asc" : "desc";
        TypedQuery<UserEntity> dataQuery = entityManager.createQuery(
            "select u " + SEARCH_CONDITION + " order by u." + criteria.sort() + " " + direction,
            UserEntity.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(
            "select count(u) " + SEARCH_CONDITION, Long.class);
        bindSearchParameters(dataQuery, criteria);
        bindSearchParameters(countQuery, criteria);
        List<User> items = dataQuery
            .setFirstResult((criteria.page() - 1) * criteria.size())
            .setMaxResults(criteria.size())
            .getResultStream()
            .map(this::toDomain)
            .toList();
        long total = countQuery.getSingleResult();
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
     * 为用户分页查询绑定公共参数。
     *
     * @param query JPA 查询对象
     * @param criteria 用户筛选条件
     */
    private void bindSearchParameters(TypedQuery<?> query, UserSearchCriteria criteria) {
        query.setParameter("keyword", blankToNull(criteria.keyword()));
        query.setParameter("status", criteria.status() == null ? null : criteria.status().code());
        query.setParameter("kycStatus", criteria.kycStatus() == null ? null : criteria.kycStatus().code());
        query.setParameter("riskLevel", criteria.riskLevel());
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

    /**
     * 将空白查询值转换为 null。
     *
     * @param value 原始查询值
     * @return 标准化查询值
     */
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
