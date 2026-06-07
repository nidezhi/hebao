package com.example.dzcom.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * API 文档启动提示。
 */
@Slf4j
@Component
public class ApiDocStartupListener implements CommandLineRunner {
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${spring.application.name:DZCOM}")
    private String applicationName;
    
    /**
     * 应用启动完成后打印文档访问地址。
     */
    @Override
    public void run(String... args) {
        log.info("\n" +
                "=================================================\n" +
                "  {} 启动成功！\n" +
                "=================================================\n" +
                "  Swagger UI 地址: http://localhost:{}/swagger-ui.html\n" +
                "  OpenAPI JSON:    http://localhost:{}/v3/api-docs\n" +
                "=================================================",
                applicationName,
                serverPort,
                serverPort
        );
    }
}
