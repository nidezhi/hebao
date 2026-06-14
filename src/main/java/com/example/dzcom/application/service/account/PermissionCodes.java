package com.example.dzcom.application.service.account;

import java.util.Set;

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

    private static final Set<String> ALL = Set.of(
        ACCOUNT_USER_READ,
        ACCOUNT_USER_CREATE,
        ACCOUNT_USER_UPDATE,
        ACCOUNT_USER_UPDATE_STATUS,
        ACCOUNT_USER_UPDATE_KYC,
        ACCOUNT_USER_UPDATE_RISK,
        ACCOUNT_USER_DELETE,
        ACCOUNT_ROLE_READ,
        ACCOUNT_ROLE_MANAGE,
        ACCOUNT_ROLE_ASSIGN,
        PRODUCT_CATALOG_MANAGE,
        MARKET_QUOTE_WRITE
    );

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
}
