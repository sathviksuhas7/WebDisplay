
package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.entity.Customer;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.dto.HelpdeskCustomerDto;
import com.epdcl.webdisplay.repo.CustomerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserService userService;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            UserService userService) {

        this.customerRepository = customerRepository;
        this.userService = userService;
    }

    // Create Customer
    @Override
    @Transactional
    public Customer createCustomer(
            String username,
            String email,
            String password,
            String customerName,
            String phone,
            String address,
            LocalDate dateOfBirth) {

        User user = userService.createCustomerLogin(
                username, email, password);

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setCustomerName(customerName);
        customer.setPhone(phone);
        customer.setAddress(address);
        customer.setDateOfBirth(dateOfBirth);

        return customerRepository.save(customer);
    }

    // Get Customer By User ID
    @Override
    public Customer getCustomerByUserId(Long userId) {

        return customerRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Customer profile not found"));
    }

    // Helpdesk Customer Search
    @Override
    @Transactional(readOnly = true)
    public List<HelpdeskCustomerDto> searchCustomers(
            String name, String phone) {

        List<Customer> customers;

        if (name != null && !name.isBlank()
                && phone != null && !phone.isBlank()) {

            customers = Stream.concat(
                    customerRepository
                            .findByCustomerNameContainingIgnoreCase(name).stream(),
                    customerRepository
                            .findByPhoneContaining(phone).stream()
            ).distinct().collect(Collectors.toList());

        } else if (name != null && !name.isBlank()) {

            customers = customerRepository
                    .findByCustomerNameContainingIgnoreCase(name);

        } else if (phone != null && !phone.isBlank()) {

            customers = customerRepository.findByPhoneContaining(phone);

        } else {
            throw new RuntimeException("Enter customer name or phone number");
        }

        return customers.stream()
                .map(customer -> new HelpdeskCustomerDto(
                        customer.getCustomerId(),
                        customer.getCustomerName(),
                        customer.getPhone(),
                        customer.getAddress()
                ))
                .collect(Collectors.toList());
    }

    // Helpdesk Get Customer By ID
    @Override
    @Transactional(readOnly = true)
    public HelpdeskCustomerDto getHelpdeskCustomerById(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        return new HelpdeskCustomerDto(
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getPhone(),
                customer.getAddress()
        );
    }
}
