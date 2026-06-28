package com.example.dzcom.application.service.product;

import com.example.dzcom.application.common.json.Jsons;
import com.example.dzcom.application.assembler.product.ProductViewAssembler;
import com.example.dzcom.application.command.product.CreateProductCommand;
import com.example.dzcom.application.command.product.SaveProductAttributeCommand;
import com.example.dzcom.application.command.product.SaveProductInvestmentProfileCommand;
import com.example.dzcom.application.command.product.UpdateProductCommand;
import com.example.dzcom.application.dto.product.ProductView;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.AuthorizationService;
import com.example.dzcom.application.service.account.PermissionCodes;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.model.product.ProductAttribute;
import com.example.dzcom.domain.model.product.ProductInvestmentProfile;
import com.example.dzcom.domain.model.product.ProductThemeRelation;
import com.example.dzcom.domain.repository.product.ProductInvestmentProfileStore;
import com.example.dzcom.domain.repository.product.ProductStore;
import com.example.dzcom.domain.repository.product.ProductAttributeStore;
import com.example.dzcom.domain.repository.product.ProductThemeRelationStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 产品中心管理用例。
 *
 * <p>所有写操作要求产品目录管理权限，并在同一事务内完成业务校验和持久化。
 * 代码层预检查用于给出可读错误，数据库唯一索引仍是并发场景的最终约束。</p>
 */
@Service
@RequiredArgsConstructor
public class ProductApplicationService {
    private static final Set<String> ATTRIBUTE_VALUE_TYPES =
        Set.of("STRING", "NUMBER", "BOOLEAN", "DATE", "JSON");
    private static final Set<String> LEVELS = Set.of("LOW", "MEDIUM", "HIGH");
    private static final Set<String> RELATION_TYPES =
        Set.of("THEME", "INDUSTRY", "INDEX", "ASSET_CLASS");

