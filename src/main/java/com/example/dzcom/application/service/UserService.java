package com.example.dzcom.application.service;

import com.example.dzcom.domain.model.User;
import com.example.dzcom.interfaces.vo.request.CreateUserRequest;
import com.example.dzcom.interfaces.vo.request.UpdateUserRequest;
import com.example.dzcom.interfaces.vo.response.UserResponse;

import java.util.List;

/**
 * 用户服务接口（Application层）
 * <p>
 * 定义用户管理的业务操作，包括用户认证、CRUD操作、状态管理等
 * 该接口遵循应用服务规范，负责协调领域对象完成业务用例
 * </p>
 *
 * @author dzcom
 * @version 1.0
 * @since 2026-05-30
 */
public interface UserService {
    
    /**
     * 用户认证（登录验证）
     * <p>
     * 验证用户名和密码是否正确，返回用户领域模型
     * 如果密码错误或用户被禁用，将抛出业务异常
     * </p>
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 用户领域模型
     * @throws com.example.dzcom.application.common.exception.BusinessException 认证失败时抛出（401或403）
     */
    User authenticate(String username, String password);
    
    /**
     * 根据业务ID获取用户
     *
     * @param userId 业务用户ID
     * @return 用户领域模型
     * @throws com.example.dzcom.application.common.exception.BusinessException 用户不存在时抛出（404）
     */
    User getById(String userId);
    
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
     * @throws com.example.dzcom.application.common.exception.BusinessException 用户名/邮箱/手机号已存在时抛出（400）
     */
    String create(CreateUserRequest request);
    
    /**
     * 更新用户信息
     * <p>
     * 支持更新：邮箱、手机号、昵称等非核心字段
     * 不允许更新：用户名、密码等核心标识字段
     * </p>
     *
     * @param bizId 业务用户ID
     * @param request 更新用户请求对象
     * @throws com.example.dzcom.application.common.exception.BusinessException 用户不存在时抛出（404）
     */
    void update(String bizId, UpdateUserRequest request);
    
    /**
     * 删除用户（逻辑删除）
     * <p>
     * 将用户的 is_deleted 标记为 1，不物理删除数据
     * 同时删除该用户的所有偏好配置
     * </p>
     *
     * @param bizId 业务用户ID
     * @throws com.example.dzcom.application.common.exception.BusinessException 用户不存在时抛出（404）
     */
    void delete(String bizId);
    
    /**
     * 查询用户详细信息
     * <p>
     * 将领域模型转换为响应VO，用于API返回
     * </p>
     *
     * @param bizId 业务用户ID
     * @return 用户响应对象
     * @throws com.example.dzcom.application.common.exception.BusinessException 用户不存在时抛出（404）
     */
    UserResponse getUserDetail(String bizId);
    
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
    List<UserResponse> listUsers(int pageNum, int pageSize);
    
    /**
     * 统计用户总数
     * <p>
     * 统计未删除的用户数量
     * </p>
     *
     * @return 用户总数
     */
    long count();
    
    /**
     * 启用用户账户
     * <p>
     * 将用户状态设置为 1（正常），用户可以登录系统
     * </p>
     *
     * @param bizId 业务用户ID
     * @throws com.example.dzcom.application.common.exception.BusinessException 用户不存在时抛出（404）
     */
    void enableUser(String bizId);
    
    /**
     * 禁用用户账户
     * <p>
     * 将用户状态设置为 0（禁用），用户无法登录系统
     * 常用于违规用户封禁、安全风控等场景
     * </p>
     *
     * @param bizId 业务用户ID
     * @throws com.example.dzcom.application.common.exception.BusinessException 用户不存在时抛出（404）
     */
    void disableUser(String bizId);
    
    /**
     * 更新用户风险等级
     * <p>
     * 根据用户的投资经验、财务状况等因素调整风险等级
     * 风险等级影响用户可购买的产品类型
     * </p>
     *
     * @param bizId 业务用户ID
     * @param riskLevel 风险等级（1-5级）
     * @throws com.example.dzcom.application.common.exception.BusinessException 用户不存在或风险等级无效时抛出
     */
    void updateRiskLevel(String bizId, Integer riskLevel);
}
