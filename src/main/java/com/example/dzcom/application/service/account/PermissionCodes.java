package com.example.dzcom.application.service.account;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统权限编码注册表。
 *
 * <p>数据库只能保存本注册表中的稳定编码，接口和应用服务禁止自行拼接权限字符串。</p>
 */
public final class PermissionCodes {
    public static final String ACCOUNT_USER_READ = "account:user:read";
    public static final String ACCOUNT_USER_CREATE = "account:user:create";
    public static final String ACCOUNT_USER_UPDATE = "account:user:update";
    public static final String ACCOUNT_USER_UPDATE_STATUS = "account:user:update-status";
    public static final String ACCOUNT_USER_UPDATE_KYC = "account:user:update-kyc";
    public static final String ACCOUNT_USER_UPDATE_RISK = "account:user:update-risk";
    public static final String ACCOUNT_USER_DELETE = "account:user:delete";
    public static final String ACCOUNT_ROLE_READ = "account:role:read";
    public static final String ACCOUNT_ROLE_MANAGE = "account:role:manage";
    public static final String ACCOUNT_ROLE_ASSIGN = "account:role:assign";
    public static final String PRODUCT_CATALOG_MANAGE = "product:catalog:manage";
    public static final String MARKET_QUOTE_WRITE = "market:quote:write";

    private static final List<PermissionDescriptor> CATALOG = List.of(
        new PermissionDescriptor(ACCOUNT_USER_READ, "查看用户", "账户与用户", "查看用户列表、详情和基础权限信息。", "LOW", "ACTIVE"),
        new PermissionDescriptor(ACCOUNT_USER_CREATE, "创建用户", "账户与用户", "创建管理端用户并设置初始状态。", "HIGH", "ACTIVE"),
        new PermissionDescriptor(ACCOUNT_USER_UPDATE, "更新用户资料", "账户与用户", "更新用户邮箱、手机、昵称等基础资料。", "MEDIUM", "ACTIVE"),
        new PermissionDescriptor(ACCOUNT_USER_UPDATE_STATUS, "变更用户状态", "账户与用户", "启用、禁用或锁定用户。", "HIGH", "ACTIVE"),
        new PermissionDescriptor(ACCOUNT_USER_UPDATE_KYC, "变更 KYC 状态", "账户与用户", "维护用户 KYC 合规状态。", "HIGH", "ACTIVE"),
        new PermissionDescriptor(ACCOUNT_USER_UPDATE_RISK, "变更用户风险等级", "账户与用户", "维护用户风险等级，用于投资和模拟交易门禁。", "HIGH", "ACTIVE"),
        new PermissionDescriptor(ACCOUNT_USER_DELETE, "删除用户", "账户与用户", "逻辑删除用户并撤销相关会话。", "HIGH", "ACTIVE"),
        new PermissionDescriptor(ACCOUNT_ROLE_READ, "查看角色", "角色权限", "查看角色、权限集合和授权边界。", "LOW", "ACTIVE"),
        new PermissionDescriptor(ACCOUNT_ROLE_MANAGE, "管理角色权限", "角色权限", "创建角色、变更角色状态和覆盖配置权限集合。", "CRITICAL", "ACTIVE"),
        new PermissionDescriptor(ACCOUNT_ROLE_ASSIGN, "分配用户角色", "角色权限", "给用户分配或撤销角色。", "HIGH", "ACTIVE"),
        new PermissionDescriptor(PRODUCT_CATALOG_MANAGE, "管理产品目录", "产品与行情", "维护产品基础信息、画像、属性和生命周期。", "HIGH", "ACTIVE"),
        new PermissionDescriptor(MARKET_QUOTE_WRITE, "写入行情", "产品与行情", "写入或修正产品行情点。", "HIGH", "ACTIVE")
    );
    private static final Set<String> ALL = CATALOG.stream()
        .map(PermissionDescriptor::permissionCode)
        .collect(Collectors.toUnmodifiableSet());

    private PermissionCodes() {
    }

    /**
     * 判断权限编码是否属于系统注册表。
     *
     * @param permissionCode 待校验权限编码
     * @return 已注册时返回 true
     * @author dz
     * @date 2026-06-14
     */
    public static boolean contains(String permissionCode) {
        return ALL.contains(permissionCode);
    }

    /**
     * 返回全部已注册权限编码。
     *
     * @return 不可变权限编码集合
     * @author dz
     * @date 2026-06-14
     */
    public static Set<String> all() {
        return ALL;
    }

    /**
     * 返回前端权限选择器所需的结构化权限目录。
     *
     * @return 不可变权限目录
     * @author dz
     * @date 2026-06-28
     */
    public static List<PermissionDescriptor> catalog() {
        return CATALOG;
    }

    /**
     * 权限目录条目。
     *
     * @param permissionCode 稳定权限编码
     * @param displayName 前端展示名称
     * @param groupName 权限分组
     * @param description 权限说明
     * @param riskLevel 风险等级
     * @param status 权限状态
     */
    public record PermissionDescriptor(
        String permissionCode,
        String displayName,
        String groupName,
        String description,
        String riskLevel,
        String status
    ) {
    }
}
