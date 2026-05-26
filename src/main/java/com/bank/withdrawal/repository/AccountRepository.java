package com.bank.withdrawal.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
@RequiredArgsConstructor
public class AccountRepository {

    private final JdbcTemplate jdbcTemplate;

    public boolean withdraw(Long accountId, BigDecimal amount) {

        String sql = """
            UPDATE accounts
            SET balance = balance - ?
            WHERE id = ?
            AND balance >= ?
        """;

        return jdbcTemplate.update(sql, amount, accountId, amount) > 0;
    }

    public BigDecimal getBalance(Long accountId) {

        return jdbcTemplate.queryForObject(
                "SELECT balance FROM accounts WHERE id = ?",
                BigDecimal.class,
                accountId
        );
    }

    public boolean exists(Long accountId) {

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM accounts WHERE id = ?",
                Integer.class,
                accountId
        );

        return count != null && count > 0;
    }
}