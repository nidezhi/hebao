package com.example.dzcom.interfaces.controller.account;

import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.application.dto.account.RoleView;
import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.application.service.account.RoleApplicationService;
import com.example.dzcom.interfaces.request.account.ConfigureRolePermissionsRequest;
import com.example.dzcom.interfaces.request.account.CreateRoleRequest;
import com.example.dzcom.interfaces.request.account.RoleStatusRequest;
import com.example.dzcom.interfaces.request.account.UpdateRoleRequest;
import com.example.dzcom.interfaces.request.account.UserRoleAssignmentRequest;
import com.example.dzcom.interfaces.request.account.UserRoleRevokeRequest;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "角色权限管理")
public class AdminRoleController {
    private final RoleApplicationService roles;

    /** 查询全部角色及权限。 */
    @PostMapping("/list")
    @Operation(summary = "角色列表")
    public Result<List<RoleView>> list() {
        return Result.success(roles.list());
    }

    /** 创建自定义角色。 */
    @PostMapping("/create")
    @Operation(summary = "创建角色")
    public Result<RoleView> create(@Valid @RequestBody CreateRoleRequest request) {
        return Result.success(roles.create(
            request.roleCode(),
            request.roleName(),
            request.description()
        ));
    }

    /** 更新角色名称和说明。 */
    @PostMapping("/update")
    @Operation(summary = "更新角色")
    public Result<RoleView> update(@Valid @RequestBody UpdateRoleRequest request) {
        return Result.success(roles.update(
            request.roleCode(),
            request.roleName(),
            request.description()
        ));
    }

    /** 启用或停用角色。 */
    @PostMapping("/status")
    @Operation(summary = "更新角色状态")
    public Result<RoleView> status(@Valid @RequestBody RoleStatusRequest request) {
        return Result.success(roles.changeStatus(request.roleCode(), request.enabled()));
    }

    /** 覆盖配置角色权限。 */
    @PostMapping("/permissions/configure")
    @Operation(summary = "配置角色权限")
    public Result<RoleView> configurePermissions(
        @Valid @RequestBody ConfigureRolePermissionsRequest request
    ) {
        return Result.success(roles.configurePermissions(
            request.roleCode(),
            request.permissions()
        ));
    }

    /** 给用户分配角色。 */
    @PostMapping("/users/assign")
    @Operation(summary = "分配用户角色")
    public Result<UserView> assign(@Valid @RequestBody UserRoleAssignmentRequest request) {
        return Result.success(roles.assign(
            request.userBizId(),
            request.roleCode(),
            request.effectiveTo()
        ));
    }

    /** 撤销用户指定角色。 */
    @PostMapping("/users/revoke")
    @Operation(summary = "撤销用户角色")
    public Result<UserView> revoke(@Valid @RequestBody UserRoleRevokeRequest request) {
        return Result.success(roles.revoke(request.userBizId(), request.roleCode()));
    }
}
