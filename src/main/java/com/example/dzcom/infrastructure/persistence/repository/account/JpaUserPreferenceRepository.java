package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaUserPreferenceRepository extends JpaRepository<UserPreferenceEntity, String> {
    /**
     * 执行 find by user biz id and preference key and deleted 处理。
     *
     * @param userBizId 业务对象的唯一标识
     * @param key 数据键
     * @param deleted deleted 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<UserPreferenceEntity> findByUserBizIdAndPreferenceKeyAndDeleted(
        /**
         * 执行 find by user biz id and preference key 处理。
         *
         * @param userBizId 业务对象的唯一标识
         * @param key 数据键
         * @return 查询到的业务数据
         * @author dz
         * @date 2026-06-14
         */
        String userBizId, String key, int deleted);
    Optional<UserPreferenceEntity> findByUserBizIdAndPreferenceKey(String userBizId, String key);
    /**
     * 执行 find all by user biz id and deleted order by preference key 处理。
     *
     * @param userBizId 业务对象的唯一标识
     * @param deleted deleted 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    List<UserPreferenceEntity> findAllByUserBizIdAndDeletedOrderByPreferenceKey(String userBizId, int deleted);
    /**
     * 执行 find all by user biz id 处理。
     *
     * @param userBizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    List<UserPreferenceEntity> findAllByUserBizId(String userBizId);
}
