
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.dto.Base64ValidationRequest;
import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.service.security.DocumentSecurityService;
import com.epdcl.webdisplay.service.security.DocumentSecurityService.ValidatedDocument;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/id-documents/test")
public class DocumentBase64TestController {

    private final DocumentSecurityService documentSecurityService;

    public DocumentBase64TestController(
            DocumentSecurityService documentSecurityService) {

        this.documentSecurityService = documentSecurityService;
    }

    /**
     * TEMPORARY TEST ENDPOINT
     *
     * Receives Base64 document data through JSON.
     * Decodes it and calls the existing DocumentSecurityService.
     *
     * Does NOT create accounts or store documents.
     */
    @PostMapping("/base64-validation")
    public ResponseEntity<Map<String, Object>> validateBase64(
            @RequestBody Base64ValidationRequest request,
            HttpSession session) {

        // Verify logged-in employee.
        Object sessionUser = session.getAttribute("loggedInUser");

        if (!(sessionUser instanceof User user)
                || user.getRole() != Role.EMPLOYEE) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "status", "ACCESS_DENIED",
                            "message", "Only employees can use this test endpoint."
                    ));
        }

        try {

            // Decode Base64 and perform all existing security checks.
            ValidatedDocument validated =
                    documentSecurityService.validateBase64AndScan(
                            request.image()
                    );

            // Return only validation information.
            // Never return the actual document bytes.
            return ResponseEntity.ok(Map.of(
                    "status", "VALID",
                    "message", "Document passed security validation.",
                    "contentType", validated.contentType(),
                    "processedSizeBytes", validated.bytes().length
            ));

        } catch (IllegalArgumentException ex) {

            // Invalid format, corrupted document or malware detected.
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", "REJECTED",
                            "message", ex.getMessage()
                    ));

        } catch (IllegalStateException ex) {

            // ClamAV unavailable or scanner failure.
            // Fail closed: never report the file as valid.
            return ResponseEntity.status(
                    HttpStatus.SERVICE_UNAVAILABLE
            ).body(Map.of(
                    "status", "SCAN_UNAVAILABLE",
                    "message", ex.getMessage()
            ));
        }
    }
}
