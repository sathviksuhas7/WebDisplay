package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.dto.AccountDto;
import com.epdcl.webdisplay.service.impl.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.Map;

@RestController
public class AccountPage {
    @Autowired
    private AccountService accountService;
    @GetMapping("/account122")
    public Map<String, Serializable> getAccount(){

       // Double amount= request.get("amount");
        AccountDto accountDto = accountService.deposit(Long.valueOf("4"), 10000);
       System.out.println("Balance: " + accountDto.getBalance());
       System.out.println("Name"+accountDto.getAccountHolderName());

        return Map.of(
                "name", accountDto.getAccountHolderName(),
                "balance", accountDto.getBalance()
        );


    }
    @GetMapping("/account")
    public Map<String, Object> getAccount(@RequestParam Long id) {

        AccountDto accountDto = accountService.getAccountById(id);

        return Map.of(
                "name", accountDto.getAccountHolderName(),
                "balance", accountDto.getBalance()
        );
    }

}
