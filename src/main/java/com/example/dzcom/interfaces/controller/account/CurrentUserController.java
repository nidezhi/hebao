package com.example.dzcom.interfaces.controller.account;

import com.example.dzcom.application.command.account.UpdateIdentitiesCommand;
import com.example.dzcom.application.dto.account.PreferenceView;
import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.application.service.account.UserApplicationService;
import com.example.dzcom.application.service.account.UserPreferenceApplicationService;
import com.example.dzcom.common.result.Result;
import com.example.dzcom.interfaces.request.account.ChangePasswordRequest;
import com.example.dzcom.interfaces.request.account.PreferenceRequest;
import com.example.dzcom.interfaces.request.account.UpdateUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 当前登录用户的资料、密码和偏好接口。 */
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
@Tag(name = "本人账户")
public class CurrentUserController {
    private final UserApplicationService users;
    private final UserPreferenceApplicationService preferences;

    @PatchMapping
    @Operation(summary = "更新本人邮箱和手机号")
    public Result<UserView> update(@Valid @RequestBody UpdateUserRequest request) {
        return Result.success(users.updateCurrentUser(UpdateIdentitiesCommand.builder()
            .email(request.email())
            .phone(request.phone())
            .build()));
    }

    @PutMapping("/password")
    @Operation(summary = "修改本人密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        users.changePassword(request.currentPassword(), request.newPassword());
        return Result.success();
    }

    @GetMapping("/preferences")
    @Operation(summary = "查询本人偏好")
    public Result<List<PreferenceView>> preferences() {
        return Result.success(preferences.list());
    }

    @PutMapping("/preferences/{key}")
    @Operation(summary = "设置本人偏好")
    public Result<PreferenceView> setPreference(@PathVariable String key,
                                                @Valid @RequestBody PreferenceRequest request) {
        return Result.success(preferences.set(key, request.value()));
    }

    @DeleteMapping("/preferences/{key}")
    @Operation(summary = "删除本人偏好")
    public Result<Void> deletePreference(@PathVariable String key) {
        preferences.delete(key);
        return Result.success();
    }
}
