
package com.epdcl.webdisplay.mapper;

import com.epdcl.webdisplay.dto.TransactionDto;
import com.epdcl.webdisplay.entity.BankTransaction;
import com.epdcl.webdisplay.entity.Transaction;

public class TransactionMapper {

    public static TransactionDto mapToTransactionDto(
            Transaction transaction) {

        TransactionDto dto = new TransactionDto();

        dto.setId(transaction.getId());
        dto.setAccountNumber(
                transaction.getAccount().getAccountNumber());

        dto.setTransactionType(transaction.getTransactionType());
        dto.setAmount(transaction.getAmount());
        dto.setTransactionDate(transaction.getTransactionDate());

        dto.setBalanceAfterTransaction(
                transaction.getBalanceAfterTransaction());

        BankTransaction bankTransaction =
                transaction.getBankTransaction();

        if (bankTransaction != null) {
            dto.setReferenceNumber(
                    bankTransaction.getReferenceNumber());

            dto.setFromAccountNumber(
                    bankTransaction.getFromAccountNumber());

            dto.setFromAccountName(
                    bankTransaction.getFromAccountName());

            dto.setToAccountNumber(
                    bankTransaction.getToAccountNumber());

            dto.setToAccountName(
                    bankTransaction.getToAccountName());

            dto.setStatus(bankTransaction.getStatus());
            dto.setDescription(bankTransaction.getDescription());
        }

        return dto;
    }
}
