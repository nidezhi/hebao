package com.example.dzcom.infrastructure.persistence.mapper.account;

import com.example.dzcom.infrastructure.persistence.entity.account.UserRiskProfileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 用户风险画像 MyBatis Mapper。 */
@Mapper
public interface UserRiskProfileMapper {
    /** 根据业务标识查询用户风险画像。 */
    UserRiskProfileEntity selectById(@Param("bizId") String bizId);

    /** 根据用户业务标识查询风险画像。 */
    UserRiskProfileEntity selectByUserBizId(@Param("userBizId") String userBizId,
                                            @Param("includeDeleted") boolean includeDeleted);

    /** 新增或更新用户风险画像。 */
    int save(UserRiskProfileEntity entity);

    /** 软删除用户风险画像。 */
    int softDeleteByUserBizId(@Param("userBizId") String userBizId);
}
