
package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository
        extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByCustomerCustomerIdOrderByCreatedAtDesc(
            Long customerId);

    boolean existsByCustomerCustomerIdAndAccountId(
            Long customerId, Long accountId);

    Optional<Beneficiary> findByIdAndCustomerCustomerId(
            Long id, Long customerId);
}
