package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserCredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaUserCredentialRepository extends JpaRepository<UserCredentialEntity, String> {
    Optional<UserCredentialEntity> findByUserBizIdAndCredentialTypeAndDeleted(
        String userBizId, String type, int deleted);
    List<UserCredentialEntity> findAllByUserBizId(String userBizId);
}
