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
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户CRUD操作")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    @Operation(summary = "创建用户", description = "注册新用户")
    public Result<String> createUser(@Valid @RequestBody CreateUserRequest request) {
        String bizId = userService.create(request);
        return Result.success(bizId);
    }
    
    @PutMapping("/{bizId}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public Result<Void> updateUser(
            @Parameter(description = "用户ID") @PathVariable String bizId,
            @Valid @RequestBody UpdateUserRequest request) {
        userService.update(bizId, request);
        return Result.success();
    }
    
    @DeleteMapping("/{bizId}")
    @Operation(summary = "删除用户", description = "逻辑删除用户")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable String bizId) {
        userService.delete(bizId);
        return Result.success();
    }
    
    @GetMapping("/{bizId}")
    @Operation(summary = "查询用户详情", description = "根据ID查询用户详细信息")
    public Result<UserResponse> getUserDetail(
            @Parameter(description = "用户ID") @PathVariable String bizId) {
        UserResponse user = userService.getUserDetail(bizId);
        return Result.success(user);
    }
    
    @GetMapping
    @Operation(summary = "查询用户列表", description = "分页查询用户列表")
    public Result<Map<String, Object>> listUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        
        List<UserResponse> users = userService.listUsers(pageNum, pageSize);
        long total = userService.count();
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", users);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        
        return Result.success(result);
    }
    
    @PutMapping("/{bizId}/enable")
    @Operation(summary = "启用用户", description = "启用被禁用的用户账户")
    public Result<Void> enableUser(
            @Parameter(description = "用户ID") @PathVariable String bizId) {
        userService.enableUser(bizId);
        return Result.success();
    }
    
    @PutMapping("/{bizId}/disable")
    @Operation(summary = "禁用用户", description = "禁用用户账户")
    public Result<Void> disableUser(
            @Parameter(description = "用户ID") @PathVariable String bizId) {
        userService.disableUser(bizId);
        return Result.success();
    }
    
    @PutMapping("/{bizId}/risk-level")
    @Operation(summary = "更新风险等级", description = "更新用户的风险承受能力等级")
    public Result<Void> updateRiskLevel(
            @Parameter(description = "用户ID") @PathVariable String bizId,
            @Parameter(description = "风险等级(1-5)") @RequestParam Integer riskLevel) {
        userService.updateRiskLevel(bizId, riskLevel);
        return Result.success();
    }
    
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取登录用户的详细信息")
    public Result<UserResponse> getCurrentUser() {
        String userId = UserContext.getCurrentUserId();
        UserResponse user = userService.getUserDetail(userId);
        return Result.success(user);
    }
}
