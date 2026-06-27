package com.stockpilot.service.discount;

import java.math.BigDecimal;

/**
 * Strategy interface for calculating discounts.
 */
public interface DiscountPolicy {
    /**
     * Calculate the discount amount based on the subtotal.
     * @param subtotal The total amount before discount.
     * @return The discount amount to be subtracted.
     */
    BigDecimal calculateDiscount(BigDecimal subtotal);
}
