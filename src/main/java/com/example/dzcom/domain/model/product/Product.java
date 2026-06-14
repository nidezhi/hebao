package com.example.dzcom.domain.model.product;

import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 产品目录聚合根。
 *
 * <p>产品只保存稳定主数据和交易约束，不承载实时价格、持仓或收益。金额和数量均使用
 * {@link BigDecimal}，避免金融计算中使用浮点数造成精度损失。状态和删除操作只能通过
 * 领域行为变更，应用服务不能直接拼装非法生命周期。</p>
 */
@Getter
@Builder(toBuilder = true)
public class Product {
    /** 跨领域引用和 API 输出使用的稳定业务标识。 */
    private final String bizId;
    /** 平台内部产品编号，创建后不可变。 */
    private final String productNo;
    /** 市场或销售渠道内的产品代码。 */
    private final String productCode;
    private String productName;
    private final ProductType productType;
    private final String marketCode;
    private final String currency;
    private ProductTradeStatus tradeStatus;
    private int riskLevel;
    private BigDecimal minInvestAmount;
    private BigDecimal amountStep;
    private BigDecimal quantityStep;
    private BigDecimal feeRate;
    private LocalDate listingDate;
    private LocalDate delistingDate;
    private String description;
    private int version;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final String createdBy;
    private String updatedBy;
    private int deleted;
    private LocalDateTime deletedAt;

    /**
     * 创建新的产品目录项。 市场代码、产品代码和类型共同表达产品身份；创建后不得通过普通更新接口修改， 避免历史行情、订单和持仓引用发生语义漂移。
     *
     * @param bizId 业务对象的唯一标识
     * @param productNo productNo 参数
     * @param productCode productCode 参数
     * @param productName productName 参数
     * @param productType productType 参数
     * @param marketCode marketCode 参数
     * @param currency currency 参数
     * @param riskLevel riskLevel 参数
     * @param minInvestAmount minInvestAmount 参数
     * @param amountStep amountStep 参数
     * @param quantityStep quantityStep 参数
     * @param feeRate feeRate 参数
     * @param listingDate listingDate 参数
     * @param delistingDate delistingDate 参数
     * @param description description 参数
     * @param operator 当前操作人标识
     * @param now 当前业务时间
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public static Product create(String bizId, String productNo, String productCode, String productName,
                                 ProductType productType, String marketCode, String currency,
                                 int riskLevel, BigDecimal minInvestAmount, BigDecimal amountStep,
                                 BigDecimal quantityStep, BigDecimal feeRate, LocalDate listingDate,
                                 LocalDate delistingDate, String description, String operator,
                                 LocalDateTime now) {
        validateRiskLevel(riskLevel);
        validateNonNegative("最小投资金额", minInvestAmount);
        validateNonNegative("金额递增步长", amountStep);
        validateNonNegative("数量递增步长", quantityStep);
        validateRate(feeRate);
        validateDateRange(listingDate, delistingDate);
        return Product.builder()
            .bizId(requireText("产品业务标识", bizId))
            .productNo(requireText("产品编号", productNo))
            .productCode(requireText("产品代码", productCode))
            .productName(requireText("产品名称", productName))
            .productType(productType)
            .marketCode(requireText("市场代码", marketCode))
            .currency(requireText("币种", currency))
            .tradeStatus(ProductTradeStatus.TRADABLE)
            .riskLevel(riskLevel)
            .minInvestAmount(minInvestAmount)
            .amountStep(amountStep)
            .quantityStep(quantityStep)
            .feeRate(feeRate)
            .listingDate(listingDate)
            .delistingDate(delistingDate)
            .description(description)
            .version(0)
            .createdAt(now)
            .updatedAt(now)
            .createdBy(operator)
            .updatedBy(operator)
            .deleted(0)
            .build();
    }

    /**
     * 更新允许变化的产品资料，稳定身份字段不在该行为的参数中。
     *
     * @param productName productName 参数
     * @param riskLevel riskLevel 参数
     * @param minInvestAmount minInvestAmount 参数
     * @param amountStep amountStep 参数
     * @param quantityStep quantityStep 参数
     * @param feeRate feeRate 参数
     * @param listingDate listingDate 参数
     * @param delistingDate delistingDate 参数
     * @param description description 参数
     * @param operator 当前操作人标识
     * @param now 当前业务时间
     * @author dz
     * @date 2026-06-14
     */
    public void updateDetails(String productName, int riskLevel, BigDecimal minInvestAmount,
                              BigDecimal amountStep, BigDecimal quantityStep, BigDecimal feeRate,
                              LocalDate listingDate, LocalDate delistingDate, String description,
                              String operator, LocalDateTime now) {
        ensureExists();
        validateRiskLevel(riskLevel);
        validateNonNegative("最小投资金额", minInvestAmount);
        validateNonNegative("金额递增步长", amountStep);
        validateNonNegative("数量递增步长", quantityStep);
        validateRate(feeRate);
        validateDateRange(listingDate, delistingDate);
        this.productName = requireText("产品名称", productName);
        this.riskLevel = riskLevel;
        this.minInvestAmount = minInvestAmount;
        this.amountStep = amountStep;
        this.quantityStep = quantityStep;
        this.feeRate = feeRate;
        this.listingDate = listingDate;
        this.delistingDate = delistingDate;
        this.description = description;
        touch(operator, now);
    }

