package com.example.dzcom.interfaces.controller;

import com.example.dzcom.interfaces.controller.account.AdminUserController;
import com.example.dzcom.interfaces.controller.account.AuthenticationController;
import com.example.dzcom.interfaces.controller.account.CurrentUserController;
import com.example.dzcom.interfaces.controller.product.AdminProductController;
import com.example.dzcom.interfaces.controller.product.ProductController;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 校验 Controller 统一使用 POST 和 JSON 请求体的接口协议铁律。
 */
class ControllerRequestContractTest {

    private static final List<Class<?>> CONTROLLERS = List.of(
        AdminUserController.class,
        AuthenticationController.class,
        CurrentUserController.class,
        AdminProductController.class,
        ProductController.class
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
}
