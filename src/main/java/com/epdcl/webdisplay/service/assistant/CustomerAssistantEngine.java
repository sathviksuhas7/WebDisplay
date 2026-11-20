package com.epdcl.webdisplay.service.assistant;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Built-in customer assistant. Pure Java with no framework dependencies, so it
 * is fast, predictable and easy to test.
 *
 * It answers only from the signed-in customer's own data, which the controller
 * loads from the session. It never performs actions: it explains and returns
 * "actions" that take the customer to the right screen of the dashboard.
 */
public class CustomerAssistantEngine {

    // ---- Data the engine works with (filled by the controller) ----
    public record Account(String number, double balance, boolean active, String branch, String district) {}
    public record Txn(String accountNumber, LocalDateTime date, String type, double amount, String status,
                      String counterparty) {}
    public record Beneficiary(String nickname, String holderName, String accountNumber) {}
    public record Ticket(Long id, String subject, String status, String priority, LocalDateTime createdAt) {}
    public record Context(String customerName, List<Account> accounts, List<Txn> transactions,
                          List<Beneficiary> beneficiaries, List<Ticket> tickets) {}

    /** Reply text, quick-reply chips, navigation buttons, and whether the question was understood. */
    public record Reply(String text, List<String> suggestions, List<Map<String, Object>> actions, boolean understood) {}

    public static final List<String> MAIN_SUGGESTIONS = List.of(
            "Check my balance", "Recent transactions", "Send money",
            "My support tickets", "Download statement", "Report a problem");

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final Pattern TICKET_NUMBER = Pattern.compile("(?:ticket|complaint|request|#)\\s*(?:no|number)?\\s*#?\\s*(\\d+)");

    public Reply welcome(String customerName) {
        return ok("Hi " + firstName(customerName) + "! 👋 I'm your WebDisplay assistant.\n"
                + "I can check your balance, show recent transactions, track support tickets and guide you "
                + "through anything on this portal. What would you like to do?", MAIN_SUGGESTIONS, List.of());
    }

