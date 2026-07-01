package com.example.dzcom;

import com.example.dzcom.infrastructure.ai.MockOpenAiCompatibleInvestmentAnalysisProvider;
import com.example.dzcom.infrastructure.ai.OpenAiCompatibleJsonCompletionClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DzcomApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 验证 Spring 启动上下文可以装配核心 AI Bean。
     *
     * <p>模型调用审计接入后，AI 客户端存在测试兼容构造器和生产构造器；本测试用于防止
     * Spring 再次误选无参构造路径导致后端无法启动。</p>
     */
    @Test
    void contextLoads() {
        assertThat(applicationContext.getBean(MockOpenAiCompatibleInvestmentAnalysisProvider.class)).isNotNull();
        assertThat(applicationContext.getBean(OpenAiCompatibleJsonCompletionClient.class)).isNotNull();
    }

}
