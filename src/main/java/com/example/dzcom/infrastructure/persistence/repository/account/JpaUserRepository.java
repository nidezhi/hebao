package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByBizIdAndDeletedFalse(String bizId);

    @Query(
        value = """
            select u from UserEntity u
            where u.deleted = false
              and (:status is null or u.status = :status)
              and (:keyword is null or lower(u.userNo) like lower(concat('%', :keyword, '%'))
                   or exists (select i.bizId from UserIdentityEntity i
                              where i.userBizId = u.bizId and i.deleted = false
                                and lower(i.identityValue) like lower(concat('%', :keyword, '%'))))
              and (:kycStatus is null or exists (select r.bizId from UserRiskProfileEntity r
                                                 where r.userBizId = u.bizId and r.deleted = false
                                                   and r.kycStatus = :kycStatus))
              and (:riskLevel is null or exists (select r.bizId from UserRiskProfileEntity r
                                                 where r.userBizId = u.bizId and r.deleted = false
                                                   and r.riskLevel = :riskLevel))
            """,
        countQuery = """
            select count(u) from UserEntity u
            where u.deleted = false
              and (:status is null or u.status = :status)
              and (:keyword is null or lower(u.userNo) like lower(concat('%', :keyword, '%'))
                   or exists (select i.bizId from UserIdentityEntity i
                              where i.userBizId = u.bizId and i.deleted = false
                                and lower(i.identityValue) like lower(concat('%', :keyword, '%'))))
              and (:kycStatus is null or exists (select r.bizId from UserRiskProfileEntity r
                                                 where r.userBizId = u.bizId and r.deleted = false
                                                   and r.kycStatus = :kycStatus))
              and (:riskLevel is null or exists (select r.bizId from UserRiskProfileEntity r
                                                 where r.userBizId = u.bizId and r.deleted = false
                                                   and r.riskLevel = :riskLevel))
            """
    )
    Page<UserEntity> search(String keyword, Integer status, Integer kycStatus, Integer riskLevel, Pageable pageable);
}
