package com.example.dzcom.interfaces.controller;

import com.example.dzcom.interfaces.controller.account.AdminUserController;
import com.example.dzcom.interfaces.controller.account.AdminRoleController;
import com.example.dzcom.interfaces.controller.account.AuthenticationController;
import com.example.dzcom.interfaces.controller.account.CurrentUserController;
import com.example.dzcom.interfaces.controller.ai.AiModelController;
import com.example.dzcom.interfaces.controller.ai.InvestmentAnalysisController;
import com.example.dzcom.interfaces.controller.product.AdminProductController;
import com.example.dzcom.interfaces.controller.product.ProductController;
import com.example.dzcom.interfaces.controller.task.InvestmentTaskController;
import com.example.dzcom.interfaces.dto.response.account.PreferenceResponse;
import com.example.dzcom.interfaces.dto.response.account.RoleResponse;
import com.example.dzcom.interfaces.dto.response.account.UserResponse;
import com.example.dzcom.interfaces.dto.response.ai.AiModelResponse;
import com.example.dzcom.interfaces.dto.response.ai.InvestmentAnalysisReportResponse;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.dto.response.market.MarketQuoteResponse;
import com.example.dzcom.interfaces.dto.response.product.ProductAttributeResponse;
import com.example.dzcom.interfaces.dto.response.product.ProductInvestmentProfileResponse;
import com.example.dzcom.interfaces.dto.response.product.ProductResponse;
import com.example.dzcom.interfaces.dto.response.product.ProductThemeRelationResponse;
import com.example.dzcom.interfaces.dto.response.task.InvestmentTaskDefinitionResponse;
import com.example.dzcom.interfaces.dto.response.task.InvestmentTaskTriggerResponse;
import com.example.dzcom.interfaces.dto.response.task.InvestmentThemeSnapshotResponse;
import com.example.dzcom.interfaces.dto.response.task.NewsArticleRelationResponse;
import com.example.dzcom.interfaces.dto.response.task.NewsArticleResponse;
import com.example.dzcom.interfaces.dto.response.task.ScheduledTaskExecutionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 校验 Controller 统一使用 POST 和 JSON 请求体的接口协议铁律。
 */
class ControllerRequestContractTest {

    private static final List<Class<?>> CONTROLLERS = List.of(
        AdminUserController.class,
        AdminRoleController.class,
        AuthenticationController.class,
        CurrentUserController.class,
        AdminProductController.class,
        ProductController.class,
        InvestmentTaskController.class,
        AiModelController.class,
        InvestmentAnalysisController.class
    );

    private static final List<Class<?>> RESPONSE_DTOS = List.of(
        UserResponse.class,
        RoleResponse.class,
        PreferenceResponse.class,
        ProductResponse.class,
        ProductAttributeResponse.class,
        ProductInvestmentProfileResponse.class,
        ProductThemeRelationResponse.class,
        MarketQuoteResponse.class,
        InvestmentTaskDefinitionResponse.class,
        InvestmentTaskTriggerResponse.class,
        ScheduledTaskExecutionResponse.class,
        NewsArticleResponse.class,
        NewsArticleRelationResponse.class,
        InvestmentThemeSnapshotResponse.class,
        AiModelResponse.class,
        InvestmentAnalysisReportResponse.class,
        PageResponse.class
    );

    /**
     * 校验所有公开 Controller 方法使用静态 POST 路径。
     *
     * @author dz
     * @date 2026-06-14
     */
    @Test
    void publicEndpointsMustUseStaticPostMappings() {
        CONTROLLERS.stream()
            .flatMap(controller -> List.of(controller.getDeclaredMethods()).stream())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .forEach(this::assertStaticPostMapping);
    }

    /**
     * 校验所有业务请求参数通过请求体传递。
     *
     * @author dz
     * @date 2026-06-14
     */
    @Test
    void businessParametersMustUseRequestBody() {
        CONTROLLERS.stream()
            .flatMap(controller -> List.of(controller.getDeclaredMethods()).stream())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .flatMap(method -> List.of(method.getParameters()).stream())
            .filter(parameter -> parameter.getType() != HttpServletResponse.class)
            .forEach(this::assertRequestBodyParameter);
    }

