package com.example.dzcom.infrastructure.persistence.mapper.account;

import com.example.dzcom.infrastructure.persistence.entity.account.RoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 角色定义 MyBatis Mapper。 */
@Mapper
public interface RoleMapper {
    /** 根据角色编码查询未删除角色。 */
    RoleEntity selectByCode(@Param("roleCode") String roleCode);

    /** 查询全部未删除角色。 */
    List<RoleEntity> selectAll();

    /** 新增或更新角色定义。 */
    int save(RoleEntity entity);
}
