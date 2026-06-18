package com.example.dzcom.application.service.ai;

/**
 * AI 模型密钥解析端口。
 *
 * <p>业务代码只使用模型配置中的密钥引用名，不读取或保存具体密钥来源。</p>
 */
public interface AiSecretResolver {
    /**
     * 根据密钥引用名取得模型调用密钥。
     *
     * @param secretRef 模型配置中声明的密钥引用名
     * @return 对应的 API Key；不存在时抛出业务异常
     * @author dz
     * @date 2026-06-18
     */
    String resolve(String secretRef);
}
