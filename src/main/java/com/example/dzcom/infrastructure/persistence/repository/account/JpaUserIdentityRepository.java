package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaUserIdentityRepository extends JpaRepository<UserIdentityEntity, String> {
    Optional<UserIdentityEntity> findByIdentityTypeAndNormalizedValueAndStatusAndDeleted(
        String identityType, String normalizedValue, int status, int deleted);
    Optional<UserIdentityEntity> findByUserBizIdAndIdentityTypeAndDeleted(
        String userBizId, String identityType, int deleted);
    List<UserIdentityEntity> findAllByUserBizIdAndDeleted(String userBizId, int deleted);
    List<UserIdentityEntity> findAllByUserBizId(String userBizId);
}
