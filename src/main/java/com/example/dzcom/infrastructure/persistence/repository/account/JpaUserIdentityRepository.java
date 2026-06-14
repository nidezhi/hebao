package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaUserIdentityRepository extends JpaRepository<UserIdentityEntity, String> {
    /**
     * 执行 find by identity type and normalized value and status and deleted 处理。
     *
     * @param identityType identityType 参数
     * @param normalizedValue normalizedValue 参数
     * @param status 目标状态或目标值
     * @param deleted deleted 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<UserIdentityEntity> findByIdentityTypeAndNormalizedValueAndStatusAndDeleted(
        /**
         * 执行 find by user biz id and identity type and deleted 处理。
         *
         * @param userBizId 业务对象的唯一标识
         * @param identityType identityType 参数
         * @param deleted deleted 参数
         * @return 查询到的业务数据
         * @author dz
         * @date 2026-06-14
         */
        String identityType, String normalizedValue, int status, int deleted);
    Optional<UserIdentityEntity> findByUserBizIdAndIdentityTypeAndDeleted(
        /**
         * 执行 find all by user biz id and deleted 处理。
         *
         * @param userBizId 业务对象的唯一标识
         * @param deleted deleted 参数
         * @return 查询到的业务数据
         * @author dz
         * @date 2026-06-14
         */
        String userBizId, String identityType, int deleted);
    List<UserIdentityEntity> findAllByUserBizIdAndDeleted(String userBizId, int deleted);
    /**
     * 执行 find all by user biz id 处理。
     *
     * @param userBizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    List<UserIdentityEntity> findAllByUserBizId(String userBizId);
}
