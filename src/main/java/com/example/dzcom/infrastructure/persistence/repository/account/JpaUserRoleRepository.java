package com.example.dzcom.infrastructure.persistence.repository.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaUserRoleRepository extends JpaRepository<UserRoleEntity, String> {
    /**
     * 执行 find all by user biz id and deleted 处理。
     *
     * @param userBizId 业务对象的唯一标识
     * @param deleted deleted 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    List<UserRoleEntity> findAllByUserBizIdAndDeleted(String userBizId, int deleted);
    /**
     * 执行 find all by user biz id 处理。
     *
     * @param userBizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    List<UserRoleEntity> findAllByUserBizId(String userBizId);
}
