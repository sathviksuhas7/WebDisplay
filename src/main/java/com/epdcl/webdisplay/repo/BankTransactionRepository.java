
package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankTransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    Optional<BankTransaction> findByReferenceNumber(String referenceNumber);
}
