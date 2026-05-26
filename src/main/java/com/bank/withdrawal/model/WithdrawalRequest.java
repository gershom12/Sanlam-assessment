package com.bank.withdrawal.model;

import java.math.BigDecimal;

public record WithdrawalRequest(
        Long accountId,
        BigDecimal amount
) {}