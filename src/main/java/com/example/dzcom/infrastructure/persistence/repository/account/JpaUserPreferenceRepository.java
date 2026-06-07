package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaUserPreferenceRepository extends JpaRepository<UserPreferenceEntity, String> {
    Optional<UserPreferenceEntity> findByUserBizIdAndPreferenceKeyAndDeletedFalse(String userBizId, String key);
    Optional<UserPreferenceEntity> findByUserBizIdAndPreferenceKey(String userBizId, String key);
    List<UserPreferenceEntity> findAllByUserBizIdAndDeletedFalseOrderByPreferenceKey(String userBizId);
    List<UserPreferenceEntity> findAllByUserBizId(String userBizId);
}
