package com.epdcl.webdisplay.mapper;

import com.epdcl.webdisplay.dto.AccountDto;
import com.epdcl.webdisplay.entity.Account;

public class AccountMapper {

    public static Account mapToAccount(AccountDto accountDto) {

        Account account = new Account();

        account.setAccountNumber(accountDto.getAccountNumber());
        account.setAccountHolderName(accountDto.getAccountHolderName());
        account.setBalance(accountDto.getBalance());
        account.setActive(accountDto.isActive());

        // Branch and District
        account.setBranchName(accountDto.getBranchName());
        account.setDistrict(accountDto.getDistrict());

        return account;
    }

    public static AccountDto mapToAccountDto(Account account) {

        AccountDto accountDto = new AccountDto();

        // Convert Long ID to String
        accountDto.setId(String.valueOf(account.getId()));

        accountDto.setAccountNumber(account.getAccountNumber());
        accountDto.setAccountHolderName(account.getAccountHolderName());
        accountDto.setBalance(account.getBalance());
        accountDto.setActive(account.isActive());
        accountDto.setCreatedAt(account.getCreatedAt());

        // Branch and District
        accountDto.setBranchName(account.getBranchName());
        accountDto.setDistrict(account.getDistrict());

        return accountDto;
    }
}