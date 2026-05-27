package com.bluemoon.services;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentServiceTest {

    private final PaymentService paymentService = new PaymentService();

    @Test
    public void validateTienNop_validAmount_returnsBigDecimal() {
        BigDecimal debt = new BigDecimal("1000");
        BigDecimal result = paymentService.validateTienNop("500", debt);
        assertEquals(new BigDecimal("500"), result);
    }

    @Test
    public void validateTienNop_zeroOrNegative_throws() {
        BigDecimal debt = new BigDecimal("1000");
        assertThrows(IllegalArgumentException.class, () -> paymentService.validateTienNop("0", debt));
        assertThrows(IllegalArgumentException.class, () -> paymentService.validateTienNop("-10", debt));
    }

    @Test
    public void validateTienNop_exceedsDebt_throws() {
        BigDecimal debt = new BigDecimal("1000");
        assertThrows(IllegalArgumentException.class, () -> paymentService.validateTienNop("1500", debt));
    }

    @Test
    public void validateTienNop_invalidNumber_throws() {
        BigDecimal debt = new BigDecimal("1000");
        assertThrows(IllegalArgumentException.class, () -> paymentService.validateTienNop("abc", debt));
    }
}
