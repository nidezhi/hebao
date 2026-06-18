package com.example.dzcom.application.service.ai;

import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.domain.model.ai.AiModel;

/** 将数据库模型记录解析为可供 Provider 使用的运行时配置。 */
public interface AiModelRuntimeConfigResolver {
    /**
     * 解析模型普通配置并通过密钥引用取得 API Key。
     *
     * @param model 数据库中状态为 ACTIVE 的模型版本
     * @return 已完成默认值处理和密钥注入的运行时配置
     * @author dz
     * @date 2026-06-18
     */
    AiModelRuntimeConfig resolve(AiModel model);
}
