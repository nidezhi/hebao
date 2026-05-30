package com.example.dzcom.application.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dzcom.common.exception.BusinessException;
import com.example.dzcom.common.utils.SecurityUtil;
import com.example.dzcom.infrastructure.dao.entity.User;
import com.example.dzcom.infrastructure.dao.mapper.UserMapper;
import com.example.dzcom.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    
    @Override
    public User authenticate(String username, String password) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        // 验证密码
        if (StrUtil.isBlank(user.getPassword()) || !SecurityUtil.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException(403, "用户已被禁用");
        }
        
        return user;
    }
    
    @Override
    public User getById(String userId) {
        return userMapper.selectById(userId);
    }
}
