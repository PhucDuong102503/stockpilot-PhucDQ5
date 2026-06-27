package com.stockpilot.service.discount;

import java.math.BigDecimal;

public class BulkDiscount implements DiscountPolicy {
    private final BigDecimal threshold;
    private final BigDecimal discountAmount;

    public BulkDiscount(BigDecimal threshold, BigDecimal discountAmount) {
        this.threshold = threshold;
        this.discountAmount = discountAmount;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(threshold) > 0) {
            return discountAmount;
        }
        return BigDecimal.ZERO;
    }
}
