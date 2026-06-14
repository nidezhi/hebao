package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.model.account.Role;

import java.util.List;
import java.util.Optional;

/** 角色定义仓储端口。 */
public interface RoleStore {
    /** 保存角色定义。 */
    Role save(Role role);

    /** 根据角色编码查询未删除角色。 */
    Optional<Role> findByCode(String roleCode);

    /** 查询全部未删除角色。 */
    List<Role> findAll();
}
