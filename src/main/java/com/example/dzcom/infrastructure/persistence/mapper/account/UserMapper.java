package com.example.dzcom.infrastructure.persistence.mapper.account;

import com.example.dzcom.domain.repository.account.UserSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.account.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 用户主体 MyBatis Mapper。 */
@Mapper
public interface UserMapper {
    /** 根据业务标识查询用户主体。 */
    UserEntity selectById(@Param("bizId") String bizId);

    /** 根据业务标识查询未删除用户主体。 */
    UserEntity selectActiveByBizId(@Param("bizId") String bizId);

    /** 根据筛选条件分页查询用户主体。 */
    List<UserEntity> search(@Param("criteria") UserSearchCriteria criteria,
                            @Param("offset") int offset,
                            @Param("sortColumn") String sortColumn);

    /** 统计符合筛选条件的用户数量。 */
    long count(@Param("criteria") UserSearchCriteria criteria);

    /** 新增或更新用户主体。 */
    int save(UserEntity entity);
}
