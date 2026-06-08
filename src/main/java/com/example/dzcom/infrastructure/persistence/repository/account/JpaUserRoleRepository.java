package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaUserRoleRepository extends JpaRepository<UserRoleEntity, String> {
    List<UserRoleEntity> findAllByUserBizIdAndDeleted(String userBizId, int deleted);
    List<UserRoleEntity> findAllByUserBizId(String userBizId);
}
