package com.example.dzcom.infrastructure.persistence.mapper.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserPreferenceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 用户偏好 MyBatis Mapper。 */
@Mapper
public interface UserPreferenceMapper {
    /** 根据业务标识查询用户偏好。 */
    UserPreferenceEntity selectById(@Param("bizId") String bizId);

    /** 根据用户业务标识和偏好键查询偏好。 */
    UserPreferenceEntity selectByUserBizIdAndKey(@Param("userBizId") String userBizId,
                                                  @Param("key") String key,
                                                  @Param("includeDeleted") boolean includeDeleted);

    /** 查询用户全部未删除偏好。 */
    List<UserPreferenceEntity> selectByUserBizId(@Param("userBizId") String userBizId);

    /** 新增或更新用户偏好。 */
    int save(UserPreferenceEntity entity);

    /** 软删除用户的全部偏好。 */
    int softDeleteByUserBizId(@Param("userBizId") String userBizId);
}
