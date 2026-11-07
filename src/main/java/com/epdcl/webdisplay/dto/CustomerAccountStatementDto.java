package com.epdcl.webdisplay.dto;

import java.util.List;

public class CustomerAccountStatementDto {

    private String accountNumber;
    private String accountHolderName;
    private String branchName;
    private String district;
    private double currentBalance;
    private boolean active;
    private List<TransactionDto> transactions;

    public CustomerAccountStatementDto(
            String accountNumber,
            String accountHolderName,
            String branchName,
            String district,
            double currentBalance,
            boolean active,
            List<TransactionDto> transactions) {

        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.branchName = branchName;
        this.district = district;
        this.currentBalance = currentBalance;
        this.active = active;
        this.transactions = transactions;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolderName() { return accountHolderName; }
    public String getBranchName() { return branchName; }
    public String getDistrict() { return district; }
    public double getCurrentBalance() { return currentBalance; }
    public boolean isActive() { return active; }
    public List<TransactionDto> getTransactions() { return transactions; }
}