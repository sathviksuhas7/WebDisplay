
package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.CustomerIdDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CustomerIdDocumentRepository
        extends JpaRepository<CustomerIdDocument, Long>,
        JpaSpecificationExecutor<CustomerIdDocument> {

    List<CustomerIdDocument> findByVerificationStatusOrderByUploadedAtAsc(
            String verificationStatus);

    Optional<CustomerIdDocument> findByIdAndVerificationStatus(
            Long id, String verificationStatus);

    List<CustomerIdDocument>
    findByUploadedByUserIdAndVerificationStatusOrderByReviewedAtDesc(
            Long uploadedByUserId, String verificationStatus);

    // All ID document applications created by an employee
    List<CustomerIdDocument>
    findByUploadedByUserIdOrderByUploadedAtDesc(Long uploadedByUserId);


}
