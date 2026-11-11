package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.dto.AccountDto;
import com.epdcl.webdisplay.dto.TransactionDto;
import com.epdcl.webdisplay.entity.Employee;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.repo.CustomerRepository;
import com.epdcl.webdisplay.exception.AccountException;
import com.epdcl.webdisplay.repo.EmployeeRepository;
import com.epdcl.webdisplay.service.impl.AccountService;

import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final Logger logger =
            LogManager.getLogger(AccountController.class);

    private final AccountService accountService;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;

    public AccountController(
            AccountService accountService,
            EmployeeRepository employeeRepository,
            CustomerRepository customerRepository) {

        this.accountService = accountService;
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
    }

    // Add Account
    @PostMapping
    public ResponseEntity<AccountDto> addAccount(
            @RequestBody AccountDto accountDto) {

        AccountDto savedAccount =
                accountService.createAccount(accountDto);

        return new ResponseEntity<>(savedAccount, HttpStatus.CREATED);
    }

    // Get Account By ID
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(
            @PathVariable Long id) {

        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    // Get Account By Account Number
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountDto> getAccountByAccountNumber(
            @PathVariable String accountNumber) {

        return ResponseEntity.ok(
                accountService.getAccountByAccountNumber(accountNumber));
    }

    // Deposit
    @PutMapping("/{id}/deposit")
    public ResponseEntity<AccountDto> deposit(
            @PathVariable Long id,
            @RequestBody Map<String, Double> request) {

        Double amount = request.get("amount");

        AccountDto accountDto = accountService.deposit(id, amount);

        return ResponseEntity.ok(accountDto);
    }

    // Withdraw
    @PutMapping("/{id}/withdraw")
    public ResponseEntity<AccountDto> withdraw(
            @PathVariable Long id,
            @RequestBody Map<String, Double> request) {

        Double amount = request.get("amount");

        AccountDto accountDto = accountService.withdraw(id, amount);

        return ResponseEntity.ok(accountDto);
    }

    // Get All Accounts
    @GetMapping
    public ResponseEntity<List<AccountDto>> getAllAccounts() {

        List<AccountDto> accounts = accountService.getAllAccounts();

        logger.info("Fetching all accounts");

        return ResponseEntity.ok(accounts);
    }

    // Deactivate Account
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateAccount(
            @PathVariable Long id) {

        accountService.deactivateAccount(id);

        return ResponseEntity.ok("Account deactivated successfully");
    }

    // Activate Account
    @PutMapping("/{id}/activate")
    public ResponseEntity<String> activateAccount(
            @PathVariable Long id) {

        accountService.activateAccount(id);

        return ResponseEntity.ok("Account activated successfully");
    }

    // Get Transaction History
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionDto>> getTransactionHistory(
            @PathVariable Long id) {

        List<TransactionDto> transactions =
                accountService.getTransactionHistory(id);

        return ResponseEntity.ok(transactions);
    }

    // Edit Account Details
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

    // Get Logged-In Employee
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getLoggedInEmployee(
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Employee employee = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Employee profile not found"));

        return ResponseEntity.ok(
                Map.of("employeeName", employee.getEmployeeName())
        );
    }
    // Transfer Money
    @PutMapping("/{id}/transfer")
    public ResponseEntity<AccountDto> transfer(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (request == null
                || request.get("recipientAccountNumber") == null
                || request.get("amount") == null) {
            throw new AccountException("Recipient account number and amount are required");
        }

        Long customerId = customerRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new AccountException("Customer profile not found"))
                .getCustomerId();

        String recipientAccountNumber =
                String.valueOf(request.get("recipientAccountNumber")).trim();

        if (recipientAccountNumber.isBlank()) {
            throw new AccountException("Recipient account number is required");
        }

        double amount;

        try {
            amount = Double.parseDouble(String.valueOf(request.get("amount")));
        } catch (NumberFormatException e) {
            throw new AccountException("Invalid transfer amount");
        }

        AccountDto result = accountService.transfer(
                id,
                customerId,
                recipientAccountNumber,
                amount
        );

        return ResponseEntity.ok(result);
    }
    // Verify Recipient Account
    @GetMapping("/recipient/{accountNumber}")
    public ResponseEntity<Map<String, String>> verifyRecipient(
            @PathVariable String accountNumber,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String recipientName =
                accountService.verifyRecipientAccount(accountNumber);

        return ResponseEntity.ok(
                Map.of("recipientName", recipientName)
        );
    }



}