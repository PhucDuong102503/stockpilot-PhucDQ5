package com.stockpilot.service.discount;

import java.math.BigDecimal;

public class NoDiscount implements DiscountPolicy {
    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        return BigDecimal.ZERO;
    }
}
