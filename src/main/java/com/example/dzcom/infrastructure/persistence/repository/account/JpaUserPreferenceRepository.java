package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaUserPreferenceRepository extends JpaRepository<UserPreferenceEntity, String> {
    Optional<UserPreferenceEntity> findByUserBizIdAndPreferenceKeyAndDeleted(
        String userBizId, String key, int deleted);
    Optional<UserPreferenceEntity> findByUserBizIdAndPreferenceKey(String userBizId, String key);
    List<UserPreferenceEntity> findAllByUserBizIdAndDeletedOrderByPreferenceKey(String userBizId, int deleted);
    List<UserPreferenceEntity> findAllByUserBizId(String userBizId);
}
