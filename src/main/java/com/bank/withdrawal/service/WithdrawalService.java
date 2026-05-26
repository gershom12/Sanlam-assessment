package com.bank.withdrawal.service;

import com.bank.withdrawal.exception.AccountNotFoundException;
import com.bank.withdrawal.exception.InsufficientFundsException;
import com.bank.withdrawal.messaging.EventPublisher;
import com.bank.withdrawal.model.WithdrawalEvent;
import com.bank.withdrawal.model.WithdrawalResponse;
import com.bank.withdrawal.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawalService {

    private final AccountRepository repository;
    private final EventPublisher eventPublisher;

    @Transactional
    public WithdrawalResponse withdraw(Long accountId, BigDecimal amount) {

        String correlationId = UUID.randomUUID().toString();

        log.info("WITHDRAWAL_START correlationId={} accountId={} amount={}",
                correlationId, accountId, amount);

        boolean updated = repository.withdraw(accountId, amount);

        if (!updated) {

            if (!repository.exists(accountId)) {
                log.warn("ACCOUNT_NOT_FOUND correlationId={} accountId={}",
                        correlationId, accountId);
                throw new AccountNotFoundException(accountId);
            }

            log.warn("INSUFFICIENT_FUNDS correlationId={} accountId={}",
                    correlationId, accountId);

            throw new InsufficientFundsException(accountId);
        }

        BigDecimal balance = repository.getBalance(accountId);

        WithdrawalEvent event = new WithdrawalEvent(
                accountId,
                amount,
                "SUCCESS"
        );

        try {
            eventPublisher.publishWithRetry(event, correlationId);
        } catch (Exception ex) {
            log.error("EVENT_PUBLISH_FAILED correlationId={} accountId={}",
                    correlationId, accountId, ex);
        }

        log.info("WITHDRAWAL_SUCCESS correlationId={} accountId={} balance={}",
                correlationId, accountId, balance);

        return new WithdrawalResponse(accountId, amount, balance, "SUCCESS");
    }
}