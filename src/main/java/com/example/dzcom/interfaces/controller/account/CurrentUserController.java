package com.example.dzcom.interfaces.controller.account;

import com.example.dzcom.application.command.account.UpdateIdentitiesCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.dto.account.PreferenceView;
import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.application.service.account.UserApplicationService;
import com.example.dzcom.application.service.account.UserPreferenceApplicationService;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.interfaces.request.account.ChangePasswordRequest;
import com.example.dzcom.interfaces.request.account.PreferenceKeyRequest;
import com.example.dzcom.interfaces.request.account.PreferenceRequest;
import com.example.dzcom.interfaces.request.account.UpdateUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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

    /**
     * 更新当前用户的邮箱和手机号登录标识。
     *
     * @param request 待更新的用户资料
     * @return 更新后的当前用户信息
     * @throws BusinessException 当登录标识冲突或用户不存在时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/update")
    @Operation(summary = "更新本人邮箱和手机号", description = "更新当前登录用户的 email 与 phone，并做唯一性校验。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回更新后的当前用户 UserView", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "400", description = "参数校验失败或登录标识冲突"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效")
    })
    public Result<UserView> update(@Valid @RequestBody UpdateUserRequest request) {
        return Result.success(users.updateCurrentUser(UpdateIdentitiesCommand.builder()
            .email(request.email())
            .phone(request.phone())
            .build()));
    }

    /**
     * 修改当前用户密码并撤销相关会话。
     *
     * @param request 当前密码和新密码
     * @return 无业务数据的成功结果
     * @throws BusinessException 当当前密码错误或新密码不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/password")
    @Operation(summary = "修改本人密码", description = "使用当前密码验证后设置新密码，成功后撤销相关会话以保证安全。新密码遵循注册时的强度规则。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "密码修改成功（Result<Void>）"),
        @ApiResponse(responseCode = "400", description = "参数校验失败或新密码不合法"),
        @ApiResponse(responseCode = "401", description = "当前密码错误或未登录")
    })
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        users.changePassword(request.currentPassword(), request.newPassword());
        return Result.success();
    }

    /**
     * 查询当前用户的全部有效偏好。
     *
     * @return 当前用户偏好列表
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/preferences/list")
    @Operation(summary = "查询本人偏好", description = "查询当前登录用户全部有效偏好，返回数组。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回偏好列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效")
    })
    public Result<List<PreferenceView>> preferences() {
        return Result.success(preferences.list());
    }

    /**
     * 新增或覆盖当前用户的指定偏好。
     *
     * @param request 偏好键和值
     * @return 保存后的偏好信息
     * @throws BusinessException 当偏好键或偏好值不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/preferences/set")
    @Operation(summary = "设置本人偏好", description = "新增或覆盖当前用户的指定偏好。json 值请保证为合法 JSON 文本（字符串值需包含引号）。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回保存后的偏好 PreferenceView", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "400", description = "偏好键或偏好值不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效")
    })
    public Result<PreferenceView> setPreference(@Valid @RequestBody PreferenceRequest request) {
        return Result.success(preferences.set(request.key(), request.value()));
    }

    /**
     * 逻辑删除当前用户的指定偏好。
     *
     * @param request 待删除的偏好键
     * @return 无业务数据的成功结果
     * @throws BusinessException 当偏好键不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/preferences/delete")
    @Operation(summary = "删除本人偏好", description = "逻辑删除当前用户指定的偏好键。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功（Result<Void>）"),
        @ApiResponse(responseCode = "400", description = "偏好键不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效")
    })
    public Result<Void> deletePreference(@Valid @RequestBody PreferenceKeyRequest request) {
        preferences.delete(request.key());
        return Result.success();
    }
}
