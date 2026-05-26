package com.bank.withdrawal.model;

import java.math.BigDecimal;

public record WithdrawalResponse(
        Long accountId,
        BigDecimal amount,
        BigDecimal balance,
        String status
) {}