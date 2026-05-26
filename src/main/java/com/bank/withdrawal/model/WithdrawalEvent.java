package com.bank.withdrawal.model;

import java.math.BigDecimal;

public record WithdrawalEvent(
        Long accountId,
        BigDecimal amount,
        String status
) {}