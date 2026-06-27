package com.stockpilot.service.discount;

import java.math.BigDecimal;

public class PercentageDiscount implements DiscountPolicy {
    private final BigDecimal percentage;

    public PercentageDiscount(double percentage) {
        this.percentage = BigDecimal.valueOf(percentage / 100.0);
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        return subtotal.multiply(percentage);
    }
}