    public Reply answer(String message, Context ctx) {
        String q = " " + message.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9# ]", " ")
                .replaceAll("\\s+", " ").trim() + " ";
        int words = q.trim().isEmpty() ? 0 : q.trim().split(" ").length;

        // 1. Safety topics always win.
        if (has(q, "fraud", "scam", "hacked", "hack ", "stolen", "unauthori", "not done by me", "didn t do",
                "did not do", "suspicious", "someone used")) {
            return ok("I'm sorry this happened — let's secure your account:\n"
                    + "• Change your password right away.\n"
                    + "• Never share your password or OTP. The bank will never ask for them.\n"
                    + "• Raise an URGENT ticket so our team can investigate the transactions.",
                    List.of("Recent transactions"),
                    List.of(action("Raise urgent ticket", "support", "subject",
                                    Map.of("priority", "URGENT", "category", "TRANSACTION",
                                            "subject", "Unauthorised / suspicious transaction")),
                            action("Change password", "link:/change-password", null, null)));
        }
        if (has(q, " otp ", " pin ", " cvv ", "share my password", "give my password")) {
            return ok("For your safety, never share your OTP, PIN, CVV or password with anyone — not even bank "
                    + "staff or me. OTPs are only typed into this portal's own verification screens.",
                    MAIN_SUGGESTIONS, List.of());
        }

        // 2. A specific ticket number.
        Matcher m = TICKET_NUMBER.matcher(q);
        if (has(q, "ticket", "complaint", "request", "#") && m.find()) {
            return ticketDetail(Long.parseLong(m.group(1)), ctx);
        }

        // 3. Short greetings / thanks (only when that is all the message says).
        if (words <= 4 && has(q, " hi ", " hello ", " hey ", " hii ", "good morning", "good afternoon",
                "good evening", "namaste")) {
            return ok("Hello " + firstName(ctx.customerName()) + "! How can I help you today?", MAIN_SUGGESTIONS, List.of());
        }
        if (words <= 5 && has(q, "thank", " ok ", "okay", " great ", " bye ", " cool ")) {
            return ok("You're welcome! I'm here whenever you need me. 😊", MAIN_SUGGESTIONS, List.of());
        }

        // 4. Tasks ("how do I ...").
        if (has(q, "add beneficiar", "new beneficiar", "save beneficiar", "add payee", "save a payee")) {
            return ok("To save a beneficiary: open Send Money → Saved Beneficiaries, enter a nickname and their "
                    + "account number, then save. Next time you can pay them with one tap.",
                    List.of("Send money"), List.of(action("Add a beneficiary", "transfer", "beneficiaryNickname", null)));
        }
        if (has(q, "statement", "passbook", "download")) {
            return ok("Your account statement is under Transactions & Statements: choose the account and dates, "
                    + "then view or download it.",
                    List.of("Recent transactions"), List.of(action("Open statements", "history", "statementDetails", null)));
        }
        if (has(q, "send money", "transfer money", "make a transfer", "how to transfer", "how do i transfer",
                "pay someone", "how to pay", "how do i pay", "fund transfer") || q.trim().equals("transfer")) {
            return ok("To send money:\n"
                    + "1. Open Send Money and choose the account to pay from.\n"
                    + "2. Enter the recipient's account number (or tap a saved beneficiary) and press Verify.\n"
                    + "3. Check the name shown, enter the amount and confirm.\n"
                    + "Available balance: " + money(totalBalance(ctx)) + ".",
                    List.of("Saved beneficiaries", "Check my balance"),
                    List.of(action("Open Send Money", "transfer", "recipientAccountNumber", null)));
        }
        if (has(q, "phone number", "mobile number", "change phone", "change mobile", "update phone", "email",
                "address", "update my details", "change my details", "edit profile", "update profile")) {
            return ok("You can update your phone, email or address from My Profile → Manage Profile Changes. "
                    + "We send an OTP to confirm it's you, and an address change also needs a proof document that "
                    + "the bank verifies.",
                    List.of("Change password"),
                    List.of(action("Update my details", "home", "profileEditForm", Map.of("open", "profile"))));
        }
        if (has(q, "password", "forgot", "can t login", "cannot login", "unable to login", "locked out",
                "login problem", "login issue")) {
            return ok("You can change your password on the Change Password page. If you've forgotten it, contact "
                    + "the Helpdesk — they'll verify you and help you reset it.",
                    List.of("Report a problem"),
                    List.of(action("Change password", "link:/change-password", null, null),
                            action("Contact support", "support", "subject", Map.of("category", "LOGIN"))));
        }
        if (has(q, "complain", "problem", "issue", "raise", "report", "talk to", "human", "agent", "customer care",
                "contact", "support team", "call me")) {
            return ok("I'll take you to the support form. Describe the problem and our Helpdesk team will reply — "
                    + "you can follow the conversation under My Support Tickets.",
                    List.of("My support tickets"), List.of(action("Raise a support ticket", "support", "subject", null)));
        }

        // 5. Information from the customer's data.
        if (has(q, "branch", "ifsc", "timing", "working hours")) {
            return ok(branchText(ctx), MAIN_SUGGESTIONS, List.of());
        }
        if (has(q, "beneficiar", "payee", "saved account")) return beneficiaries(ctx);
        if (has(q, "ticket", "my request", "my complaint")) return tickets(ctx);
        if (has(q, "last transaction", "latest transaction", "last payment", "last transfer", "recent payment")) {
            return transactions(ctx, 1);
        }
        if (has(q, "transaction", "history", "spent", "debit", "credit", "deposit", "withdraw", "received",
                "recent", "payment")) {
            return transactions(ctx, 5);
        }
        if (has(q, "balance", "how much", "money do i have", "my account", "accounts", "overview", "funds",
                "account details")) {
            return balance(ctx);
        }
        if (has(q, "what can you do", "help", "options", "menu", "features", "who are you")) {
            return ok("Here's what I can do:\n"
                    + "• Show your balance and account status\n"
                    + "• List recent or latest transactions\n"
                    + "• Show saved beneficiaries\n"
                    + "• Track support tickets (try \"status of ticket 12\")\n"
                    + "• Guide you to send money, download statements, update phone/email/address, change "
                    + "password or raise a complaint\n"
                    + "I never move money or change details myself — I take you to the right screen.",
                    MAIN_SUGGESTIONS, List.of());
        }

        return new Reply("Sorry, I didn't quite catch that. Try \"What's my balance?\", \"Show my last "
                + "transaction\", \"Status of ticket 12\" or \"How do I send money?\".",
                MAIN_SUGGESTIONS, List.of(action("Ask the Helpdesk", "support", "subject", null)), false);
    }

