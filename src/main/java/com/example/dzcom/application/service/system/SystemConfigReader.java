package com.example.dzcom.application.service.system;

import java.math.BigDecimal;
import java.util.Optional;

/** 读取非敏感系统配置。 */
public interface SystemConfigReader {
    /** 读取字符串配置。 */
    Optional<String> stringValue(String configGroup, String configKey);

    /** 读取数值配置。 */
    Optional<BigDecimal> decimalValue(String configGroup, String configKey);
}
