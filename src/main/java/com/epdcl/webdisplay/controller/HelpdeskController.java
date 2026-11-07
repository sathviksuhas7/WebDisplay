
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.dto.HelpdeskCustomerDto;
import com.epdcl.webdisplay.dto.HelpdeskAccountDto;
import com.epdcl.webdisplay.service.impl.CustomerService;
import com.epdcl.webdisplay.service.impl.AccountService;
import com.epdcl.webdisplay.dto.HelpdeskTransactionDto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/helpdesk")
public class HelpdeskController {

    private final CustomerService customerService;
    private final AccountService accountService;

    public HelpdeskController(
            CustomerService customerService,
            AccountService accountService) {

        this.customerService = customerService;
        this.accountService = accountService;
    }

    // Search customers by name or phone
    @GetMapping("/customers/search")
    public ResponseEntity<List<HelpdeskCustomerDto>> searchCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone) {

        return ResponseEntity.ok(
                customerService.searchCustomers(name, phone));
    }

    // Get customer details by ID
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<HelpdeskCustomerDto> getCustomer(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                customerService.getHelpdeskCustomerById(customerId));
    }

    // Helpdesk views customer's masked accounts
    @GetMapping("/customers/{customerId}/accounts")
    public ResponseEntity<List<HelpdeskAccountDto>> getCustomerAccounts(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                accountService.getHelpdeskCustomerAccounts(customerId));
    }

    // Helpdesk views customer's transaction history
    @GetMapping("/customers/{customerId}/transactions")
    public ResponseEntity<List<HelpdeskTransactionDto>> getCustomerTransactions(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                accountService.getHelpdeskCustomerTransactions(customerId));
    }
}
