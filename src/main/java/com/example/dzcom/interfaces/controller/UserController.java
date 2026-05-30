package com.example.dzcom.interfaces.controller;

import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.common.context.UserContext;
import com.example.dzcom.interfaces.vo.request.CreateUserRequest;
import com.example.dzcom.interfaces.vo.request.UpdateUserRequest;
import com.example.dzcom.interfaces.vo.response.UserResponse;
import com.example.dzcom.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器（Interfaces层）
 * <p>
 * 提供用户管理的 RESTful API 接口，包括用户的 CRUD 操作、状态管理、风险等级管理等
 * 所有接口都遵循统一的响应格式 Result<T>，并集成了 Swagger 文档注解
 * </p>
 *
 * @author dzcom
 * @version 1.0
 * @since 2026-05-30
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "提供用户的增删改查、状态管理、风险等级管理等完整功能")
@RequiredArgsConstructor
public class UserController {
    
    /**
     * 用户服务
     */
    private final UserService userService;
    
    /**
     * 创建新用户
     * <p>
     * POST /api/users
     * </p>
     *
     * @param request 创建用户请求（包含用户名、密码、邮箱、手机号等）
     * @return 新用户的业务ID
     */
    @PostMapping
    @Operation(
        summary = "创建用户",
        description = "注册新用户，需要校验用户名、邮箱、手机号的唯一性，密码会自动加密存储"
    )
    public Result<String> createUser(@Valid @RequestBody CreateUserRequest request) {
        String bizId = userService.create(request);
        return Result.success(bizId);
    }
    
    /**
     * 更新用户信息
     * <p>
     * PUT /api/users/{bizId}
     * </p>
     *
     * @param bizId 业务用户ID
     * @param request 更新用户请求（包含邮箱、手机号、风险等级等）
     * @return 操作结果
     */
    @PutMapping("/{bizId}")
    @Operation(
        summary = "更新用户",
        description = "更新用户信息，支持更新邮箱、手机号、风险等级，不允许修改用户名和密码"
    )
    public Result<Void> updateUser(
            @Parameter(description = "用户业务ID", example = "550e8400-e29b-41d4-a716-446655440000") 
            @PathVariable String bizId,
            @Valid @RequestBody UpdateUserRequest request) {
        userService.update(bizId, request);
        return Result.success();
    }
    
    /**
     * 删除用户（逻辑删除）
     * <p>
     * DELETE /api/users/{bizId}
     * </p>
     *
     * @param bizId 业务用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{bizId}")
    @Operation(
        summary = "删除用户",
        description = "逻辑删除用户，将 is_deleted 标记为 1，同时删除用户的所有偏好配置"
    )
    public Result<Void> deleteUser(
            @Parameter(description = "用户业务ID", example = "550e8400-e29b-41d4-a716-446655440000") 
            @PathVariable String bizId) {
        userService.delete(bizId);
        return Result.success();
    }
    
    /**
     * 查询用户详情
     * <p>
     * GET /api/users/{bizId}
     * </p>
     *
     * @param bizId 业务用户ID
     * @return 用户详细信息
     */
    @GetMapping("/{bizId}")
    @Operation(
        summary = "查询用户详情",
        description = "根据业务ID查询用户的详细信息，包括基本信息、认证状态、风险等级等"
    )
    public Result<UserResponse> getUserDetail(
            @Parameter(description = "用户业务ID", example = "550e8400-e29b-41d4-a716-446655440000") 
            @PathVariable String bizId) {
        UserResponse user = userService.getUserDetail(bizId);
        return Result.success(user);
    }
    
    /**
     * 分页查询用户列表
     * <p>
     * GET /api/users?pageNum=1&pageSize=10
     * </p>
     *
     * @param pageNum 页码（从1开始，默认1）
     * @param pageSize 每页数量（默认10，建议10-100）
     * @return 分页结果（包含用户列表、总数、页码等信息）
     */
    @GetMapping
    @Operation(
        summary = "查询用户列表",
        description = "分页查询用户列表，按创建时间倒序排列，仅返回未删除的用户"
    )
    public Result<Map<String, Object>> listUsers(
            @Parameter(description = "页码", example = "1") 
            @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量", example = "10") 
            @RequestParam(defaultValue = "10") int pageSize) {
        
        List<UserResponse> users = userService.listUsers(pageNum, pageSize);
        long total = userService.count();
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", users);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        
        return Result.success(result);
    }
    
    /**
     * 启用用户账户
     * <p>
     * PUT /api/users/{bizId}/enable
     * </p>
     *
     * @param bizId 业务用户ID
     * @return 操作结果
     */
    @PutMapping("/{bizId}/enable")
    @Operation(
        summary = "启用用户",
        description = "启用被禁用的用户账户，将状态设置为 1，用户可以正常登录"
    )
    public Result<Void> enableUser(
            @Parameter(description = "用户业务ID", example = "550e8400-e29b-41d4-a716-446655440000") 
            @PathVariable String bizId) {
        userService.enableUser(bizId);
        return Result.success();
    }
    
    /**
     * 禁用用户账户
     * <p>
     * PUT /api/users/{bizId}/disable
     * </p>
     *
     * @param bizId 业务用户ID
     * @return 操作结果
     */
    @PutMapping("/{bizId}/disable")
    @Operation(
        summary = "禁用用户",
        description = "禁用用户账户，将状态设置为 0，用户无法登录系统。常用于违规用户封禁、安全风控等场景"
    )
    public Result<Void> disableUser(
            @Parameter(description = "用户业务ID", example = "550e8400-e29b-41d4-a716-446655440000") 
            @PathVariable String bizId) {
        userService.disableUser(bizId);
        return Result.success();
    }
    
    /**
     * 更新用户风险等级
     * <p>
     * PUT /api/users/{bizId}/risk-level?riskLevel=3
     * </p>
     *
     * @param bizId 业务用户ID
     * @param riskLevel 风险等级（1-5级：1-保守型, 2-稳健型, 3-平衡型, 4-成长型, 5-进取型）
     * @return 操作结果
     */
    @PutMapping("/{bizId}/risk-level")
    @Operation(
        summary = "更新风险等级",
        description = "更新用户的风险承受能力等级（1-5级），风险等级影响用户可购买的产品类型"
    )
    public Result<Void> updateRiskLevel(
            @Parameter(description = "用户业务ID", example = "550e8400-e29b-41d4-a716-446655440000") 
            @PathVariable String bizId,
            @Parameter(description = "风险等级(1-5): 1-保守型, 2-稳健型, 3-平衡型, 4-成长型, 5-进取型", 
                      example = "3", required = true) 
            @RequestParam Integer riskLevel) {
        userService.updateRiskLevel(bizId, riskLevel);
        return Result.success();
    }
    
    /**
     * 获取当前登录用户信息
     * <p>
     * GET /api/users/me
     * </p>
     *
     * @return 当前登录用户的详细信息
     */
    @GetMapping("/me")
    @Operation(
        summary = "获取当前用户信息",
        description = "获取当前登录用户的详细信息，从 Token 中解析用户ID"
    )
    public Result<UserResponse> getCurrentUser() {
        String userId = UserContext.getCurrentUserId();
        UserResponse user = userService.getUserDetail(userId);
        return Result.success(user);
    }
}
