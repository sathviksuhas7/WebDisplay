package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.dto.AccountDto;

import com.epdcl.webdisplay.dto.TransactionDto;

import com.epdcl.webdisplay.dto.CustomerAccountStatementDto;

import com.epdcl.webdisplay.dto.HelpdeskAccountDto;

import com.epdcl.webdisplay.dto.HelpdeskTransactionDto;

import java.util.List;

import com.epdcl.webdisplay.dto.AccountDto;

public interface AccountService {

    AccountDto createAccount(AccountDto accountDto);

    AccountDto getAccountById(Long id);

    AccountDto deposit(Long id,double amount);

    AccountDto withdraw(Long id,double amount);

    List<AccountDto> getAllAccounts();

    //transaction
    List<TransactionDto> getTransactionHistory(Long accountId);

//    void deleteAccount(Long id);

    AccountDto updateBalance(Long id, double newBalance);

    AccountDto getAccountByAccountNumber(String accountNumber);

    void deactivateAccount(Long id);

    void activateAccount(Long id);

//    AccountDto updateAccount(Long id, String accountHolderName);

    AccountDto updateAccount(
            Long id,
            String accountHolderName,
            String branchName,
            String district
    );

    CustomerAccountStatementDto getCustomerAccountStatement(
            Long accountId, Long customerId);

    List<AccountDto> getAccountsByCustomerId(Long customerId);

    List<TransactionDto> getCustomerTransactionHistory(Long accountId, Long customerId);

    List<HelpdeskAccountDto> getHelpdeskCustomerAccounts(Long customerId);

    List<HelpdeskTransactionDto> getHelpdeskCustomerTransactions(Long customerId);


}

