package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.dto.AccountDto;
import com.epdcl.webdisplay.dto.CustomerProfileDto;
import com.epdcl.webdisplay.entity.Customer;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.service.impl.AccountService;
import com.epdcl.webdisplay.service.impl.CustomerService;
import com.epdcl.webdisplay.dto.TransactionDto;
import com.epdcl.webdisplay.dto.CustomerAccountStatementDto;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerProfileController {

    private final CustomerService customerService;
    private final AccountService accountService;

    public CustomerProfileController(CustomerService customerService,
                                     AccountService accountService) {
        this.customerService = customerService;
        this.accountService = accountService;
    }

    @GetMapping("/profile")
    public ResponseEntity<CustomerProfileDto> getMyProfile(HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        Customer customer = customerService.getCustomerByUserId(user.getId());

        CustomerProfileDto profile = new CustomerProfileDto(
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getDateOfBirth()
        );

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> getMyAccounts(HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        Customer customer = customerService.getCustomerByUserId(user.getId());

        List<AccountDto> accounts =
                accountService.getAccountsByCustomerId(customer.getCustomerId());

        return ResponseEntity.ok(accounts);
    }
    // Customer Transaction History
    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<TransactionDto>> getMyTransactions(
            @PathVariable Long accountId,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        Customer customer =
                customerService.getCustomerByUserId(user.getId());

        List<TransactionDto> transactions =
                accountService.getCustomerTransactionHistory(
                        accountId, customer.getCustomerId());

        return ResponseEntity.ok(transactions);
    }
    // Customer Account Statement
    @GetMapping("/accounts/{accountId}/statement")
    public ResponseEntity<CustomerAccountStatementDto> getMyAccountStatement(
            @PathVariable Long accountId,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        Customer customer =
                customerService.getCustomerByUserId(user.getId());

        CustomerAccountStatementDto statement =
                accountService.getCustomerAccountStatement(
                        accountId, customer.getCustomerId());

        return ResponseEntity.ok(statement);
    }
}