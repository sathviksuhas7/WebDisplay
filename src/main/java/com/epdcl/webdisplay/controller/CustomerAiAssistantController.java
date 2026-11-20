package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.dto.AccountDto;
import com.epdcl.webdisplay.dto.TransactionDto;
import com.epdcl.webdisplay.entity.Customer;
import com.epdcl.webdisplay.entity.SupportTicket;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.repo.SupportTicketRepository;
import com.epdcl.webdisplay.service.assistant.CustomerAssistantEngine;
import com.epdcl.webdisplay.service.assistant.CustomerAssistantEngine.Context;
import com.epdcl.webdisplay.service.assistant.CustomerAssistantEngine.Reply;
import com.epdcl.webdisplay.service.impl.AccountService;
import com.epdcl.webdisplay.service.impl.BeneficiaryService;
import com.epdcl.webdisplay.service.impl.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;

/**
 * Customer chat assistant endpoint.
 *
 * The built-in {@link CustomerAssistantEngine} always works and answers from the
 * signed-in customer's own data (identity comes from the session, never from the
 * browser). If an n8n webhook is configured, questions the built-in assistant
 * does not understand are sent to it; if n8n fails, the built-in answer is used.
 */
@RestController
@RequestMapping("/api/customer/assistant")
public class CustomerAiAssistantController {

    private static final Logger log = LoggerFactory.getLogger(CustomerAiAssistantController.class);

    private final CustomerService customerService;
    private final AccountService accountService;
    private final BeneficiaryService beneficiaryService;
    private final SupportTicketRepository ticketRepository;
    private final CustomerAssistantEngine engine = new CustomerAssistantEngine();
    private final String webhookUrl;
    private final String webhookSecret;

    public CustomerAiAssistantController(CustomerService customerService, AccountService accountService,
            BeneficiaryService beneficiaryService, SupportTicketRepository ticketRepository,
            @Value("${app.ai.n8n.webhook-url:}") String webhookUrl,
            @Value("${app.ai.n8n.shared-secret:}") String webhookSecret) {
        this.customerService = customerService;
        this.accountService = accountService;
        this.beneficiaryService = beneficiaryService;
        this.ticketRepository = ticketRepository;
        this.webhookUrl = webhookUrl == null ? "" : webhookUrl.trim();
        this.webhookSecret = webhookSecret == null ? "" : webhookSecret.trim();
    }

