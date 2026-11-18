
package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.entity.Customer;
import com.epdcl.webdisplay.dto.HelpdeskCustomerDto;

import java.time.LocalDate;
import java.util.List;

public interface CustomerService {

    Customer createCustomer(
            String username,
            String email,
            String password,
            String customerName,
            String phone,
            String address,
            LocalDate dateOfBirth
    );

    Customer getCustomerByUserId(Long userId);

    List<HelpdeskCustomerDto> searchCustomers(
            String name,
            String phone
    );

    HelpdeskCustomerDto getHelpdeskCustomerById(Long customerId);
}
