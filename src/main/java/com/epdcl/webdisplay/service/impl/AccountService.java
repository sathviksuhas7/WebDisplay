package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.dto.AccountDto;
import com.epdcl.webdisplay.dto.TransactionDto;
import com.epdcl.webdisplay.dto.CustomerAccountStatementDto;
import com.epdcl.webdisplay.dto.HelpdeskAccountDto;
import com.epdcl.webdisplay.dto.HelpdeskTransactionDto;

import java.util.List;

public interface AccountService {

    AccountDto createAccount(AccountDto accountDto);

    AccountDto getAccountById(Long id);

    AccountDto getAccountByAccountNumber(String accountNumber);

    AccountDto deposit(Long id, double amount);

    AccountDto withdraw(Long id, double amount);

    List<AccountDto> getAllAccounts();

    void deactivateAccount(Long id);

    void activateAccount(Long id);

    AccountDto updateAccount(
            Long id,
            String accountHolderName,
            String branchName,
            String district
    );

    AccountDto transfer(
            Long senderAccountId,
            Long customerId,
            String recipientAccountNumber,
            double amount
    );

    List<TransactionDto> getTransactionHistory(Long accountId);

    CustomerAccountStatementDto getCustomerAccountStatement(
            Long accountId,
            Long customerId
    );

    List<AccountDto> getAccountsByCustomerId(Long customerId);

    List<TransactionDto> getCustomerTransactionHistory(
            Long accountId,
            Long customerId
    );

    List<HelpdeskAccountDto> getHelpdeskCustomerAccounts(Long customerId);

    List<HelpdeskTransactionDto> getHelpdeskCustomerTransactions(
            Long customerId
    );

    String verifyRecipientAccount(String accountNumber);
}