    @GetMapping(value = "/welcome", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> welcome(HttpSession session) {
        if (!(session.getAttribute("loggedInUser") instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in again."));
        }
        String name = null;
        try {
            name = customerService.getCustomerByUserId(user.getId()).getCustomerName();
        } catch (RuntimeException ignored) {
            // Generic greeting.
        }
        return ResponseEntity.ok(toJson(engine.welcome(name), "built-in"));
    }

    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> chat(@RequestBody(required = false) ChatRequest body, HttpSession session) {
        if (!(session.getAttribute("loggedInUser") instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in again."));
        }
        if (body == null || body.message() == null || body.message().isBlank() || body.message().length() > 1000) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please type a question (up to 1000 characters)."));
        }

        Customer customer;
        try {
            customer = customerService.getCustomerByUserId(user.getId());
        } catch (RuntimeException ex) {
            return ResponseEntity.ok(Map.of("reply",
                    "I couldn't find a customer profile for your login. Please contact the bank.",
                    "suggestions", List.of(), "actions", List.of(), "mode", "built-in"));
        }

        String message = body.message().trim();
        Context context = loadContext(user, customer);
        Reply reply = engine.answer(message, context);

        if (!reply.understood() && !webhookUrl.isBlank() && !webhookSecret.isBlank()) {
            String generated = askN8n(user, message, context);
            if (generated != null && !generated.isBlank()) {
                return ResponseEntity.ok(toJson(new Reply(generated, reply.suggestions(), List.of(), true), "ai"));
            }
        }
        return ResponseEntity.ok(toJson(reply, "built-in"));
    }

    private Context loadContext(User user, Customer customer) {
        List<CustomerAssistantEngine.Account> accounts = new ArrayList<>();
        List<CustomerAssistantEngine.Txn> txns = new ArrayList<>();
        List<AccountDto> accountDtos;
        try {
            accountDtos = accountService.getAccountsByCustomerId(customer.getCustomerId());
        } catch (RuntimeException ex) {
            accountDtos = List.of();
        }
        for (AccountDto a : accountDtos) {
            accounts.add(new CustomerAssistantEngine.Account(a.getAccountNumber(), a.getBalance(), a.isActive(),
                    a.getBranchName(), a.getDistrict()));
            try {
                for (TransactionDto t : accountService.getCustomerTransactionHistory(
                        Long.valueOf(a.getId()), customer.getCustomerId()).stream().limit(20).toList()) {
                    String type = t.getTransactionType() == null ? "" : t.getTransactionType().toUpperCase(Locale.ROOT);
                    String counterparty = type.equals("TRANSFER_DEBIT") ? t.getToAccountName()
                            : type.equals("TRANSFER_CREDIT") ? t.getFromAccountName() : null;
                    txns.add(new CustomerAssistantEngine.Txn(a.getAccountNumber(), t.getTransactionDate(),
                            t.getTransactionType(), t.getAmount(), t.getStatus(), counterparty));
                }
            } catch (RuntimeException ignored) {
                // An account without history must not break the chat.
            }
        }

        List<CustomerAssistantEngine.Beneficiary> beneficiaries = new ArrayList<>();
        try {
            beneficiaryService.getMyBeneficiaries(user.getId()).forEach(b -> beneficiaries.add(
                    new CustomerAssistantEngine.Beneficiary(b.getNickname(), b.getAccountHolderName(), b.getAccountNumber())));
        } catch (RuntimeException ignored) {
            // No beneficiaries available.
        }

        List<CustomerAssistantEngine.Ticket> tickets = ticketRepository
                .findByCustomerCustomerId(customer.getCustomerId()).stream()
                .sorted(Comparator.comparing(SupportTicket::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(t -> new CustomerAssistantEngine.Ticket(t.getTicketId(), t.getSubject(), t.getStatus(),
                        t.getPriority(), t.getCreatedAt()))
                .toList();

        return new Context(customer.getCustomerName(), accounts, txns, beneficiaries, tickets);
    }

    /** Optional generative AI through n8n. Only masked account data is sent. */
    private String askN8n(User user, String message, Context ctx) {
        try {
            Map<String, Object> customerContext = new LinkedHashMap<>();
            customerContext.put("customerName", ctx.customerName());
            customerContext.put("accounts", ctx.accounts().stream().map(a -> Map.of(
                    "accountNumber", a.number() == null || a.number().length() <= 4 ? "XXXX"
                            : "XXXX" + a.number().substring(a.number().length() - 4),
                    "balance", a.balance(),
                    "status", a.active() ? "ACTIVE" : "INACTIVE")).toList());
            customerContext.put("recentSupportTickets", ctx.tickets().stream().limit(5).map(t -> Map.of(
                    "ticketId", t.id(), "subject", Objects.toString(t.subject(), ""),
                    "status", Objects.toString(t.status(), ""))).toList());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sessionId", "customer-" + user.getId());
            payload.put("message", message);
            payload.put("customerContext", customerContext);

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(Duration.ofSeconds(3));
            factory.setReadTimeout(Duration.ofSeconds(20));
            Map<?, ?> response = RestClient.builder().requestFactory(factory).build()
                    .post().uri(webhookUrl).contentType(MediaType.APPLICATION_JSON)
                    .header("X-WebDisplay-AI-Secret", webhookSecret)
                    .body(payload).retrieve().body(Map.class);
            if (response == null) return null;
            Object text = response.get("reply") != null ? response.get("reply")
                    : response.get("output") != null ? response.get("output") : response.get("text");
            return text == null ? null : text.toString();
        } catch (Exception ex) {
            log.warn("n8n assistant unavailable, using the built-in assistant: {}", ex.getMessage());
            return null;
        }
    }

    private static Map<String, Object> toJson(Reply reply, String mode) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("reply", reply.text());
        m.put("suggestions", reply.suggestions());
        m.put("actions", reply.actions());
        m.put("mode", mode);
        return m;
    }

    public record ChatRequest(String message) {}
}
