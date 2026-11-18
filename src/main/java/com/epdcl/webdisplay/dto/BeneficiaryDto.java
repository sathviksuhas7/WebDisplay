
package com.epdcl.webdisplay.dto;

public class BeneficiaryDto {

    private Long id;
    private String nickname;
    private String accountNumber;
    private String accountHolderName;

    public BeneficiaryDto() {
    }

    public BeneficiaryDto(
            Long id,
            String nickname,
            String accountNumber,
            String accountHolderName) {
        this.id = id;
        this.nickname = nickname;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }
}
