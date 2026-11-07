package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.dto.CustomerDto;
import com.epdcl.webdisplay.entity.Customer;
import com.epdcl.webdisplay.service.impl.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<String> createCustomer(
            @RequestBody CustomerDto customerDto) {

        Customer customer = customerService.createCustomer(
                customerDto.getUsername(),
                customerDto.getEmail(),
                customerDto.getPassword(),
                customerDto.getCustomerName(),
                customerDto.getPhone(),
                customerDto.getAddress(),
                customerDto.getDateOfBirth()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Customer created successfully. Customer ID: "
                        + customer.getCustomerId());
    }
}