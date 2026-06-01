package com.example.dzcom.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Knife4j配置类
 * 配置OpenAPI文档信息和Knife4j增强功能
 */
@Configuration
public class Knife4jConfig {
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${spring.application.name:AI理财平台}")
    private String applicationName;
    
    /**
     * 自定义OpenAPI配置
     * 包含API文档的基本信息、联系方式、许可证和安全认证方案
     *
     * @return OpenAPI实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("AI理财平台 API文档")
                .description("AI驱动的理财产品平台接口文档，提供用户管理、产品查询、投资组合管理等核心功能")
                .version("1.0.0")
                .contact(new Contact()
                    .name("技术团队")
                    .email("tech@aiwealth.com")
                    .url("https://github.com/your-repo"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(
                new Server().url("http://localhost:" + serverPort).description("本地开发环境"),
                new Server().url("https://api.aiwealth.com").description("生产环境")
            ))
            .components(new Components()
                .addSecuritySchemes("cookie-auth", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("AUTH_TOKEN")
                        .description("Cookie认证令牌，用于登录验证")));
    }
}
