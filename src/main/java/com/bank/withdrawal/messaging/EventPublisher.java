package com.bank.withdrawal.messaging;

import com.bank.withdrawal.model.WithdrawalEvent;

public interface EventPublisher {
    void publishWithRetry(WithdrawalEvent event, String correlationId);
}