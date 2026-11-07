package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository
        extends JpaRepository<Account, Long> {

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumber(String accountNumber);

    // Find all accounts belonging to a particular customer
    List<Account> findByCustomerCustomerId(Long customerId);

    Optional<Account> findByIdAndCustomerCustomerId(Long id, Long customerId);
}