
package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.dto.AccountDto;
import com.epdcl.webdisplay.entity.Account;
import com.epdcl.webdisplay.exception.AccountException;
import com.epdcl.webdisplay.mapper.AccountMapper;
import com.epdcl.webdisplay.repo.AccountRepository;

import com.epdcl.webdisplay.dto.TransactionDto;
import com.epdcl.webdisplay.mapper.TransactionMapper;

import com.epdcl.webdisplay.repo.TransactionRepository;
import com.epdcl.webdisplay.entity.Transaction;


import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;


    public AccountServiceImpl(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // Generate Account Number
    private String generateAccountNumber() {

        String prefix = "DC";

        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("ddMMyy"));

        int randomNumber = new Random().nextInt(10000);

        return prefix + date + String.format("%04d", randomNumber);
    }

    // Add Account
    @Override
    public AccountDto createAccount(AccountDto accountDto) {

        String accountNumber;

        do {
            accountNumber = generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        Account account = AccountMapper.mapToAccount(accountDto);

        account.setAccountNumber(accountNumber);

        Account accountSaved = accountRepository.save(account);

        return AccountMapper.mapToAccountDto(accountSaved);
    }

    // Get Account By ID
    @Override
    public AccountDto getAccountById(Long id) {

        Account account = accountRepository
                .findById(id)
                .orElseThrow(() -> new AccountException("Account not found"));

        return AccountMapper.mapToAccountDto(account);
    }


    // Deposit
    @Transactional
    @Override
    public AccountDto deposit(Long id, double amount) {

        if (!Double.isFinite(amount) || amount <= 0) {
            throw new AccountException(
                    "Deposit amount must be greater than zero");
        }

        Account account = accountRepository
                .findById(id)
                .orElseThrow(() -> new AccountException("Account not found"));

        if (!account.isActive()) {
            throw new AccountException(
                    "Account is inactive. Transactions are not allowed.");
        }

        double total = account.getBalance() + amount;
        account.setBalance(total);

        Account savedAccount = accountRepository.save(account);

        // Create deposit transaction
        Transaction transaction = new Transaction();
        transaction.setAccount(savedAccount);
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setBalanceAfterTransaction(total);

        transactionRepository.save(transaction);

        return AccountMapper.mapToAccountDto(savedAccount);
    }


    // Withdraw
    @Transactional
    @Override
    public AccountDto withdraw(Long id, double amount) {

        if (!Double.isFinite(amount) || amount <= 0) {
            throw new AccountException(
                    "Withdrawal amount must be greater than zero");
        }

        Account account = accountRepository
                .findById(id)
                .orElseThrow(() -> new AccountException("Account not found"));

        if (!account.isActive()) {
            throw new AccountException(
                    "Account is inactive. Transactions are not allowed.");
        }

        if (account.getBalance() < amount) {
            throw new AccountException("Insufficient funds");
        }

        double total = account.getBalance() - amount;
        account.setBalance(total);

        Account savedAccount = accountRepository.save(account);

        // Create withdrawal transaction
        Transaction transaction = new Transaction();
        transaction.setAccount(savedAccount);
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setAmount(amount);
        transaction.setBalanceAfterTransaction(total);

        transactionRepository.save(transaction);

        return AccountMapper.mapToAccountDto(savedAccount);
    }

    // Get All Accounts
    @Override
    public List<AccountDto> getAllAccounts() {

        List<Account> accounts = accountRepository.findAll();

        return accounts.stream()
                .map(AccountMapper::mapToAccountDto)
                .collect(Collectors.toList());
    }


    // Delete Account
//    @Override
//    public void deleteAccount(Long id) {
//
//        Account account = accountRepository
//                .findById(id)
//                .orElseThrow(() -> new AccountException("Account not found"));
//
//        accountRepository.deleteById(id);
//    }
    @Override
    public AccountDto getAccountByAccountNumber(String accountNumber) {

        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account not found"));

        return AccountMapper.mapToAccountDto(account);
    }

    // Deactivate Account
    @Override
    public void deactivateAccount(Long id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountException("Account not found"));

        account.setActive(false);

        accountRepository.save(account);
    }

    // Activate Account
    @Override
    public void activateAccount(Long id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountException("Account not found"));

        account.setActive(true);

        accountRepository.save(account);
    }


    // Edit Account Balance - Active Accounts Only
    @Override
    public AccountDto updateBalance(Long id, double newBalance) {

        if (!Double.isFinite(newBalance) || newBalance < 0) {
            throw new AccountException(
                    "Balance cannot be negative or invalid");
        }

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountException("Account not found"));

        // Allow editing only active accounts
        if (!account.isActive()) {
            throw new AccountException(
                    "Cannot edit an inactive account");
        }

        account.setBalance(newBalance);

        Account savedAccount = accountRepository.save(account);

        return AccountMapper.mapToAccountDto(savedAccount);
    }


    // Get Transaction History
    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionHistory(Long accountId) {

        // Check whether account exists
        accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountException("Account not found"));

        // Fetch transactions, newest first
        List<Transaction> transactions =
                transactionRepository
                        .findByAccount_IdOrderByTransactionDateDesc(accountId);

        // Convert entities into DTOs
        return transactions.stream()
                .map(TransactionMapper::mapToTransactionDto)
                .collect(Collectors.toList());
    }

    // Edit Account - Active Accounts Only
    @Override
    public AccountDto updateAccount(
            Long id,
            String accountHolderName,
            String branchName,
            String district) {

        if (accountHolderName == null || accountHolderName.trim().isEmpty()) {
            throw new AccountException("Account holder name cannot be empty");
        }

        if (branchName == null || branchName.trim().isEmpty()) {
            throw new AccountException("Branch name cannot be empty");
        }

        if (district == null || district.trim().isEmpty()) {
            throw new AccountException("District cannot be empty");
        }

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountException("Account not found"));

        if (!account.isActive()) {
            throw new AccountException("Cannot edit an inactive account");
        }

        account.setAccountHolderName(accountHolderName.trim());
        account.setBranchName(branchName.trim());
        account.setDistrict(district.trim());

        Account savedAccount = accountRepository.save(account);

        return AccountMapper.mapToAccountDto(savedAccount);
    }


}
