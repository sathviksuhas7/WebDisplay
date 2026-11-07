package com.epdcl.webdisplay.dto;

import java.time.LocalDate;

public class CustomerProfileDto {

    private Long customerId;
    private String customerName;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;

    public CustomerProfileDto(Long customerId, String customerName,
                              String phone, String address,
                              LocalDate dateOfBirth) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
}