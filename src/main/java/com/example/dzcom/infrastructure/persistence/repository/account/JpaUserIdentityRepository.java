package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaUserIdentityRepository extends JpaRepository<UserIdentityEntity, String> {
    Optional<UserIdentityEntity> findByIdentityTypeAndNormalizedValueAndStatusAndDeletedFalse(
        String identityType, String normalizedValue, int status);
    Optional<UserIdentityEntity> findByUserBizIdAndIdentityTypeAndDeletedFalse(String userBizId, String identityType);
    List<UserIdentityEntity> findAllByUserBizIdAndDeletedFalse(String userBizId);
    List<UserIdentityEntity> findAllByUserBizId(String userBizId);
}
