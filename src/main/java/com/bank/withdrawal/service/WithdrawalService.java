package com.bank.withdrawal.service;

import com.bank.withdrawal.exception.AccountNotFoundException;
import com.bank.withdrawal.exception.InsufficientFundsException;
import com.bank.withdrawal.messaging.SnsEventPublisher;
import com.bank.withdrawal.model.WithdrawalEvent;
import com.bank.withdrawal.model.WithdrawalResponse;
import com.bank.withdrawal.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawalService {

    private final AccountRepository accountRepository;
    private final SnsEventPublisher eventPublisher;

    @Transactional
    public WithdrawalResponse withdraw(Long accountId, BigDecimal amount) {

        validate(accountId, amount);

        boolean success = accountRepository.withdraw(accountId, amount);

        if (!success) {

            if (!accountRepository.exists(accountId)) {
                throw new AccountNotFoundException(accountId);
            }

            throw new InsufficientFundsException(accountId);
        }

        BigDecimal updatedBalance = accountRepository.getBalance(accountId);

        WithdrawalEvent event = new WithdrawalEvent(
                accountId,
                amount,
                "SUCCESS"
        );

        // publish AFTER successful withdrawal
        eventPublisher.publish(event);

        log.info(
                "WITHDRAWAL_SUCCESS accountId={} amount={} balance={}",
                accountId, amount, updatedBalance
        );

        return new WithdrawalResponse(
                accountId,
                amount,
                updatedBalance,
                "SUCCESS"
        );
    }

    private void validate(Long accountId, BigDecimal amount) {

        if (accountId == null) {
            throw new IllegalArgumentException("accountId is required");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
    }
}