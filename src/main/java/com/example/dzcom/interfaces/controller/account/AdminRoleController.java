package com.example.dzcom.interfaces.controller.account;

import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.service.account.RoleApplicationService;
import com.example.dzcom.interfaces.dto.response.account.RoleResponse;
import com.example.dzcom.interfaces.dto.response.account.UserResponse;
import com.example.dzcom.interfaces.request.account.ConfigureRolePermissionsRequest;
import com.example.dzcom.interfaces.request.account.CreateRoleRequest;
import com.example.dzcom.interfaces.request.account.RoleStatusRequest;
import com.example.dzcom.interfaces.request.account.UpdateRoleRequest;
import com.example.dzcom.interfaces.request.account.UserRoleAssignmentRequest;
import com.example.dzcom.interfaces.request.account.UserRoleRevokeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 管理端角色、权限和用户角色分配接口。 */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@Tag(name = "角色权限管理", description = "管理角色、角色权限以及用户角色分配的管理端接口")
public class AdminRoleController {
    private final RoleApplicationService roles;

    /** 查询全部角色及权限。 */
    @PostMapping("/list")
    @Operation(summary = "角色列表", description = "返回系统中全部角色及其权限集合。用于管理端角色查看与权限审计。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回角色响应数组", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无角色查看权限"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<List<RoleResponse>> list() {
        return Result.success(roles.list().stream().map(RoleResponse::from).toList());
    }

    /** 创建自定义角色。 */
    @PostMapping("/create")
    @Operation(summary = "创建角色", description = "创建自定义角色，roleCode 必须为大写字母开头，长度 3-64，允许数字和下划线。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回新建角色响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无角色创建权限"),
        @ApiResponse(responseCode = "409", description = "角色编码冲突"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<RoleResponse> create(@Valid @RequestBody CreateRoleRequest request) {
        return Result.success(RoleResponse.from(roles.create(
            request.roleCode(),
            request.roleName(),
            request.description()
        )));
    }

    /** 更新角色名称和说明。 */
    @PostMapping("/update")
    @Operation(summary = "更新角色", description = "更新角色名称与说明。角色编码为不可变字段。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回更新后的角色响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无角色更新权限"),
        @ApiResponse(responseCode = "404", description = "角色不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<RoleResponse> update(@Valid @RequestBody UpdateRoleRequest request) {
        return Result.success(RoleResponse.from(roles.update(
            request.roleCode(),
            request.roleName(),
            request.description()
        )));
    }

    /** 启用或停用角色。 */
    @PostMapping("/status")
    @Operation(summary = "更新角色状态", description = "启用或停用指定角色（通常用于临时禁止某角色的权限）。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回变更后的角色响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无角色状态变更权限"),
        @ApiResponse(responseCode = "404", description = "角色不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<RoleResponse> status(@Valid @RequestBody RoleStatusRequest request) {
        return Result.success(RoleResponse.from(
            roles.changeStatus(request.roleCode(), request.enabled())));
    }

    /** 覆盖配置角色权限。 */
    @PostMapping("/permissions/configure")
    @Operation(summary = "配置角色权限", description = "覆盖配置角色的权限集合。传入的 permissions 将替换现有权限集合。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回更新后的角色响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败或权限格式不正确"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无角色权限配置权限"),
        @ApiResponse(responseCode = "404", description = "角色不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<RoleResponse> configurePermissions(
        @Valid @RequestBody ConfigureRolePermissionsRequest request
    ) {
        return Result.success(RoleResponse.from(roles.configurePermissions(
            request.roleCode(),
            request.permissions()
        )));
    }

    /** 给用户分配角色。 */
    @PostMapping("/users/assign")
    @Operation(summary = "分配用户角色", description = "给用户分配角色，可设置 effectiveTo 为角色失效时间（可选）。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回用户最新响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无用户角色分配权限"),
        @ApiResponse(responseCode = "404", description = "用户或角色不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<UserResponse> assign(@Valid @RequestBody UserRoleAssignmentRequest request) {
        return Result.success(UserResponse.from(roles.assign(
            request.userBizId(),
            request.roleCode(),
            request.effectiveTo()
        )));
    }

    /** 撤销用户指定角色。 */
    @PostMapping("/users/revoke")
    @Operation(summary = "撤销用户角色", description = "撤销用户的指定角色分配。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回用户最新响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无用户角色撤销权限"),
        @ApiResponse(responseCode = "404", description = "用户或角色不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<UserResponse> revoke(@Valid @RequestBody UserRoleRevokeRequest request) {
        return Result.success(UserResponse.from(
            roles.revoke(request.userBizId(), request.roleCode())));
    }
}
