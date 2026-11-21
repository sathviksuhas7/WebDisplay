
package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount_IdOrderByTransactionDateDesc(
            Long accountId);

    List<Transaction> findByAccount_IdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long accountId, LocalDateTime from, LocalDateTime to);
}
