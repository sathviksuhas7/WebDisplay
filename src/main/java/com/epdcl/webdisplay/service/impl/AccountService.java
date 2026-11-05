package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.dto.AccountDto;

import com.epdcl.webdisplay.dto.TransactionDto;

import java.util.List;

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


}

