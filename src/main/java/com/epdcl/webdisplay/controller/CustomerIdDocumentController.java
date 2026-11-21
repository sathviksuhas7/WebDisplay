
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.entity.CustomerIdDocument;
import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.service.impl.CustomerIdDocumentService;

import jakarta.servlet.http.HttpSession;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/id-documents")
public class CustomerIdDocumentController {

    private final CustomerIdDocumentService documentService;

    public CustomerIdDocumentController(
            CustomerIdDocumentService documentService) {
        this.documentService = documentService;
    }

    // EMPLOYEE: CREATE ACCOUNT AND UPLOAD ID DOCUMENT
    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(
            @RequestParam Long customerId,
            @RequestParam String branchName,
            @RequestParam String district,
            @RequestParam(defaultValue = "SAVINGS") String accountType,
            @RequestParam("document") MultipartFile document,
            HttpSession session) throws Exception {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in again."));
        }

        if (loggedInUser.getRole() != Role.EMPLOYEE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Only employees can create account applications."
                    ));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                documentService.createAccountWithDocument(
                        customerId,
                        branchName,
                        district,
                        accountType,
                        document,
                        loggedInUser.getId()
                )
        );
    }

    // EMPLOYEE: VIEW ALL ID DOCUMENT APPLICATIONS
    @GetMapping("/employee/applications")
    public ResponseEntity<?> getEmployeeApplications(
            HttpSession session) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in again."));
        }

        if (loggedInUser.getRole() != Role.EMPLOYEE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Only employees can view their applications."
                    ));
        }

        List<Map<String, Object>> result = new ArrayList<>();

        documentService
                .getAllApplicationsForEmployee(loggedInUser.getId())
                .forEach(doc -> {

                    Map<String, Object> item = new LinkedHashMap<>();

                    item.put("id", doc.getId());
                    item.put("customerId", doc.getCustomer().getCustomerId());
                    item.put("customerName", doc.getCustomer().getCustomerName());
                    item.put("accountNumber", doc.getAccount().getAccountNumber());
                    item.put("fileName", doc.getOriginalFileName());
                    item.put("status", doc.getVerificationStatus());
                    item.put("rejectionReason", doc.getRejectionReason());
                    item.put("uploadedAt", doc.getUploadedAt());
                    item.put("reviewedAt", doc.getReviewedAt());

                    result.add(item);
                });

        return ResponseEntity.ok(result);
    }

    // EMPLOYEE: VIEW REJECTED DOCUMENTS UPLOADED BY LOGGED-IN EMPLOYEE
    @GetMapping("/employee/rejected")
    public ResponseEntity<?> getRejectedDocuments(
            HttpSession session) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in again."));
        }

        if (loggedInUser.getRole() != Role.EMPLOYEE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Only employees can access rejected documents."
                    ));
        }

        List<Map<String, Object>> result = new ArrayList<>();

        documentService
                .getRejectedDocumentsForEmployee(loggedInUser.getId())
                .forEach(doc -> {

                    Map<String, Object> item =
                            new LinkedHashMap<>();

                    item.put("id", doc.getId());
                    item.put(
                            "customerId",
                            doc.getCustomer().getCustomerId()
                    );
                    item.put(
                            "customerName",
                            doc.getCustomer().getCustomerName()
                    );
                    item.put(
                            "accountNumber",
                            doc.getAccount().getAccountNumber()
                    );
                    item.put(
                            "fileName",
                            doc.getOriginalFileName()
                    );
                    item.put(
                            "status",
                            doc.getVerificationStatus()
                    );
                    item.put(
                            "rejectionReason",
                            doc.getRejectionReason()
                    );
                    item.put(
                            "uploadedAt",
                            doc.getUploadedAt()
                    );
                    item.put(
                            "reviewedAt",
                            doc.getReviewedAt()
                    );

                    result.add(item);
                });

        return ResponseEntity.ok(result);
    }

    // EMPLOYEE: RE-UPLOAD A REJECTED DOCUMENT
    @PostMapping("/{id}/reupload")
    public ResponseEntity<?> reuploadDocument(
            @PathVariable Long id,
            @RequestParam("document") MultipartFile file,
            HttpSession session) throws Exception {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in again."));
        }

        if (loggedInUser.getRole() != Role.EMPLOYEE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Only employees can re-upload documents."
                    ));
        }

        documentService.reuploadRejectedDocument(
                id,
                file,
                loggedInUser.getId()
        );

        return ResponseEntity.ok(Map.of(
                "message",
                "Document re-uploaded successfully. Waiting for Bank Manager verification."
        ));
    }

    // BANK MANAGER: VIEW ALL PENDING DOCUMENTS
    @GetMapping("/manager/pending")
    public ResponseEntity<?> getPendingDocuments(
            HttpSession session) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in again."));
        }

        if (loggedInUser.getRole() != Role.BANK_MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Only Bank Managers can verify documents."
                    ));
        }

        List<Map<String, Object>> result =
                documentService.getPendingDocuments()
                        .stream()
                        .map(doc -> {

                            Map<String, Object> item =
                                    new LinkedHashMap<>();

                            item.put("id", doc.getId());
                            item.put(
                                    "customerId",
                                    doc.getCustomer().getCustomerId()
                            );
                            item.put(
                                    "customerName",
                                    doc.getCustomer().getCustomerName()
                            );
                            item.put(
                                    "accountId",
                                    doc.getAccount().getId()
                            );
                            item.put(
                                    "accountNumber",
                                    doc.getAccount().getAccountNumber()
                            );
                            item.put(
                                    "fileName",
                                    doc.getOriginalFileName()
                            );
                            item.put(
                                    "status",
                                    doc.getVerificationStatus()
                            );
                            item.put(
                                    "uploadedAt",
                                    doc.getUploadedAt()
                            );

                            return item;
                        })
                        .toList();

        return ResponseEntity.ok(result);
    }

    // BANK MANAGER: VIEW UPLOADED ID DOCUMENT
    @GetMapping("/manager/{id}/file")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable Long id,
            HttpSession session) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        if (loggedInUser.getRole() != Role.BANK_MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        }

        Resource resource =
                documentService.getDocumentFile(id);

        String filename = resource.getFilename();

        String contentType = "application/octet-stream";

        if (filename != null) {

            String lowerFilename = filename.toLowerCase();

            if (lowerFilename.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (lowerFilename.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerFilename.endsWith(".jpg")
                    || lowerFilename.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            }
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline"
                )
                .contentType(
                        MediaType.parseMediaType(contentType)
                )
                .body(resource);
    }

    // BANK MANAGER: APPROVE DOCUMENT AND ACTIVATE ACCOUNT
    @PostMapping("/manager/{id}/approve")
    public ResponseEntity<?> approve(
            @PathVariable Long id,
            HttpSession session) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in again."));
        }

        if (loggedInUser.getRole() != Role.BANK_MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Only Bank Managers can approve documents."
                    ));
        }

        documentService.approveDocument(id);

        return ResponseEntity.ok(Map.of(
                "message",
                "Document approved. Account activated."
        ));
    }

    // BANK MANAGER: REJECT DOCUMENT
    @PostMapping("/manager/{id}/reject")
    public ResponseEntity<?> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in again."));
        }

        if (loggedInUser.getRole() != Role.BANK_MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Only Bank Managers can reject documents."
                    ));
        }

        String reason = request.get("reason");

        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Rejection reason is required."
                    ));
        }

        documentService.rejectDocument(id, reason);

        return ResponseEntity.ok(Map.of(
                "message",
                "Document rejected. Account remains inactive."
        ));
    }

    // EMPLOYEE: SEARCH ID DOCUMENT APPLICATIONS WITH PAGINATION
    @GetMapping("/employee/applications/search")
    public ResponseEntity<?> searchEmployeeApplications(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in again."));
        }

        if (loggedInUser.getRole() != Role.EMPLOYEE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message",
                            "Only employees can access applications."));
        }

        if (page < 0 || size < 1 || size > 50) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid pagination values."));
        }

        if (!List.of("ALL", "PENDING", "APPROVED", "REJECTED")
                .contains(status.toUpperCase())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid verification status."));
        }

        Page<CustomerIdDocument> applications =
                documentService.searchEmployeeApplications(
                        loggedInUser.getId(),
                        search,
                        status,
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, "uploadedAt")
                        )
                );

        List<Map<String, Object>> records =
                applications.getContent().stream().map(doc -> {

                    Map<String, Object> item = new LinkedHashMap<>();

                    item.put("id", doc.getId());
                    item.put("customerId",
                            doc.getCustomer().getCustomerId());
                    item.put("customerName",
                            doc.getCustomer().getCustomerName());
                    item.put("accountNumber",
                            doc.getAccount().getAccountNumber());
                    item.put("fileName",
                            doc.getOriginalFileName());
                    item.put("status",
                            doc.getVerificationStatus());
                    item.put("rejectionReason",
                            doc.getRejectionReason());
                    item.put("uploadedAt",
                            doc.getUploadedAt());
                    item.put("reviewedAt",
                            doc.getReviewedAt());

                    return item;

                }).toList();

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("content", records);
        response.put("currentPage", applications.getNumber());
        response.put("totalPages", applications.getTotalPages());
        response.put("totalElements", applications.getTotalElements());
        response.put("pageSize", applications.getSize());

        return ResponseEntity.ok(response);
    }


}
