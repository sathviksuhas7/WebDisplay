
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.dto.AccountDto;
import com.epdcl.webdisplay.dto.CustomerAccountStatementDto;
import com.epdcl.webdisplay.dto.CustomerProfileDto;
import com.epdcl.webdisplay.dto.TransactionDto;
import com.epdcl.webdisplay.entity.Customer;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.exception.AccountException;
import com.epdcl.webdisplay.service.impl.AccountService;
import com.epdcl.webdisplay.service.impl.CustomerService;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.format.annotation.DateTimeFormat;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerProfileController {

    private final CustomerService customerService;
    private final AccountService accountService;

    public CustomerProfileController(
            CustomerService customerService,
            AccountService accountService) {

        this.customerService = customerService;
        this.accountService = accountService;
    }

    @GetMapping("/profile")
    public ResponseEntity<CustomerProfileDto> getMyProfile(
            HttpSession session) {

        User user = getLoggedInUser(session);

        Customer customer =
                customerService.getCustomerByUserId(user.getId());

        CustomerProfileDto profile = new CustomerProfileDto(
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getDateOfBirth()
        );

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> getMyAccounts(
            HttpSession session) {

        User user = getLoggedInUser(session);

        Customer customer =
                customerService.getCustomerByUserId(user.getId());

        List<AccountDto> accounts =
                accountService.getAccountsByCustomerId(
                        customer.getCustomerId());

        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<TransactionDto>> getMyTransactions(
            @PathVariable Long accountId,
            HttpSession session) {

        User user = getLoggedInUser(session);

        Customer customer =
                customerService.getCustomerByUserId(user.getId());

        List<TransactionDto> transactions =
                accountService.getCustomerTransactionHistory(
                        accountId,
                        customer.getCustomerId());

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/accounts/{accountId}/statement")
    public ResponseEntity<CustomerAccountStatementDto> getMyAccountStatement(
            @PathVariable Long accountId,
            HttpSession session) {

        User user = getLoggedInUser(session);

        Customer customer =
                customerService.getCustomerByUserId(user.getId());

        CustomerAccountStatementDto statement =
                accountService.getCustomerAccountStatement(
                        accountId,
                        customer.getCustomerId());

        return ResponseEntity.ok(statement);
    }

    @GetMapping(value = "/accounts/{accountId}/statement/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> downloadMyAccountStatement(
            @PathVariable Long accountId,
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            HttpSession session) throws IOException {

        User user = getLoggedInUser(session);
        Customer customer = customerService.getCustomerByUserId(user.getId());
        CustomerAccountStatementDto statement = accountService.getCustomerAccountStatement(
                accountId, customer.getCustomerId());
        List<TransactionDto> transactions = accountService.getCustomerTransactionsForPeriod(
                accountId, customer.getCustomerId(), fromDate, toDate);

        byte[] pdf = buildStatementPdf(statement, transactions, fromDate, toDate);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=WebDisplay-Account-Statement.pdf")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(pdf);
    }

    private byte[] buildStatementPdf(
            CustomerAccountStatementDto statement,
            List<TransactionDto> transactions,
            LocalDate fromDate,
            LocalDate toDate) throws IOException {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(document, page);
            float y = 750;

            y = writePdfLine(stream, bold, 16, 50, y, "WebDisplay - Account Statement");
            y = writePdfLine(stream, font, 10, 50, y - 12, "Account: " + statement.getAccountNumber());
            y = writePdfLine(stream, font, 10, 50, y - 4, "Holder: " + statement.getAccountHolderName());
            y = writePdfLine(stream, font, 10, 50, y - 4, "Type: " + statement.getAccountType());
            y = writePdfLine(stream, font, 10, 50, y - 4, "Period: " + fromDate + " to " + toDate);
            y = writePdfLine(stream, font, 10, 50, y - 4, "Current balance: INR " + String.format("%.2f", statement.getCurrentBalance()));
            y = writePdfLine(stream, bold, 10, 50, y - 18,
                    "Date                 Type                 Amount (INR)       Balance After (INR)       Reference");
            for (TransactionDto tx : transactions) {
                if (y < 55) {
                    stream.close();
                    page = new PDPage();
                    document.addPage(page);
                    stream = new PDPageContentStream(document, page);
                    y = 750;
                    y = writePdfLine(stream, bold, 10, 50, y,
                            "Date                 Type                 Amount (INR)       Balance After (INR)       Reference");
                }
                String date = tx.getTransactionDate() == null ? "-" :
                        tx.getTransactionDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
                String line = String.format("%-20s %-20s %-18.2f %-24.2f %s",
                        date, safePdfText(tx.getTransactionType()), tx.getAmount(),
                        tx.getBalanceAfterTransaction(), safePdfText(tx.getReferenceNumber()));
                y = writePdfLine(stream, font, 8, 50, y - 14, line);
            }
            if (transactions.isEmpty()) {
                y = writePdfLine(stream, font, 10, 50, y - 16,
                        "No transactions were recorded in the selected period.");
            }
            stream.close();
            document.save(output);
            return output.toByteArray();
        }
    }

    private float writePdfLine(PDPageContentStream stream, PDType1Font font,
                               int size, float x, float y, String text) throws IOException {
        stream.beginText();
        stream.setFont(font, size);
        stream.newLineAtOffset(x, y);
        stream.showText(safePdfText(text));
        stream.endText();
        return y - size - 5;
    }

    private String safePdfText(String value) {
        if (value == null) return "-";
        return value.replaceAll("[^\\x20-\\x7E]", " ");
    }

    private User getLoggedInUser(HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            throw new AccountException("Please log in again");
        }

        return user;
    }
}
