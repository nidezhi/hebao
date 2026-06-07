package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserRiskProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserRiskProfileRepository extends JpaRepository<UserRiskProfileEntity, String> {
    Optional<UserRiskProfileEntity> findByUserBizIdAndDeletedFalse(String userBizId);
    Optional<UserRiskProfileEntity> findByUserBizId(String userBizId);
}
