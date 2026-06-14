package com.example.dzcom.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 配置。
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 执行 custom open api 处理。
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("DZCOM API 文档")
                .description("DZCOM 项目接口文档")
                .version("0.1.0")
                .contact(new Contact()
                    .name("技术团队")
                    .email("tech@example.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("本地开发环境")
            ));
    }
}
