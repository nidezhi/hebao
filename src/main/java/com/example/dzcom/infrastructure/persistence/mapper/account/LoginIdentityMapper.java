package com.example.dzcom.infrastructure.persistence.mapper.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserIdentityEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 登录标识 MyBatis Mapper。 */
@Mapper
public interface LoginIdentityMapper {
    /** 根据业务标识查询登录标识。 */
    UserIdentityEntity selectById(@Param("bizId") String bizId);

    /** 根据类型和标准化值查询有效登录标识。 */
    UserIdentityEntity selectByTypeAndNormalizedValue(@Param("type") String type,
                                                       @Param("normalizedValue") String normalizedValue);

    /** 根据用户业务标识和类型查询未删除登录标识。 */
    UserIdentityEntity selectByUserBizIdAndType(@Param("userBizId") String userBizId,
                                                 @Param("type") String type);

    /** 根据用户业务标识查询登录标识列表。 */
    List<UserIdentityEntity> selectByUserBizId(@Param("userBizId") String userBizId,
                                               @Param("includeDeleted") boolean includeDeleted);

    /** 新增或更新登录标识。 */
    int save(UserIdentityEntity entity);

    /** 软删除用户的全部登录标识。 */
    int softDeleteByUserBizId(@Param("userBizId") String userBizId);
}
