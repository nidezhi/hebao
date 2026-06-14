package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserCredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaUserCredentialRepository extends JpaRepository<UserCredentialEntity, String> {
    /**
     * 执行 find by user biz id and credential type and deleted 处理。
     *
     * @param userBizId 业务对象的唯一标识
     * @param type 数据类型
     * @param deleted deleted 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<UserCredentialEntity> findByUserBizIdAndCredentialTypeAndDeleted(
        /**
         * 执行 find all by user biz id 处理。
         *
         * @param userBizId 业务对象的唯一标识
         * @return 查询到的业务数据
         * @author dz
         * @date 2026-06-14
         */
        String userBizId, String type, int deleted);
    List<UserCredentialEntity> findAllByUserBizId(String userBizId);
}
