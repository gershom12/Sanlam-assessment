package com.bank.withdrawal.controller;

import com.bank.withdrawal.model.WithdrawalRequest;
import com.bank.withdrawal.model.WithdrawalResponse;
import com.bank.withdrawal.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bank")
@RequiredArgsConstructor
public class BankAccountController {

    private final WithdrawalService withdrawalService;

    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawalResponse> withdraw(@RequestBody WithdrawalRequest request) {

        return ResponseEntity.ok(
                withdrawalService.withdraw(request.accountId(), request.amount())
        );
    }
}