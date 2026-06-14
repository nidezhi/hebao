package com.example.dzcom.domain.service.account;

import com.example.dzcom.domain.enums.account.IdentityType;

public interface IdentityNormalizer {
    /**
     * 执行 detect type 处理。
     *
     * @param account account 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    IdentityType detectType(String account);

    /**
     * 规范化输入值并返回统一格式。
     *
     * @param type 数据类型
     * @param value 待处理的数据值
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    String normalize(IdentityType type, String value);
}
