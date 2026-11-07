package com.epdcl.webdisplay.dto;

public class HelpdeskAccountDto {

    private String accountNumber;
    private String accountHolderName;
    private String branchName;
    private String district;
    private boolean active;

    public HelpdeskAccountDto(
            String accountNumber,
            String accountHolderName,
            String branchName,
            String district,
            boolean active) {

        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.branchName = branchName;
        this.district = district;
        this.active = active;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getDistrict() {
        return district;
    }

    public boolean isActive() {
        return active;
    }
}