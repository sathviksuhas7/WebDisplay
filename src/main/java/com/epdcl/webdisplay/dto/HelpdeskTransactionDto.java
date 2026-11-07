package com.epdcl.webdisplay.dto;

import java.time.LocalDateTime;

public class HelpdeskTransactionDto {

    private Long transactionId;
    private String accountNumber;
    private String transactionType;
    private double amount;
    private LocalDateTime transactionDate;
    private double balanceAfterTransaction;

    public HelpdeskTransactionDto(
            Long transactionId,
            String accountNumber,
            String transactionType,
            double amount,
            LocalDateTime transactionDate,
            double balanceAfterTransaction) {

        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    public Long getTransactionId() { return transactionId; }
    public String getAccountNumber() { return accountNumber; }
    public String getTransactionType() { return transactionType; }
    public double getAmount() { return amount; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public double getBalanceAfterTransaction() { return balanceAfterTransaction; }
}