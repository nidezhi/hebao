package com.example.dzcom.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * API 文档启动提示。
 *
 * <p>监听 Web Server 完成初始化事件，而不是读取静态 {@code server.port} 配置。
 * 因此即使使用随机端口、环境变量覆盖端口或配置上下文路径，日志中打印的地址
 * 仍然是本次启动真实可访问的地址。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "springdoc.api-docs", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class ApiDocStartupListener implements ApplicationListener<WebServerInitializedEvent> {

    @Value("${spring.application.name:DZCOM}")
    private String applicationName;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${springdoc.api-docs.path:/v3/api-docs}")
    private String apiDocsPath;

    @Value("${springdoc.swagger-ui.path:/swagger-ui.html}")
    private String swaggerUiPath;

    @Value("${knife4j.enable:false}")
    private boolean knife4jEnabled;

    /**
     * 应用端口绑定完成后打印全部可用的 API 文档入口。
     */
    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int actualPort = event.getWebServer().getPort();
        String baseUrl = "http://localhost:" + actualPort + normalizeContextPath(contextPath);
        String knife4jLine = knife4jEnabled
            ? "\n  Knife4j 文档:    " + baseUrl + "/doc.html"
            : "";

        log.info("""

            =================================================
              {} 启动成功
            ================================================={} 
              Swagger UI:      {}{}
              OpenAPI JSON:    {}{}
            =================================================
            """,
            applicationName,
            knife4jLine,
            baseUrl,
            normalizePath(swaggerUiPath),
            baseUrl,
            normalizePath(apiDocsPath)
        );
    }

    private String normalizeContextPath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "";
        }
        return path.startsWith("/") ? removeTrailingSlash(path) : "/" + removeTrailingSlash(path);
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private String removeTrailingSlash(String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }
}
