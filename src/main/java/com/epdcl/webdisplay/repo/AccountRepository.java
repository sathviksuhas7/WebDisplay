
package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumber(String accountNumber);

    // Find all accounts belonging to a particular customer
    List<Account> findByCustomerCustomerId(Long customerId);

    Optional<Account> findByIdAndCustomerCustomerId(Long id, Long customerId);

    // Lock an account while processing a financial transaction
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :accountId")
    Optional<Account> findByIdForUpdate(@Param("accountId") Long accountId);
}
