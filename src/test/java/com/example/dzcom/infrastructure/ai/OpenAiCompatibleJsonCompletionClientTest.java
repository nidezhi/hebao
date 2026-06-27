package com.example.dzcom.infrastructure.ai;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** OpenAI-compatible JSON 客户端测试。 */
class OpenAiCompatibleJsonCompletionClientTest {
    @Test
    void shouldExtractJsonObjectFromMarkdownFence() throws Exception {
        OpenAiCompatibleJsonCompletionClient client =
            new OpenAiCompatibleJsonCompletionClient(JsonMapper.builder().findAndAddModules().build());

        String normalized = normalize(client, """
            ```json
            {"candidates":[]}
            ```
            """);

        assertEquals("{\"candidates\":[]}", normalized);
    }

    @Test
    void shouldExtractFirstJsonObjectFromWrappedText() throws Exception {
        OpenAiCompatibleJsonCompletionClient client =
            new OpenAiCompatibleJsonCompletionClient(JsonMapper.builder().findAndAddModules().build());

        String normalized = normalize(client, "整理结果如下：{\"candidates\":[{\"sourceCode\":\"CSRC\"}]}请查收。");

        assertEquals("{\"candidates\":[{\"sourceCode\":\"CSRC\"}]}", normalized);
    }

    private String normalize(OpenAiCompatibleJsonCompletionClient client, String value) throws Exception {
        Method method = OpenAiCompatibleJsonCompletionClient.class
            .getDeclaredMethod("normalizeJsonObjectContent", String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(client, value, "TEST_OPERATION");
    }
}
