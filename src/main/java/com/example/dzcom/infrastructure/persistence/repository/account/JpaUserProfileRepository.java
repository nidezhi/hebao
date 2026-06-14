package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserProfileRepository extends JpaRepository<UserProfileEntity, String> {
    /**
     * 执行 find by user biz id and deleted 处理。
     *
     * @param userBizId 业务对象的唯一标识
     * @param deleted deleted 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<UserProfileEntity> findByUserBizIdAndDeleted(String userBizId, int deleted);
    /**
     * 执行 find by user biz id 处理。
     *
     * @param userBizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<UserProfileEntity> findByUserBizId(String userBizId);
}