    // ---- Data answers ----

    private Reply balance(Context ctx) {
        if (ctx.accounts().isEmpty()) {
            return ok("You don't have an account linked to your profile yet. If you applied recently, it will "
                    + "appear once the bank verifies your documents.", List.of("Report a problem"), List.of());
        }
        StringBuilder t = new StringBuilder(ctx.accounts().size() == 1 ? "Your account:\n" : "Your accounts:\n");
        for (Account a : ctx.accounts()) {
            t.append("• A/c ").append(mask(a.number())).append(" — ").append(money(a.balance()))
                    .append(a.active() ? "" : " (inactive)")
                    .append(a.branch() == null || a.branch().isBlank() ? "" : " · " + a.branch() + " branch")
                    .append('\n');
        }
        if (ctx.accounts().size() > 1) t.append("Total available: ").append(money(totalBalance(ctx)));
        return ok(t.toString().trim(), List.of("Recent transactions", "Send money", "Download statement"),
                List.of(action("View my accounts", "home", "accountList", null)));
    }

    private Reply transactions(Context ctx, int limit) {
        List<Txn> list = new ArrayList<>(ctx.transactions());
        if (list.isEmpty()) {
            return ok("There are no transactions on your accounts yet.", List.of("Check my balance", "Send money"), List.of());
        }
        list.sort(Comparator.comparing(Txn::date, Comparator.nullsLast(Comparator.reverseOrder())));
        boolean many = ctx.accounts().size() > 1;
        StringBuilder t = new StringBuilder(limit == 1 ? "Your latest transaction:\n" : "Your recent transactions:\n");
        list.stream().limit(limit).forEach(x -> {
            boolean credit = isCredit(x.type());
            t.append("• ").append(x.date() == null ? "" : x.date().format(DATE) + " — ")
                    .append(describe(x)).append(": ").append(credit ? "+" : "−").append(money(x.amount()))
                    .append(many ? " (A/c " + mask(x.accountNumber()) + ")" : "")
                    .append(x.status() == null || "SUCCESS".equalsIgnoreCase(x.status()) ? "" : " · " + x.status())
                    .append('\n');
        });
        return ok(t.toString().trim(), List.of("Download statement", "Report a problem"),
                List.of(action("See full history", "history", "transactionTable", null)));
    }

    private Reply beneficiaries(Context ctx) {
        if (ctx.beneficiaries().isEmpty()) {
            return ok("You haven't saved any beneficiaries yet. Saving one lets you pay them faster next time.",
                    List.of("Send money"), List.of(action("Add a beneficiary", "transfer", "beneficiaryNickname", null)));
        }
        StringBuilder t = new StringBuilder("Your saved beneficiaries:\n");
        ctx.beneficiaries().stream().limit(10).forEach(b -> t.append("• ").append(b.nickname())
                .append(b.holderName() == null ? "" : " — " + b.holderName())
                .append(" (A/c ").append(mask(b.accountNumber())).append(")\n"));
        return ok(t.toString().trim(), List.of("Send money"),
                List.of(action("Pay a beneficiary", "transfer", "beneficiaryList", null)));
    }

    private Reply tickets(Context ctx) {
        if (ctx.tickets().isEmpty()) {
            return ok("You don't have any support tickets. If something's wrong, I can take you to the form.",
                    List.of("Report a problem"), List.of(action("Raise a support ticket", "support", "subject", null)));
        }
        StringBuilder t = new StringBuilder("Your recent support tickets:\n");
        ctx.tickets().stream().limit(5).forEach(x -> t.append("• #").append(x.id()).append(" ")
                .append(x.subject()).append(" — ").append(friendlyStatus(x.status())).append('\n'));
        t.append("Ask \"status of ticket ").append(ctx.tickets().get(0).id()).append("\" for details.");
        return ok(t.toString(), List.of("Report a problem"),
                List.of(action("Open my tickets", "support", "ticketTable", null)));
    }

