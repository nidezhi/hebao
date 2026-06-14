package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<UserEntity, String> {
    /**
     * 执行 find by biz id and deleted 处理。
     *
     * @param bizId 业务对象的唯一标识
     * @param deleted deleted 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<UserEntity> findByBizIdAndDeleted(String bizId, int deleted);

    /**
     * 根据查询条件获取业务数据列表。
     *
     * @param keyword 模糊查询关键字
     * @param status 目标状态或目标值
     * @param kycStatus kycStatus 参数
     * @param riskLevel riskLevel 参数
     * @param pageable pageable 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Query(
        value = """
            select u from UserEntity u
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
            """,
        countQuery = """
            select count(u) from UserEntity u
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
            """
    )
    Page<UserEntity> search(String keyword, Integer status, Integer kycStatus, Integer riskLevel, Pageable pageable);
}
