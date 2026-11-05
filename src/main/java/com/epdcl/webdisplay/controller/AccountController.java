
package com.epdcl.webdisplay.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.epdcl.webdisplay.service.impl.AccountService;
import com.epdcl.webdisplay.exception.AccountException;

import com.epdcl.webdisplay.dto.AccountDto;
import com.epdcl.webdisplay.dto.TransactionDto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private static final Logger logger =
            LogManager.getLogger(AccountController.class);

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Add Account REST API
    @PostMapping
    public ResponseEntity<AccountDto> addAccount(
            @RequestBody AccountDto accountDto) {

        AccountDto savedAccount = accountService.createAccount(accountDto);

        return new ResponseEntity<>(savedAccount, HttpStatus.CREATED);
    }

    // Get Account REST API
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(
            @PathVariable Long id) {

        AccountDto accountDto = accountService.getAccountById(id);

        return ResponseEntity.ok(accountDto);
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountDto> getAccountByAccountNumber(
            @PathVariable String accountNumber) {

        return ResponseEntity.ok(
                accountService.getAccountByAccountNumber(accountNumber));
    }

    // Deposit REST API
    @PutMapping("/{id}/deposit")
    public ResponseEntity<AccountDto> deposit(
            @PathVariable Long id,
            @RequestBody Map<String, Double> request) {

        Double amount = request.get("amount");

        AccountDto accountDto = accountService.deposit(id, amount);

        return ResponseEntity.ok(accountDto);
    }

    // Withdraw REST API
    @PutMapping("/{id}/withdraw")
    public ResponseEntity<AccountDto> withdraw(
            @PathVariable Long id,
            @RequestBody Map<String, Double> request) {

        Double amount = request.get("amount");

        AccountDto accountDto = accountService.withdraw(id, amount);

        return ResponseEntity.ok(accountDto);
    }

    // Get All Accounts REST API
//    @GetMapping
//    public ResponseEntity<List<AccountDto>> getAllAccounts() {
//
//        List<AccountDto> accounts = accountService.getAllAccounts();
//
//        return ResponseEntity.ok(accounts);
//    }
    @GetMapping
    public ResponseEntity<List<AccountDto>> getAllAccounts() {
        List<AccountDto> accounts = accountService.getAllAccounts();

        logger.info("Fetching all accounts{}"+accounts);

        return ResponseEntity.ok(accounts);
    }

    // Delete Account REST API
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deleteAccount(@PathVariable Long id) {
//
//        accountService.deleteAccount(id);
//
//        return ResponseEntity.ok("Account deleted successfully");
//    }


    // Deactivate Account
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateAccount(@PathVariable Long id) {

        accountService.deactivateAccount(id);

        return ResponseEntity.ok("Account deactivated successfully");
    }

    // Activate Account
    @PutMapping("/{id}/activate")
    public ResponseEntity<String> activateAccount(@PathVariable Long id) {

        accountService.activateAccount(id);

        return ResponseEntity.ok("Account activated successfully");
    }

    // Edit Account Balance REST API
    @PutMapping("/{id}/balance")
    public ResponseEntity<AccountDto> updateBalance(
            @PathVariable Long id,
            @RequestBody Map<String, Double> request) {

        Double newBalance = request.get("balance");

        if (newBalance == null) {
            throw new AccountException("Balance is required");
        }

        AccountDto updatedAccount =
                accountService.updateBalance(id, newBalance);

        return ResponseEntity.ok(updatedAccount);
    }

    // Get Transaction History REST API
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionDto>> getTransactionHistory(
            @PathVariable Long id) {

        List<TransactionDto> transactions =
                accountService.getTransactionHistory(id);

        return ResponseEntity.ok(transactions);
    }

    // EDIT ACCOUNT
    @PutMapping("/{id}/edit")
    public AccountDto updateAccount(
            @PathVariable Long id,
            @RequestBody AccountDto accountDto) {

        return accountService.updateAccount(
                id,
                accountDto.getAccountHolderName(),
                accountDto.getBranchName(),
                accountDto.getDistrict()
        );
    }
}
