package com.example.dzcom;

import io.swagger.v3.oas.annotations.media.Schema;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 校验核心数据类型的类级和字段级业务说明完整。
 *
 * <p>持久化实体、领域对象、接口请求 DTO 和接口响应 DTO 是理解业务数据的主要入口，
 * 新增字段如果缺少 {@link Schema} 会直接导致本测试失败。</p>
 */
class DocumentationContractTest {
    private static final List<String> DOCUMENTED_PACKAGES = List.of(
        "com.example.dzcom.infrastructure.persistence.entity",
        "com.example.dzcom.domain.model",
        "com.example.dzcom.application.command",
        "com.example.dzcom.application.dto",
        "com.example.dzcom.interfaces.request",
        "com.example.dzcom.interfaces.dto.response"
    );

    /**
     * 校验核心数据类型和字段均声明非空 Schema 说明。
     *
     * @author dz
     * @date 2026-06-18
     */
    @Test
    void coreDataTypesMustHaveDetailedSchemaDescriptions() {
        DOCUMENTED_PACKAGES.stream()
            .flatMap(packageName -> scanClasses(packageName).stream())
            .forEach(this::assertDocumentedType);
    }

    /**
     * 扫描指定包下的全部独立数据类型。
     *
     * @param packageName 待扫描的基础包名
     * @return 包内可加载的数据类型集合
     * @author dz
     * @date 2026-06-18
     */
    private List<Class<?>> scanClasses(String packageName) {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
        return scanner.findCandidateComponents(packageName).stream()
            .<Class<?>>map(definition -> loadClass(definition.getBeanClassName()))
            .filter(type -> !type.getName().contains("$"))
            .filter(type -> !type.getSimpleName().endsWith("Test"))
            .toList();
    }

    /**
     * 校验单个类型及其全部业务字段具有 Schema 说明。
     *
     * @param type 待校验的数据类型
     * @author dz
     * @date 2026-06-18
     */
    private void assertDocumentedType(Class<?> type) {
        Schema typeSchema = type.getAnnotation(Schema.class);
        assertNotNull(typeSchema, type.getName() + " 必须声明类级 @Schema");
        assertNotNullDescription(typeSchema.description(), type.getName() + " 的类级 @Schema");

        List.of(type.getDeclaredFields()).stream()
            .filter(field -> !field.isSynthetic())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .forEach(this::assertDocumentedField);
    }

    /**
     * 校验单个业务字段具有非空 Schema 说明。
     *
     * @param field 待校验字段
     * @author dz
     * @date 2026-06-18
     */
    private void assertDocumentedField(Field field) {
        Schema fieldSchema = field.getAnnotation(Schema.class);
        assertNotNull(fieldSchema, field + " 必须声明字段级 @Schema");
        assertNotNullDescription(fieldSchema.description(), field + " 的字段级 @Schema");
    }

    /**
     * 校验 Schema description 不是空白字符串。
     *
     * @param description Schema 中的中文业务说明
     * @param source 断言失败时用于定位的类型或字段
     * @author dz
     * @date 2026-06-18
     */
    private void assertNotNullDescription(String description, String source) {
        assertNotNull(description, source + " description 不能为空");
        org.junit.jupiter.api.Assertions.assertFalse(
            description.isBlank(),
            source + " description 不能为空"
        );
    }

    /**
     * 按完整类名加载类型。
     *
     * @param className 完整类名
     * @return 已加载的 Class 对象
     * @throws IllegalStateException 当类无法加载时抛出
     * @author dz
     * @date 2026-06-18
     */
    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("无法加载文档契约校验类型: " + className, exception);
        }
    }
}
