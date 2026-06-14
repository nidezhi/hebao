package com.example.dzcom.domain.model.product;

import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 产品聚合规则测试。
 *
 * <p>测试聚焦跨字段和生命周期约束；长度、格式等协议校验由接口层验证注解负责。</p>
 */
class ProductTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 8, 10, 0);

    /**
     * 执行 should create tradable product with stable identity 处理。
     *
     * @author dz
     * @date 2026-06-14
     */
    @Test
    void shouldCreateTradableProductWithStableIdentity() {
        Product product = createProduct();

        assertEquals(ProductTradeStatus.TRADABLE, product.getTradeStatus());
        assertEquals("000001", product.getProductCode());
        assertEquals(0, product.getDeleted());
    }

    /**
     * 执行 should reject invalid risk and date range 处理。
     *
     * @author dz
     * @date 2026-06-14
     */
    @Test
    void shouldRejectInvalidRiskAndDateRange() {
        assertThrows(IllegalArgumentException.class, () -> Product.create(
            "product-id", "P0001", "000001", "测试产品", ProductType.STOCK,
            "SSE", "CNY", 6, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE,
            BigDecimal.ZERO, LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 1),
            null, "admin", NOW
        ));
    }

    /**
     * 执行 delete should be idempotent and prevent reactivation 处理。
     *
     * @author dz
     * @date 2026-06-14
     */
    @Test
    void deleteShouldBeIdempotentAndPreventReactivation() {
        Product product = createProduct();
        product.delete("admin", NOW.plusMinutes(1));
        product.delete("admin", NOW.plusMinutes(2));

        assertEquals(1, product.getDeleted());
        assertEquals(ProductTradeStatus.DISABLED, product.getTradeStatus());
        assertTrue(product.getDeletedAt().isEqual(NOW.plusMinutes(1)));
        assertThrows(IllegalStateException.class,
            () -> product.changeTradeStatus(ProductTradeStatus.TRADABLE, "admin", NOW.plusMinutes(3)));
    }

    /**
     * 执行 create product 处理。
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    private Product createProduct() {
        return Product.create(
            "product-id", "P0001", "000001", "测试产品", ProductType.STOCK,
            "SSE", "CNY", 3, new BigDecimal("100.0000"), BigDecimal.ZERO,
            new BigDecimal("1.00000000"), new BigDecimal("0.00100000"),
            LocalDate.of(2026, 1, 1), null, "测试产品", "admin", NOW
        );
    }
}