    private Reply ticketDetail(long id, Context ctx) {
        Optional<Ticket> found = ctx.tickets().stream().filter(t -> t.id() != null && t.id() == id).findFirst();
        if (found.isEmpty()) {
            return ok("I couldn't find ticket #" + id + " on your profile. Ask \"my support tickets\" to see the ones you have.",
                    List.of("My support tickets"), List.of(action("Open my tickets", "support", "ticketTable", null)));
        }
        Ticket t = found.get();
        return ok("Ticket #" + t.id() + " — " + t.subject() + "\n"
                        + "• Status: " + friendlyStatus(t.status()) + "\n"
                        + "• Priority: " + Objects.toString(t.priority(), "-") + "\n"
                        + (t.createdAt() == null ? "" : "• Raised on: " + t.createdAt().format(DAY) + "\n")
                        + "You can read and reply to the conversation under My Support Tickets.",
                List.of("My support tickets"), List.of(action("Open ticket", "support", "ticketTable", null)));
    }

    // ---- Helpers ----

    private static Reply ok(String text, List<String> suggestions, List<Map<String, Object>> actions) {
        return new Reply(text, suggestions, actions, true);
    }

    private static boolean has(String q, String... words) {
        for (String w : words) if (q.contains(w)) return true;
        return false;
    }

    private static boolean isCredit(String type) {
        String t = type == null ? "" : type.toUpperCase(Locale.ROOT);
        return t.equals("DEPOSIT") || t.equals("TRANSFER_CREDIT");
    }

    private static String describe(Txn x) {
        String type = x.type() == null ? "" : x.type().toUpperCase(Locale.ROOT);
        String who = x.counterparty() == null || x.counterparty().isBlank() ? "" : x.counterparty();
        return switch (type) {
            case "DEPOSIT" -> "Deposit";
            case "WITHDRAWAL" -> "Withdrawal";
            case "TRANSFER_DEBIT" -> who.isEmpty() ? "Money sent" : "Sent to " + who;
            case "TRANSFER_CREDIT" -> who.isEmpty() ? "Money received" : "Received from " + who;
            case "TRANSFER" -> "Transfer";
            case "" -> "Transaction";
            default -> type.charAt(0) + type.substring(1).toLowerCase(Locale.ROOT).replace('_', ' ');
        };
    }

    static String friendlyStatus(String status) {
        if (status == null) return "-";
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "OPEN" -> "Open (waiting for an agent)";
            case "IN_PROGRESS" -> "In progress";
            case "PENDING_REVIEW" -> "Under manager review";
            case "RESOLVED" -> "Resolved";
            case "CLOSED" -> "Closed";
            default -> status;
        };
    }

    private static double totalBalance(Context ctx) {
        return ctx.accounts().stream().filter(Account::active).mapToDouble(Account::balance).sum();
    }

    private static String branchText(Context ctx) {
        Set<String> branches = new LinkedHashSet<>();
        for (Account a : ctx.accounts()) {
            if (a.branch() != null && !a.branch().isBlank()) {
                branches.add(a.branch() + (a.district() == null || a.district().isBlank() ? "" : ", " + a.district()));
            }
        }
        return branches.isEmpty()
                ? "I don't have branch details for your accounts yet. Please contact the Helpdesk."
                : "Your account branch: " + String.join("; ", branches) + ".";
    }

    static String money(double amount) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).format(amount);
    }

    static String mask(String number) {
        if (number == null || number.length() <= 4) return "XXXX";
        return "XXXX" + number.substring(number.length() - 4);
    }

    static String firstName(String name) {
        if (name == null || name.isBlank()) return "there";
        return name.trim().split("\\s+")[0];
    }

    private static Map<String, Object> action(String label, String tab, String focus, Map<String, String> prefill) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("label", label);
        a.put("tab", tab);
        if (focus != null) a.put("focus", focus);
        if (prefill != null) a.put("prefill", prefill);
        return a;
    }
}
