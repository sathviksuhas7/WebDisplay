package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.CustomerAddressChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerAddressChangeRequestRepository
        extends JpaRepository<CustomerAddressChangeRequest, Long> {

    List<CustomerAddressChangeRequest> findByCustomerIdOrderByCreatedAtDesc(
            Long customerId);

    Optional<CustomerAddressChangeRequest> findByIdAndCustomerId(
            Long id, Long customerId);

    List<CustomerAddressChangeRequest> findByStatusOrderByCreatedAtAsc(
            String status);
}