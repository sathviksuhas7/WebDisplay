
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.dto.BeneficiaryDto;
import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.exception.AccountException;
import com.epdcl.webdisplay.service.impl.BeneficiaryService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(
            BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    private User getCustomerUser(HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            throw new AccountException("Please login first");
        }

        if (user.getRole() != Role.CUSTOMER) {
            throw new AccountException(
                    "Only customers can manage beneficiaries");
        }

        return user;
    }

    @GetMapping
    public ResponseEntity<List<BeneficiaryDto>> getBeneficiaries(
            HttpSession session) {

        User user = getCustomerUser(session);

        return ResponseEntity.ok(
                beneficiaryService.getMyBeneficiaries(user.getId()));
    }

    @PostMapping
    public ResponseEntity<BeneficiaryDto> addBeneficiary(
            @RequestBody Map<String, String> request,
            HttpSession session) {

        User user = getCustomerUser(session);

        BeneficiaryDto beneficiary =
                beneficiaryService.addBeneficiary(
                        user.getId(),
                        request.get("accountNumber"),
                        request.get("nickname")
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(beneficiary);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBeneficiary(
            @PathVariable Long id,
            HttpSession session) {

        User user = getCustomerUser(session);

        beneficiaryService.deleteBeneficiary(user.getId(), id);

        return ResponseEntity.noContent().build();
    }
}