    /**
     * 变更交易状态；逻辑删除后的产品不能被重新启用。
     *
     * @param target 目标状态或目标值
     * @param operator 当前操作人标识
     * @param now 当前业务时间
     * @throws IllegalArgumentException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    public void changeTradeStatus(ProductTradeStatus target, String operator, LocalDateTime now) {
        ensureExists();
        if (target == null) {
            throw new IllegalArgumentException("产品交易状态不能为空");
        }
        tradeStatus = target;
        touch(operator, now);
    }

    /**
     * 幂等逻辑删除产品，并同步禁止后续交易。
     *
     * @param operator 当前操作人标识
     * @param now 当前业务时间
     * @author dz
     * @date 2026-06-14
     */
    public void delete(String operator, LocalDateTime now) {
        if (deleted == 0) {
            deleted = 1;
            deletedAt = now;
            tradeStatus = ProductTradeStatus.DISABLED;
            touch(operator, now);
        }
    }

    /**
     * 执行 ensure exists 处理。
     *
     * @throws IllegalStateException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private void ensureExists() {
        if (deleted == 1) {
            throw new IllegalStateException("产品不存在");
        }
    }

    /**
     * 执行 touch 处理。
     *
     * @param operator 当前操作人标识
     * @param now 当前业务时间
     * @author dz
     * @date 2026-06-14
     */
    private void touch(String operator, LocalDateTime now) {
        updatedBy = operator;
        updatedAt = now;
    }

    /**
     * 校验输入值是否满足业务约束。
     *
     * @param riskLevel riskLevel 参数
     * @throws IllegalArgumentException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private static void validateRiskLevel(int riskLevel) {
        if (riskLevel < 1 || riskLevel > 5) {
            throw new IllegalArgumentException("产品风险等级必须在 1 到 5 之间");
        }
    }

    /**
     * 校验输入值是否满足业务约束。
     *
     * @param field field 参数
     * @param value 待处理的数据值
     * @throws IllegalArgumentException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private static void validateNonNegative(String field, BigDecimal value) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(field + "不能为负数");
        }
    }

    /**
     * 校验输入值是否满足业务约束。
     *
     * @param value 待处理的数据值
     * @throws IllegalArgumentException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private static void validateRate(BigDecimal value) {
        validateNonNegative("费率", value);
        if (value.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("费率不能大于 1");
        }
    }

    /**
     * 校验输入值是否满足业务约束。
     *
     * @param listingDate listingDate 参数
     * @param delistingDate delistingDate 参数
     * @throws IllegalArgumentException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private static void validateDateRange(LocalDate listingDate, LocalDate delistingDate) {
        if (listingDate != null && delistingDate != null && delistingDate.isBefore(listingDate)) {
            throw new IllegalArgumentException("停止销售日期不能早于上市日期");
        }
    }

    /**
     * 执行 require text 处理。
     *
     * @param field field 参数
     * @param value 待处理的数据值
     * @return 方法执行后的结果
     * @throws IllegalArgumentException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private static String requireText(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + "不能为空");
        }
        return value.trim();
    }
}
