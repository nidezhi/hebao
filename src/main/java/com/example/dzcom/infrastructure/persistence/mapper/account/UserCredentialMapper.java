package com.example.dzcom.infrastructure.persistence.mapper.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserCredentialEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 用户凭据 MyBatis Mapper。 */
@Mapper
public interface UserCredentialMapper {
    /** 根据业务标识查询用户凭据。 */
    UserCredentialEntity selectById(@Param("bizId") String bizId);

    /** 查询用户未删除的密码凭据。 */
    UserCredentialEntity selectPasswordByUserBizId(@Param("userBizId") String userBizId);

    /** 新增或更新用户凭据。 */
    int save(UserCredentialEntity entity);

    /** 软删除用户的全部凭据。 */
    int softDeleteByUserBizId(@Param("userBizId") String userBizId);
}
