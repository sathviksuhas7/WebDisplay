
package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.dto.BeneficiaryDto;
import com.epdcl.webdisplay.entity.Account;
import com.epdcl.webdisplay.entity.Beneficiary;
import com.epdcl.webdisplay.entity.Customer;
import com.epdcl.webdisplay.exception.AccountException;
import com.epdcl.webdisplay.repo.AccountRepository;
import com.epdcl.webdisplay.repo.BeneficiaryRepository;
import com.epdcl.webdisplay.repo.CustomerRepository;
import com.epdcl.webdisplay.audit.annotation.Audited;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public BeneficiaryServiceImpl(
            BeneficiaryRepository beneficiaryRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository) {

        this.beneficiaryRepository = beneficiaryRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    private Customer getCustomer(Long userId) {
        return customerRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new AccountException("Customer profile not found"));
    }

    private BeneficiaryDto mapToDto(Beneficiary beneficiary) {
        Account account = beneficiary.getAccount();

        return new BeneficiaryDto(
                beneficiary.getId(),
                beneficiary.getNickname(),
                account.getAccountNumber(),
                account.getAccountHolderName()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryDto> getMyBeneficiaries(Long userId) {

        Customer customer = getCustomer(userId);

        return beneficiaryRepository
                .findByCustomerCustomerIdOrderByCreatedAtDesc(
                        customer.getCustomerId())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Audited(
            action = "BENEFICIARY_ADDED",
            details = "Customer added a beneficiary"
    )
    @Override
    @Transactional
    public BeneficiaryDto addBeneficiary(
            Long userId,
            String accountNumber,
            String nickname) {

        Customer customer = getCustomer(userId);

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new AccountException(
                    "Recipient account number is required");
        }

        if (nickname == null || nickname.isBlank()
                || nickname.trim().length() > 60) {
            throw new AccountException(
                    "Enter a nickname between 1 and 60 characters");
        }

        Account account = accountRepository
                .findByAccountNumber(accountNumber.trim())
                .orElseThrow(() ->
                        new AccountException("Recipient account not found"));

        if (!account.isActive()) {
            throw new AccountException(
                    "This recipient account is inactive");
        }

        if (account.getCustomer() == null) {
            throw new AccountException(
                    "This account cannot be added as a beneficiary");
        }

        if (account.getCustomer().getCustomerId()
                .equals(customer.getCustomerId())) {
            throw new AccountException(
                    "You cannot add your own account as a beneficiary");
        }

        if (beneficiaryRepository
                .existsByCustomerCustomerIdAndAccountId(
                        customer.getCustomerId(), account.getId())) {
            throw new AccountException(
                    "This beneficiary has already been saved");
        }

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setCustomer(customer);
        beneficiary.setAccount(account);
        beneficiary.setNickname(nickname.trim());

        return mapToDto(beneficiaryRepository.save(beneficiary));
    }

    @Audited(
            action = "BENEFICIARY_DELETED",
            details = "Customer deleted a beneficiary"
    )
    @Override
    @Transactional
    public void deleteBeneficiary(Long userId, Long beneficiaryId) {

        Customer customer = getCustomer(userId);

        Beneficiary beneficiary = beneficiaryRepository
                .findByIdAndCustomerCustomerId(
                        beneficiaryId, customer.getCustomerId())
                .orElseThrow(() ->
                        new AccountException("Beneficiary not found"));

        beneficiaryRepository.delete(beneficiary);
    }
}
