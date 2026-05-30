package com.example.dzcom.application.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.infrastructure.utils.SecurityUtil;
import com.example.dzcom.domain.model.User;
import com.example.dzcom.domain.repository.UserRepository;
import com.example.dzcom.interfaces.vo.request.CreateUserRequest;
import com.example.dzcom.interfaces.vo.request.UpdateUserRequest;
import com.example.dzcom.interfaces.vo.response.UserResponse;
import com.example.dzcom.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    @Override
    public User authenticate(String username, String password) {
        // 查询用户
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        
        // 验证密码
        if (StrUtil.isBlank(user.getPasswordHash()) || !SecurityUtil.matches(password, user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        // 检查用户状态
        if (!user.isActive()) {
            throw new BusinessException(403, "用户已被禁用");
        }
        
        return user;
    }
    
    @Override
    public User getById(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }
    
    @Override
    @Transactional
    public String create(CreateUserRequest request) {
        // 检查用户名是否已存在
        userRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new BusinessException(400, "用户名已存在");
        });
        
        // 检查邮箱是否已存在
        if (StrUtil.isNotBlank(request.getEmail())) {
            userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
                throw new BusinessException(400, "邮箱已被注册");
            });
        }
        
        // 检查手机号是否已存在
        if (StrUtil.isNotBlank(request.getPhone())) {
            userRepository.findByPhone(request.getPhone()).ifPresent(u -> {
                throw new BusinessException(400, "手机号已被注册");
            });
        }
        
        // 构建用户对象
        User user = User.builder()
            .bizId(UUID.randomUUID().toString())
            .userNo(generateUserNo())
            .username(request.getUsername())
            .email(request.getEmail())
            .phone(request.getPhone())
            .passwordHash(SecurityUtil.encodePassword(request.getPassword()))
            .kycStatus(0)
            .riskLevel(request.getRiskLevel() != null ? request.getRiskLevel() : 1)
            .status(1)
            .isDeleted(0)
            .build();
        
        // 保存用户
        userRepository.save(user);
        
        log.info("用户创建成功: bizId={}, username={}", user.getBizId(), user.getUsername());
        return user.getBizId();
    }
    
    @Override
    @Transactional
    public void update(String bizId, UpdateUserRequest request) {
        User user = getById(bizId);
        
        // 更新邮箱
        if (StrUtil.isNotBlank(request.getEmail())) {
            // 检查邮箱是否被其他用户使用
            userRepository.findByEmail(request.getEmail())
                .filter(u -> !u.getBizId().equals(bizId))
                .ifPresent(u -> {
                    throw new BusinessException(400, "邮箱已被其他用户使用");
                });
            user.setEmail(request.getEmail());
        }
        
        // 更新手机号
        if (StrUtil.isNotBlank(request.getPhone())) {
            // 检查手机号是否被其他用户使用
            userRepository.findByPhone(request.getPhone())
                .filter(u -> !u.getBizId().equals(bizId))
                .ifPresent(u -> {
                    throw new BusinessException(400, "手机号已被其他用户使用");
                });
            user.setPhone(request.getPhone());
        }
        
        // 更新风险等级
        if (request.getRiskLevel() != null) {
            user.updateRiskLevel(request.getRiskLevel());
        }
        
        userRepository.update(user);
        log.info("用户更新成功: bizId={}", bizId);
    }
    
    @Override
    @Transactional
    public void delete(String bizId) {
        // 检查用户是否存在
        getById(bizId);
        
        // 逻辑删除
        userRepository.deleteById(bizId);
        log.info("用户删除成功: bizId={}", bizId);
    }
    
    @Override
    public UserResponse getUserDetail(String bizId) {
        User user = getById(bizId);
        return convertToResponse(user);
    }
    
    @Override
    public List<UserResponse> listUsers(int pageNum, int pageSize) {
        List<User> users = userRepository.findAll(pageNum, pageSize);
        return users.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public long count() {
        return userRepository.count();
    }
    
    @Override
    @Transactional
    public void enableUser(String bizId) {
        User user = getById(bizId);
        user.enable();
        userRepository.update(user);
        log.info("用户启用成功: bizId={}", bizId);
    }
    
    @Override
    @Transactional
    public void disableUser(String bizId) {
        User user = getById(bizId);
        user.disable();
        userRepository.update(user);
        log.info("用户禁用成功: bizId={}", bizId);
    }
    
    @Override
    @Transactional
    public void updateRiskLevel(String bizId, Integer riskLevel) {
        User user = getById(bizId);
        user.updateRiskLevel(riskLevel);
        userRepository.update(user);
        log.info("用户风险等级更新成功: bizId={}, riskLevel={}", bizId, riskLevel);
    }
    
    /**
     * 生成用户编号
     */
    private String generateUserNo() {
        return "U" + System.currentTimeMillis();
    }
    
    /**
     * 领域模型转响应VO
     */
    private UserResponse convertToResponse(User user) {
        if (user == null) {
            return null;
        }
        return BeanUtil.copyProperties(user, UserResponse.class);
    }
}
