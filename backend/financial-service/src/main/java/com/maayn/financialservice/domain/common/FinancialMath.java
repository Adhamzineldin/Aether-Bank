package com.maayn.financialservice.domain.common;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class FinancialMath {

    public static final int SCALE = 2;
    public static final MathContext MC = MathContext.DECIMAL64;

    private FinancialMath() {
    }

    public static BigDecimal money(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal percentToRate(BigDecimal percent) {
        return percent == null
                ? BigDecimal.ZERO
                : percent.divide(BigDecimal.valueOf(100), MC);
    }

    public static BigDecimal rateToPercent(BigDecimal rate) {
        return rate == null
                ? BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)
                : rate.multiply(BigDecimal.valueOf(100)).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal safeDivide(BigDecimal dividend, BigDecimal divisor) {
        if (dividend == null || divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return dividend.divide(divisor, MC);
    }
}
