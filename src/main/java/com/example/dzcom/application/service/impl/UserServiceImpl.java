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
 * 用户服务实现类（Application层）
 * <p>
 * 实现 UserService 接口，提供用户管理的完整业务逻辑
 * 包含用户认证、CRUD操作、状态管理等功能
 * 使用事务保证数据一致性，使用日志记录关键操作
 * </p>
 *
 * @author dzcom
 * @version 1.0
 * @since 2026-05-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    /**
     * 用户认证（登录验证）
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 用户领域模型
     * @throws BusinessException 认证失败时抛出（401或403）
     */
    @Override
    public User authenticate(String username, String password) {
        // 1. 根据用户名查询用户
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        
        // 2. 验证密码是否正确
        if (StrUtil.isBlank(user.getPasswordHash()) || !SecurityUtil.matches(password, user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        // 3. 检查用户是否处于激活状态
        if (!user.isActive()) {
            throw new BusinessException(403, "用户已被禁用");
        }
        
        return user;
    }
    
    /**
     * 根据业务ID获取用户
     *
     * @param userId 业务用户ID
     * @return 用户领域模型
     * @throws BusinessException 用户不存在时抛出（404）
     */
    @Override
    public User getById(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }
    
    /**
     * 创建新用户
     * <p>
     * 业务流程：
     * 1. 校验用户名、邮箱、手机号的唯一性
     * 2. 对用户密码进行BCrypt加密
     * 3. 生成用户编号和业务ID
     * 4. 设置默认风险等级和KYC状态
     * 5. 持久化到数据库
     * </p>
     *
     * @param request 创建用户请求对象
     * @return 新用户的业务ID
     * @throws BusinessException 用户名/邮箱/手机号已存在时抛出（400）
     */
    @Override
    @Transactional
    public String create(CreateUserRequest request) {
        // 1. 检查用户名是否已存在
        userRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new BusinessException(400, "用户名已存在");
        });
        
        // 2. 检查邮箱是否已存在（如果提供）
        if (StrUtil.isNotBlank(request.getEmail())) {
            userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
                throw new BusinessException(400, "邮箱已被注册");
            });
        }
        
        // 3. 检查手机号是否已存在（如果提供）
        if (StrUtil.isNotBlank(request.getPhone())) {
            userRepository.findByPhone(request.getPhone()).ifPresent(u -> {
                throw new BusinessException(400, "手机号已被注册");
            });
        }
        
        // 4. 构建用户领域模型
        User user = User.builder()
            .bizId(UUID.randomUUID().toString())  // 生成UUID作为业务ID
            .userNo(generateUserNo())              // 生成可读的用户编号
            .username(request.getUsername())
            .email(request.getEmail())
            .phone(request.getPhone())
            .passwordHash(SecurityUtil.encodePassword(request.getPassword()))  // BCrypt加密
            .kycStatus(0)                          // 默认未认证
            .riskLevel(request.getRiskLevel() != null ? request.getRiskLevel() : 1)  // 默认保守型
            .status(1)                             // 默认激活
            .isDeleted(0)                          // 未删除
            .build();
        
        // 5. 持久化到数据库
        userRepository.save(user);
        
        log.info("用户创建成功: bizId={}, username={}", user.getBizId(), user.getUsername());
        return user.getBizId();
    }
    
    /**
     * 更新用户信息
     * <p>
     * 支持更新：邮箱、手机号、风险等级等非核心字段
     * 不允许更新：用户名、密码等核心标识字段
     * </p>
     *
     * @param bizId 业务用户ID
     * @param request 更新用户请求对象
     * @throws BusinessException 用户不存在或邮箱/手机号被其他用户使用时抛出（400/404）
     */
    @Override
    @Transactional
    public void update(String bizId, UpdateUserRequest request) {
        // 1. 查询用户是否存在
        User user = getById(bizId);
        
        // 2. 更新邮箱（如果提供）
        if (StrUtil.isNotBlank(request.getEmail())) {
            // 检查邮箱是否被其他用户使用
            userRepository.findByEmail(request.getEmail())
                .filter(u -> !u.getBizId().equals(bizId))  // 排除当前用户
                .ifPresent(u -> {
                    throw new BusinessException(400, "邮箱已被其他用户使用");
                });
            user.setEmail(request.getEmail());
        }
        
        // 3. 更新手机号（如果提供）
        if (StrUtil.isNotBlank(request.getPhone())) {
            // 检查手机号是否被其他用户使用
            userRepository.findByPhone(request.getPhone())
                .filter(u -> !u.getBizId().equals(bizId))  // 排除当前用户
                .ifPresent(u -> {
                    throw new BusinessException(400, "手机号已被其他用户使用");
                });
            user.setPhone(request.getPhone());
        }
        
        // 4. 更新风险等级（如果提供）
        if (request.getRiskLevel() != null) {
            user.updateRiskLevel(request.getRiskLevel());  // 使用领域行为，包含校验逻辑
        }
        
        // 5. 持久化更新
        userRepository.update(user);
        log.info("用户更新成功: bizId={}", bizId);
    }
    
    /**
     * 删除用户（逻辑删除）
     * <p>
     * 将用户的 is_deleted 标记为 1，不物理删除数据
     * 同时删除该用户的所有偏好配置
     * </p>
     *
     * @param bizId 业务用户ID
     * @throws BusinessException 用户不存在时抛出（404）
     */
    @Override
    @Transactional
    public void delete(String bizId) {
        // 1. 检查用户是否存在
        getById(bizId);
        
        // 2. 逻辑删除用户
        userRepository.deleteById(bizId);
        log.info("用户删除成功: bizId={}", bizId);
    }
    
    /**
     * 查询用户详细信息
     *
     * @param bizId 业务用户ID
     * @return 用户响应对象
     * @throws BusinessException 用户不存在时抛出（404）
     */
    @Override
    public UserResponse getUserDetail(String bizId) {
        User user = getById(bizId);
        return convertToResponse(user);
    }
    
    /**
     * 分页查询用户列表
     * <p>
     * 按创建时间倒序排列，仅返回未删除的用户
     * </p>
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小（建议10-100）
     * @return 用户响应对象列表
     */
    @Override
    public List<UserResponse> listUsers(int pageNum, int pageSize) {
        List<User> users = userRepository.findAll(pageNum, pageSize);
        return users.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * 统计用户总数
     *
     * @return 未删除的用户数量
     */
    @Override
    public long count() {
        return userRepository.count();
    }
    
    /**
     * 启用用户账户
     *
     * @param bizId 业务用户ID
     * @throws BusinessException 用户不存在时抛出（404）
     */
    @Override
    @Transactional
    public void enableUser(String bizId) {
        User user = getById(bizId);
        user.enable();  // 使用领域行为
        userRepository.update(user);
        log.info("用户启用成功: bizId={}", bizId);
    }
    
    /**
     * 禁用用户账户
     * <p>
     * 常用于违规用户封禁、安全风控等场景
     * </p>
     *
     * @param bizId 业务用户ID
     * @throws BusinessException 用户不存在时抛出（404）
     */
    @Override
    @Transactional
    public void disableUser(String bizId) {
        User user = getById(bizId);
        user.disable();  // 使用领域行为
        userRepository.update(user);
        log.info("用户禁用成功: bizId={}", bizId);
    }
    
    /**
     * 更新用户风险等级
     * <p>
     * 根据用户的投资经验、财务状况等因素调整风险等级
     * 风险等级影响用户可购买的产品类型
     * </p>
     *
     * @param bizId 业务用户ID
     * @param riskLevel 风险等级（1-5级）
     * @throws BusinessException 用户不存在或风险等级无效时抛出
     */
    @Override
    @Transactional
    public void updateRiskLevel(String bizId, Integer riskLevel) {
        User user = getById(bizId);
        user.updateRiskLevel(riskLevel);  // 使用领域行为，包含校验逻辑
        userRepository.update(user);
        log.info("用户风险等级更新成功: bizId={}, riskLevel={}", bizId, riskLevel);
    }
    
    /**
     * 生成用户编号
     * <p>
     * 格式：U + 时间戳，例如：U1717065600000
     * </p>
     *
     * @return 用户编号
     */
    private String generateUserNo() {
        return "U" + System.currentTimeMillis();
    }
    
    /**
     * 领域模型转响应VO
     * <p>
     * 将领域层的 User 对象转换为接口层的 UserResponse 对象
     * </p>
     *
     * @param user 用户领域模型
     * @return 用户响应对象
     */
    private UserResponse convertToResponse(User user) {
        if (user == null) {
            return null;
        }
        return BeanUtil.copyProperties(user, UserResponse.class);
    }
}
