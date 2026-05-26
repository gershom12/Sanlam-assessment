package com.bank.withdrawal.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Long id) {
        super("Insufficient funds for account: " + id);
    }
}