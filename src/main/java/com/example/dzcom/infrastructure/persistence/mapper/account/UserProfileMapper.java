package com.example.dzcom.infrastructure.persistence.mapper.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserProfileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 用户资料 MyBatis Mapper。 */
@Mapper
public interface UserProfileMapper {
    /** 根据业务标识查询用户资料。 */
    UserProfileEntity selectById(@Param("bizId") String bizId);

    /** 根据用户业务标识查询资料。 */
    UserProfileEntity selectByUserBizId(@Param("userBizId") String userBizId,
                                        @Param("includeDeleted") boolean includeDeleted);

    /** 新增或更新用户资料。 */
    int save(UserProfileEntity entity);

    /** 软删除用户资料。 */
    int softDeleteByUserBizId(@Param("userBizId") String userBizId);
}
