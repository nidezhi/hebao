package com.example.dzcom.infrastructure.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dzcom.infrastructure.dao.entity.UserPreference;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户偏好Mapper
 */
@Mapper
public interface UserPreferenceMapper extends BaseMapper<UserPreference> {
}
