
package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.dto.AccountDto;
import com.epdcl.webdisplay.dto.TransactionDto;
import com.epdcl.webdisplay.dto.CustomerAccountStatementDto;
import com.epdcl.webdisplay.dto.HelpdeskAccountDto;
import com.epdcl.webdisplay.dto.HelpdeskTransactionDto;

import com.epdcl.webdisplay.entity.Account;
import com.epdcl.webdisplay.entity.Customer;
import com.epdcl.webdisplay.entity.Transaction;
import com.epdcl.webdisplay.entity.BankTransaction;

import com.epdcl.webdisplay.exception.AccountException;
import com.epdcl.webdisplay.mapper.AccountMapper;
import com.epdcl.webdisplay.mapper.TransactionMapper;

import com.epdcl.webdisplay.repo.AccountRepository;
import com.epdcl.webdisplay.repo.CustomerRepository;
import com.epdcl.webdisplay.repo.TransactionRepository;
import com.epdcl.webdisplay.repo.BankTransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final BankTransactionRepository bankTransactionRepository;

    public AccountServiceImpl(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            CustomerRepository customerRepository,
            BankTransactionRepository bankTransactionRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.bankTransactionRepository = bankTransactionRepository;
    }

    // Generate Account Number
    private String generateAccountNumber() {

        String prefix = "DC";

        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("ddMMyy"));

        int randomNumber = new Random().nextInt(10000);

        return prefix + date + String.format("%04d", randomNumber);
    }

    // Create a master bank transaction
    private BankTransaction createBankTransaction(
            Account fromAccount,
            Account toAccount,
            String transactionType,
            double amount,
            String description,
            Long initiatedBy) {

        BankTransaction bankTransaction = new BankTransaction();

        bankTransaction.setReferenceNumber(
                "BT" + UUID.randomUUID().toString()
                        .replace("-", "").toUpperCase());

        if (fromAccount != null) {
            bankTransaction.setFromAccountId(fromAccount.getId());
            bankTransaction.setFromAccountNumber(
                    fromAccount.getAccountNumber());
            bankTransaction.setFromAccountName(
                    fromAccount.getAccountHolderName());
        }

        if (toAccount != null) {
            bankTransaction.setToAccountId(toAccount.getId());
            bankTransaction.setToAccountNumber(
                    toAccount.getAccountNumber());
            bankTransaction.setToAccountName(
                    toAccount.getAccountHolderName());
        }

        bankTransaction.setTransactionType(transactionType);
        bankTransaction.setAmount(BigDecimal.valueOf(amount));
        bankTransaction.setStatus("SUCCESS");
        bankTransaction.setDescription(description);
        bankTransaction.setInitiatedBy(initiatedBy);

        return bankTransactionRepository.save(bankTransaction);
    }

    // Create an account-wise ledger entry
    private void createLedgerEntry(
            Account account,
            BankTransaction bankTransaction,
            String transactionType,
            double amount,
            double balanceAfterTransaction) {

        Transaction transaction = new Transaction();

        transaction.setAccount(account);
        transaction.setBankTransaction(bankTransaction);
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setBalanceAfterTransaction(
                balanceAfterTransaction);

        transactionRepository.save(transaction);
    }

    // Add Account and Link It to a Customer
    @Override
    @Transactional
    public AccountDto createAccount(AccountDto accountDto) {

        if (accountDto.getCustomerId() == null) {
            throw new AccountException("Customer ID is required");
        }

        Customer customer = customerRepository.findById(
                        accountDto.getCustomerId())
                .orElseThrow(() ->
                        new AccountException("Customer not found"));

        String accountNumber;

        do {
            accountNumber = generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        Account account = AccountMapper.mapToAccount(accountDto);

        account.setAccountNumber(accountNumber);
        account.setCustomer(customer);
        account.setAccountHolderName(customer.getCustomerName());

        Account accountSaved = accountRepository.save(account);

        return AccountMapper.mapToAccountDto(accountSaved);
    }

    // Get Account By ID
    @Override
    public AccountDto getAccountById(Long id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountException("Account not found"));

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

        Account account = accountRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new AccountException("Account not found"));

        if (!account.isActive()) {
            throw new AccountException(
                    "Account is inactive. Transactions are not allowed.");
        }

        double total = account.getBalance() + amount;
        account.setBalance(total);

        Account savedAccount = accountRepository.save(account);

        Long customerId = account.getCustomer() != null
                ? account.getCustomer().getCustomerId()
                : null;

        BankTransaction bankTransaction = createBankTransaction(
                null,
                savedAccount,
                "DEPOSIT",
                amount,
                "Cash deposit",
                customerId
        );

        createLedgerEntry(
                savedAccount,
                bankTransaction,
                "DEPOSIT",
                amount,
                total
        );

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

        Account account = accountRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new AccountException("Account not found"));

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

        Long customerId = account.getCustomer() != null
                ? account.getCustomer().getCustomerId()
                : null;

        BankTransaction bankTransaction = createBankTransaction(
                savedAccount,
                null,
                "WITHDRAWAL",
                amount,
                "Cash withdrawal",
                customerId
        );

        createLedgerEntry(
                savedAccount,
                bankTransaction,
                "WITHDRAWAL",
                amount,
                total
        );

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

    // Get Accounts By Customer ID
    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsByCustomerId(Long customerId) {

        List<Account> accounts =
                accountRepository.findByCustomerCustomerId(customerId);

        return accounts.stream()
                .map(AccountMapper::mapToAccountDto)
                .collect(Collectors.toList());
    }

    // Get Account By Account Number
    @Override
    public AccountDto getAccountByAccountNumber(String accountNumber) {

        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new AccountException("Account not found"));

        return AccountMapper.mapToAccountDto(account);
    }

    // Deactivate Account
    @Override
    public void deactivateAccount(Long id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountException("Account not found"));

        account.setActive(false);

        accountRepository.save(account);
    }

    // Activate Account
    @Override
    public void activateAccount(Long id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountException("Account not found"));

        account.setActive(true);

        accountRepository.save(account);
    }

    // Get Transaction History
    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionHistory(Long accountId) {

        accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new AccountException("Account not found"));

        List<Transaction> transactions =
                transactionRepository
                        .findByAccount_IdOrderByTransactionDateDesc(accountId);

        return transactions.stream()
                .map(TransactionMapper::mapToTransactionDto)
                .collect(Collectors.toList());
    }

    // Get Transaction History for a Specific Customer
    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> getCustomerTransactionHistory(
            Long accountId, Long customerId) {

        Account account = accountRepository
                .findByIdAndCustomerCustomerId(accountId, customerId)
                .orElseThrow(() ->
                        new AccountException(
                                "Account not found for this customer"));

        List<Transaction> transactions =
                transactionRepository
                        .findByAccount_IdOrderByTransactionDateDesc(
                                account.getId());

        return transactions.stream()
                .map(TransactionMapper::mapToTransactionDto)
                .collect(Collectors.toList());
    }

    // Edit Account
    @Override
    public AccountDto updateAccount(
            Long id,
            String accountHolderName,
            String branchName,
            String district) {

        if (accountHolderName == null
                || accountHolderName.trim().isEmpty()) {
            throw new AccountException(
                    "Account holder name cannot be empty");
        }

        if (branchName == null || branchName.trim().isEmpty()) {
            throw new AccountException("Branch name cannot be empty");
        }

        if (district == null || district.trim().isEmpty()) {
            throw new AccountException("District cannot be empty");
        }

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountException("Account not found"));

        if (!account.isActive()) {
            throw new AccountException(
                    "Cannot edit an inactive account");
        }

        account.setAccountHolderName(accountHolderName.trim());
        account.setBranchName(branchName.trim());
        account.setDistrict(district.trim());

        Account savedAccount = accountRepository.save(account);

        return AccountMapper.mapToAccountDto(savedAccount);
    }

    // Get Customer Account Statement
    @Override
    @Transactional(readOnly = true)
    public CustomerAccountStatementDto getCustomerAccountStatement(
            Long accountId, Long customerId) {

        Account account = accountRepository
                .findByIdAndCustomerCustomerId(accountId, customerId)
                .orElseThrow(() ->
                        new AccountException(
                                "Account not found for this customer"));

        List<TransactionDto> transactions =
                getCustomerTransactionHistory(accountId, customerId);

        return new CustomerAccountStatementDto(
                account.getAccountNumber(),
                account.getAccountHolderName(),
                account.getBranchName(),
                account.getDistrict(),
                account.getBalance(),
                account.isActive(),
                transactions
        );
    }

    // Helpdesk Views Customer Accounts
    @Override
    @Transactional(readOnly = true)
    public List<HelpdeskAccountDto> getHelpdeskCustomerAccounts(
            Long customerId) {

        List<Account> accounts =
                accountRepository.findByCustomerCustomerId(customerId);

        return accounts.stream()
                .map(account -> {

                    String accountNumber = account.getAccountNumber();

                    String maskedNumber = accountNumber.length() > 4
                            ? "XXXX" + accountNumber.substring(
                            accountNumber.length() - 4)
                            : "XXXX";

                    return new HelpdeskAccountDto(
                            maskedNumber,
                            account.getAccountHolderName(),
                            account.getBranchName(),
                            account.getDistrict(),
                            account.isActive()
                    );
                })
                .collect(Collectors.toList());
    }

    // Helpdesk Views Customer Transaction History
    @Override
    @Transactional(readOnly = true)
    public List<HelpdeskTransactionDto> getHelpdeskCustomerTransactions(
            Long customerId) {

        List<Account> accounts =
                accountRepository.findByCustomerCustomerId(customerId);

        return accounts.stream()
                .flatMap(account ->
                        transactionRepository
                                .findByAccount_IdOrderByTransactionDateDesc(
                                        account.getId())
                                .stream()
                                .map(transaction -> {

                                    String number =
                                            account.getAccountNumber();

                                    String maskedNumber = number.length() > 4
                                            ? "XXXX" + number.substring(
                                            number.length() - 4)
                                            : "XXXX";

                                    return new HelpdeskTransactionDto(
                                            transaction.getId(),
                                            maskedNumber,
                                            transaction.getTransactionType(),
                                            transaction.getAmount(),
                                            transaction.getTransactionDate(),
                                            transaction.getBalanceAfterTransaction()
                                    );
                                }))
                .collect(Collectors.toList());
    }

    // Transfer Money
    @Override
    @Transactional
    public AccountDto transfer(
            Long senderAccountId,
            Long customerId,
            String recipientAccountNumber,
            double amount) {

        if (!Double.isFinite(amount) || amount <= 0) {
            throw new AccountException(
                    "Transfer amount must be greater than zero");
        }

        if (recipientAccountNumber == null
                || recipientAccountNumber.isBlank()) {
            throw new AccountException(
                    "Recipient account number is required");
        }

        // Verify sender belongs to logged-in customer
        Account sender = accountRepository
                .findByIdAndCustomerCustomerId(senderAccountId, customerId)
                .orElseThrow(() ->
                        new AccountException("Sender account not found"));

        // Find recipient
        Account recipient = accountRepository
                .findByAccountNumber(recipientAccountNumber.trim())
                .orElseThrow(() ->
                        new AccountException("Recipient account not found"));

        if (sender.getId().equals(recipient.getId())) {
            throw new AccountException(
                    "Cannot transfer to the same account");
        }

        // Lock both accounts in ID order to avoid deadlocks
        Long firstId = Math.min(sender.getId(), recipient.getId());
        Long secondId = Math.max(sender.getId(), recipient.getId());

        Account first = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() ->
                        new AccountException("Account not found"));

        Account second = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() ->
                        new AccountException("Account not found"));

        // Identify locked sender and recipient
        sender = sender.getId().equals(firstId) ? first : second;
        recipient = recipient.getId().equals(firstId) ? first : second;

        if (!sender.isActive() || !recipient.isActive()) {
            throw new AccountException("Both accounts must be active");
        }

        if (sender.getBalance() < amount) {
            throw new AccountException("Insufficient funds");
        }

        // Update balances
        double senderBalance = sender.getBalance() - amount;
        double recipientBalance = recipient.getBalance() + amount;

        sender.setBalance(senderBalance);
        recipient.setBalance(recipientBalance);

        accountRepository.save(sender);
        accountRepository.save(recipient);

        // Create one master transaction for the complete transfer
        BankTransaction bankTransaction = createBankTransaction(
                sender,
                recipient,
                "TRANSFER",
                amount,
                "Account-to-account transfer",
                customerId
        );

        // Sender debit ledger entry
        createLedgerEntry(
                sender,
                bankTransaction,
                "TRANSFER_DEBIT",
                amount,
                senderBalance
        );

        // Recipient credit ledger entry
        createLedgerEntry(
                recipient,
                bankTransaction,
                "TRANSFER_CREDIT",
                amount,
                recipientBalance
        );

        return AccountMapper.mapToAccountDto(sender);
    }

    // Verify Recipient Account
    @Override
    @Transactional(readOnly = true)
    public String verifyRecipientAccount(String accountNumber) {

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new AccountException("Account number is required");
        }

        Account account = accountRepository
                .findByAccountNumber(accountNumber.trim())
                .orElseThrow(() ->
                        new AccountException(
                                "Recipient account not found"));

        if (!account.isActive()) {
            throw new AccountException(
                    "Recipient account is inactive");
        }

        return account.getAccountHolderName();
    }
}
