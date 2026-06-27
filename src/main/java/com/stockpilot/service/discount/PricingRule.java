package com.stockpilot.service.discount;

import java.math.BigDecimal;

/**
 * Functional interface for custom pricing rules.
 */
@FunctionalInterface
public interface PricingRule {
    BigDecimal apply(BigDecimal originalPrice, int quantity);
}