    /**
     * 校验 Controller 分组和接口说明完整，避免 Swagger 只展示方法名或空描述。
     *
     * @author dz
     * @date 2026-06-15
     */
    @Test
    void swaggerDocumentationMustBeDetailed() {
        CONTROLLERS.forEach(controller -> {
            Tag tag = controller.getAnnotation(Tag.class);
            assertNotNull(tag, controller + " 必须声明 @Tag");
            assertFalse(tag.name().isBlank(), controller + " 的 @Tag.name 不能为空");
            assertFalse(tag.description().isBlank(), controller + " 的 @Tag.description 不能为空");
        });
        CONTROLLERS.stream()
            .flatMap(controller -> List.of(controller.getDeclaredMethods()).stream())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .forEach(this::assertDetailedSwaggerDocumentation);
    }

    /**
     * 校验 Controller 返回类型和接口响应 DTO 不得暴露应用层或领域层对象。
     *
     * @author dz
     * @date 2026-06-15
     */
    @Test
    void controllerResponsesMustKeepLayerBoundaries() {
        CONTROLLERS.stream()
            .flatMap(controller -> List.of(controller.getDeclaredMethods()).stream())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .forEach(method -> assertNoInternalType(
                method.getGenericReturnType().getTypeName(),
                method + " 的返回类型"
            ));
        RESPONSE_DTOS.stream()
            .flatMap(response -> List.of(response.getRecordComponents()).stream())
            .map(RecordComponent::getGenericType)
            .forEach(type -> assertNoInternalType(type.getTypeName(), "接口响应 DTO 字段"));
    }

    /**
     * 校验接口方法只声明固定的 POST 映射路径。
     *
     * @param method 待校验的 Controller 方法
     * @author dz
     * @date 2026-06-14
     */
    private void assertStaticPostMapping(Method method) {
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        boolean usesPost = postMapping != null || requestMapping != null
            && List.of(requestMapping.method()).contains(RequestMethod.POST);
        assertTrue(usesPost, method + " 必须映射为 POST 请求");
        String[] paths = postMapping != null ? postMapping.value() : requestMapping.value();
        List.of(paths).forEach(path ->
            assertFalse(path.contains("{"), method + " 禁止使用动态路径参数"));
    }

    /**
     * 校验业务参数只使用请求体绑定，不使用 URL 参数绑定。
     *
     * @param parameter 待校验的方法参数
     * @author dz
     * @date 2026-06-14
     */
    private void assertRequestBodyParameter(Parameter parameter) {
        assertTrue(parameter.isAnnotationPresent(RequestBody.class),
            parameter + " 必须使用 @RequestBody");
        assertFalse(parameter.isAnnotationPresent(RequestParam.class),
            parameter + " 禁止使用 @RequestParam");
        assertFalse(parameter.isAnnotationPresent(PathVariable.class),
            parameter + " 禁止使用 @PathVariable");
    }

    /**
     * 校验接口具有完整操作说明、真实成功返回模型和系统错误响应。
     *
     * @param method 待校验的 Controller 方法
     * @author dz
     * @date 2026-06-15
     */
    private void assertDetailedSwaggerDocumentation(Method method) {
        Operation operation = method.getAnnotation(Operation.class);
        assertNotNull(operation, method + " 必须声明 @Operation");
        assertFalse(operation.summary().isBlank(), method + " 的 @Operation.summary 不能为空");
        assertFalse(operation.description().isBlank(), method + " 的 @Operation.description 不能为空");

        ApiResponses responses = method.getAnnotation(ApiResponses.class);
        assertNotNull(responses, method + " 必须声明 @ApiResponses");
        List<ApiResponse> responseList = List.of(responses.value());
        assertTrue(responseList.stream()
                .filter(response -> response.responseCode().startsWith("2"))
                .anyMatch(ApiResponse::useReturnTypeSchema),
            method + " 的成功响应必须使用方法真实返回类型生成 schema");
        assertTrue(responseList.stream()
                .anyMatch(response -> "500".equals(response.responseCode())),
            method + " 必须说明 500 系统错误响应");
    }

    /**
     * 校验接口公开结构不引用内部层类型。
     *
     * @param typeName 待校验的完整类型名称
     * @param source 类型来源说明
     * @author dz
     * @date 2026-06-15
     */
    private void assertNoInternalType(String typeName, String source) {
        assertFalse(typeName.contains(".domain."), source + " 禁止暴露 Domain 类型: " + typeName);
        assertFalse(typeName.contains(".infrastructure."), source + " 禁止暴露基础设施类型: " + typeName);
        assertFalse(typeName.contains(".application.dto."), source + " 禁止暴露应用层 DTO: " + typeName);
        assertFalse(typeName.contains(".application.common.page."),
            source + " 禁止暴露应用层分页对象: " + typeName);
    }
}