    private final ProductStore store;
    private final ProductAttributeStore attributes;
    private final ProductInvestmentProfileStore investmentProfiles;
    private final ProductThemeRelationStore themeRelations;
    private final ProductViewAssembler assembler;
    private final AuthorizationService authorization;
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
        CurrentOperator operator = requiredManager();
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
        CurrentOperator operator = requiredManager();
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
        CurrentOperator operator = requiredManager();
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
        requiredManager();
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
     * 保存产品投资风险画像、交易约束和主题关系。
     *
     * <p>该用例是阶段 1 产品池建设的前端可见出口。保存后，产品详情会展示资产类别、
     * 风险摘要、波动和流动性、Mock 交易开关、最短持有天数，以及主题/行业/指数关系。</p>
     *
     * @param command 产品投资画像和主题关系保存命令
     * @return 包含投资画像和主题关系的产品详情
     * @throws BusinessException 当产品不存在或画像参数不合法时抛出
     * @author dz
     * @date 2026-06-22
     */
    @Transactional
    public ProductView saveInvestmentProfile(SaveProductInvestmentProfileCommand command) {
        requiredManager();
        Product product = requiredProduct(command.productBizId());
        LocalDateTime now = clock.now();
        ProductInvestmentProfile existed = investmentProfiles
            .findByProductBizId(command.productBizId())
            .orElse(null);
        ProductInvestmentProfile profile = buildInvestmentProfile(command, existed, now);
        List<ProductThemeRelation> relations = buildThemeRelations(command, now);
        investmentProfiles.save(profile);
        themeRelations.replaceByProductBizId(command.productBizId(), relations);
        return assembler.assembleInvestmentDetail(
            product,
            attributes.findByProductBizId(command.productBizId()),
            java.util.Optional.of(profile),
            relations
        );
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
        CurrentOperator operator = requiredManager();
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
     * 构建产品投资画像领域对象。
     *
     * @param command 产品投资画像保存命令
     * @param existed 已存在的产品投资画像
     * @param now 当前北京时间
     * @return 产品投资画像领域对象
     * @author dz
     * @date 2026-06-22
     */
    private ProductInvestmentProfile buildInvestmentProfile(
        SaveProductInvestmentProfileCommand command,
        ProductInvestmentProfile existed,
        LocalDateTime now
    ) {
        int suitableRiskLevel = command.suitableRiskLevel() == null
            ? 3
            : command.suitableRiskLevel();
        validateRiskLevel(suitableRiskLevel);
        validateLevel(command.volatilityLevel(), "波动等级");
        validateLevel(command.liquidityLevel(), "流动性等级");
        validateQualityScore(command.dataQualityScore());
        return ProductInvestmentProfile.builder()
            .bizId(existed == null ? ids.newBizId() : existed.bizId())
            .productBizId(command.productBizId())
            .assetClass(normalizeUpper(command.assetClass(), null))
            .riskSummary(command.riskSummary())
            .volatilityLevel(normalizeUpper(command.volatilityLevel(), "MEDIUM"))
            .liquidityLevel(normalizeUpper(command.liquidityLevel(), "MEDIUM"))
            .maxDrawdown(command.maxDrawdown())
            .suitableRiskLevel(suitableRiskLevel)
            .mockTradable(Boolean.TRUE.equals(command.mockTradable()))
            .minHoldingDays(command.minHoldingDays() == null ? 0 : command.minHoldingDays())
            .tradingNotes(command.tradingNotes())
            .dataQualityScore(command.dataQualityScore() == null
                ? java.math.BigDecimal.ZERO
                : command.dataQualityScore())
            .createdAt(existed == null ? now : existed.createdAt())
            .updatedAt(now)
            .build();
    }

    /**
     * 构建产品主题关系领域对象集合。
     *
     * @param command 产品投资画像保存命令
     * @param now 当前北京时间
     * @return 产品主题、行业、指数和资产类别关系列表
     * @author dz
     * @date 2026-06-22
     */
    private List<ProductThemeRelation> buildThemeRelations(
        SaveProductInvestmentProfileCommand command,
        LocalDateTime now
    ) {
        if (command.relations() == null) {
            return java.util.List.of();
        }
        return command.relations().stream()
            .map(relation -> buildThemeRelation(command.productBizId(), relation, now))
            .toList();
    }

    /**
     * 构建单条产品主题关系。
     *
     * @param productBizId 产品业务唯一标识
     * @param relation 产品主题关系命令项
     * @param now 当前北京时间
     * @return 产品主题关系领域对象
     * @author dz
     * @date 2026-06-22
     */
    private ProductThemeRelation buildThemeRelation(
        String productBizId,
        SaveProductInvestmentProfileCommand.Relation relation,
        LocalDateTime now
    ) {
        String relationType = normalizeUpper(relation.relationType(), null);
        if (!RELATION_TYPES.contains(relationType)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不支持的产品关系类型");
        }
        String relationName = relation.relationName() == null
            ? null
            : relation.relationName().trim();
        if (relationName == null || relationName.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "产品关系展示名称不能为空");
        }
        java.math.BigDecimal relationWeight = relation.relationWeight() == null
            ? java.math.BigDecimal.ONE
            : relation.relationWeight();
        validateRatio("关系权重", relationWeight);
        return ProductThemeRelation.builder()
            .bizId(ids.newBizId())
            .productBizId(productBizId)
            .relationType(relationType)
            .relationCode(normalizeUpper(relation.relationCode(), null))
            .relationName(relationName)
            .relationWeight(relationWeight)
            .sourceCode(normalizeUpper(relation.sourceCode(), "MANUAL"))
            .evidence(relation.evidence())
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    /**
     * 获取必需的业务对象，不存在时终止当前流程。
     *
     * @return 查询到的业务数据
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private CurrentOperator requiredManager() {
        return authorization.require(PermissionCodes.PRODUCT_CATALOG_MANAGE);
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
     * 校验用户或产品风险等级。
     *
     * @param riskLevel 风险等级，取值 1-5
     * @throws BusinessException 当风险等级不在允许范围内时抛出
     * @author dz
     * @date 2026-06-22
     */
    private void validateRiskLevel(int riskLevel) {
        if (riskLevel < 1 || riskLevel > 5) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "风险等级必须在1到5之间");
        }
    }

    /**
     * 校验波动或流动性等级。
     *
     * @param level 等级编码
     * @param fieldName 字段展示名称
     * @throws BusinessException 当等级不在 LOW/MEDIUM/HIGH 范围内时抛出
     * @author dz
     * @date 2026-06-22
     */
    private void validateLevel(String level, String fieldName) {
        String normalized = normalizeUpper(level, "MEDIUM");
        if (!LEVELS.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldName + "必须是LOW/MEDIUM/HIGH");
        }
    }

    /**
     * 校验产品画像数据质量分。
     *
     * @param value 数据质量分，允许为空；非空时必须在 0-1
     * @throws BusinessException 当质量分超出范围时抛出
     * @author dz
     * @date 2026-06-22
     */
    private void validateQualityScore(java.math.BigDecimal value) {
        if (value == null) {
            return;
        }
        validateRatio("画像数据质量分", value);
    }

    /**
     * 校验比例类字段。
     *
     * @param fieldName 字段展示名称
     * @param value 比例值，必须在 0-1
     * @throws BusinessException 当比例超出范围时抛出
     * @author dz
     * @date 2026-06-22
     */
    private void validateRatio(String fieldName, java.math.BigDecimal value) {
        if (value.compareTo(java.math.BigDecimal.ZERO) < 0
            || value.compareTo(java.math.BigDecimal.ONE) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldName + "必须在0到1之间");
        }
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
        if (!Jsons.isValid(jsonValue)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "产品属性值必须是合法 JSON");
        }
    }
}
