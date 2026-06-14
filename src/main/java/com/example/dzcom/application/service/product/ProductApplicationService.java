package com.example.dzcom.application.service.product;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.example.dzcom.application.assembler.product.ProductViewAssembler;
import com.example.dzcom.application.command.product.CreateProductCommand;
import com.example.dzcom.application.command.product.SaveProductAttributeCommand;
import com.example.dzcom.application.command.product.UpdateProductCommand;
import com.example.dzcom.application.dto.product.ProductView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.example.dzcom.common.exception.BusinessException;
import com.example.dzcom.common.service.ClockProvider;
import com.example.dzcom.common.service.IdGenerator;
import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.model.product.ProductAttribute;
import com.example.dzcom.domain.repository.product.ProductStore;
import com.example.dzcom.domain.repository.product.ProductAttributeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 产品中心管理用例。
 *
 * <p>所有写操作要求 ADMIN 角色，并在同一事务内完成业务校验和持久化。
 * 代码层预检查用于给出可读错误，数据库唯一索引仍是并发场景的最终约束。</p>
 */
@Service
@RequiredArgsConstructor
public class ProductApplicationService {
    private static final Set<String> ATTRIBUTE_VALUE_TYPES =
        Set.of("STRING", "NUMBER", "BOOLEAN", "DATE", "JSON");

    private final ProductStore store;
    private final ProductAttributeStore attributes;
    private final ProductViewAssembler assembler;
    private final CurrentOperatorProvider currentOperator;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /**
     * 创建或保存对应的业务数据。
     *
     * @param command 应用用例命令
     * @return 方法执行后的结果
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public ProductView create(CreateProductCommand command) {
        CurrentOperator operator = requiredAdmin();
        String marketCode = normalizeUpper(command.marketCode(), "OTC");
        String productCode = normalizeUpper(command.productCode(), null);
        if (store.existsByMarketAndCode(marketCode, productCode)) {
            throw new BusinessException(HttpStatus.CONFLICT, "同一市场下产品代码已存在");
        }
        String bizId = ids.newBizId();
        LocalDateTime now = clock.now();
        Product product = Product.create(
            bizId, "P" + bizId.replace("-", "").substring(0, 20).toUpperCase(),
            productCode, command.productName(), command.productType(), marketCode,
            normalizeUpper(command.currency(), "CNY"), command.riskLevel(),
            command.minInvestAmount(), command.amountStep(), command.quantityStep(),
            command.feeRate(), command.listingDate(), command.delistingDate(),
            command.description(), operator.userBizId(), now
        );
        return assembler.assembleDetail(store.save(product), java.util.List.of());
    }

    /**
     * 更新对应的业务数据。
     *
     * @param bizId 业务对象的唯一标识
     * @param command 应用用例命令
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public ProductView update(String bizId, UpdateProductCommand command) {
        CurrentOperator operator = requiredAdmin();
        Product product = requiredProduct(bizId);
        product.updateDetails(command.productName(), command.riskLevel(),
            command.minInvestAmount(), command.amountStep(), command.quantityStep(),
            command.feeRate(), command.listingDate(), command.delistingDate(),
            command.description(), operator.userBizId(), clock.now());
        return assembler.assembleDetail(store.save(product), attributes.findByProductBizId(bizId));
    }

    /**
     * 执行 change status 处理。
     *
     * @param bizId 业务对象的唯一标识
     * @param status 目标状态或目标值
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public ProductView changeStatus(String bizId, ProductTradeStatus status) {
        CurrentOperator operator = requiredAdmin();
        Product product = requiredProduct(bizId);
        product.changeTradeStatus(status, operator.userBizId(), clock.now());
        return assembler.assembleDetail(store.save(product), attributes.findByProductBizId(bizId));
    }

    /**
     * 创建或保存对应的业务数据。
     *
     * @param productBizId 业务对象的唯一标识
     * @param command 应用用例命令
     * @return 方法执行后的结果
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public ProductView saveAttribute(String productBizId, SaveProductAttributeCommand command) {
        requiredAdmin();
        Product product = requiredProduct(productBizId);
        String valueType = normalizeUpper(command.valueType(), "STRING");
        if (!ATTRIBUTE_VALUE_TYPES.contains(valueType)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不支持的产品属性值类型");
        }
        validateJson(command.jsonValue());
        LocalDate effectiveDate = command.effectiveDate() == null
            ? LocalDate.of(1970, 1, 1) : command.effectiveDate();
        LocalDateTime now = clock.now();
        ProductAttribute existing = attributes.find(productBizId, command.key(), effectiveDate, true)
            .orElse(null);
        ProductAttribute attribute = ProductAttribute.builder()
            .bizId(existing == null ? ids.newBizId() : existing.bizId())
            .productBizId(productBizId)
            .key(command.key().trim())
            .valueType(valueType)
            .jsonValue(command.jsonValue())
            .effectiveDate(effectiveDate)
            .sourceCode(command.sourceCode())
            .createdAt(existing == null ? now : existing.createdAt())
            .updatedAt(now)
            .deleted(0)
            .build();
        attributes.save(attribute);
        return assembler.assembleDetail(product, attributes.findByProductBizId(productBizId));
    }

    /**
     * 删除或逻辑删除对应的业务数据。
     *
     * @param bizId 业务对象的唯一标识
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public void delete(String bizId) {
        CurrentOperator operator = requiredAdmin();
        Product product = requiredProduct(bizId);
        product.delete(operator.userBizId(), clock.now());
        store.save(product);
    }

    /**
     * 获取必需的业务对象，不存在时终止当前流程。
     *
     * @param bizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    private Product requiredProduct(String bizId) {
        return store.findByBizId(bizId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "产品不存在"));
    }

    /**
     * 获取必需的业务对象，不存在时终止当前流程。
     *
     * @return 查询到的业务数据
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private CurrentOperator requiredAdmin() {
        CurrentOperator operator = currentOperator.required();
        if (!operator.hasRole("ADMIN")) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }
        return operator;
    }

    /**
     * 规范化输入值并返回统一格式。
     *
     * @param value 待处理的数据值
     * @param defaultValue defaultValue 参数
     * @return 方法执行后的结果
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private String normalizeUpper(String value, String defaultValue) {
        String normalized = value == null || value.isBlank() ? defaultValue : value.trim();
        if (normalized == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "必填编码不能为空");
        }
        return normalized.toUpperCase(java.util.Locale.ROOT);
    }

    /**
     * 在进入持久化层前校验 JSON，保证不同数据库方言返回一致的 400 业务错误， 而不是依赖 MySQL JSON 列在事务提交时抛出难以理解的 SQL 异常。
     *
     * @param jsonValue jsonValue 参数
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private void validateJson(String jsonValue) {
        try {
            JSON.parse(jsonValue);
        } catch (JSONException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "产品属性值必须是合法 JSON");
        }
    }
}
