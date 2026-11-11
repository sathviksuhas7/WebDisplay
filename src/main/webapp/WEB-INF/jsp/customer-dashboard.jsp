
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Customer Dashboard | WebDisplay</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="${sessionScope.csrfToken}">

    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: Arial, sans-serif;
        }

        body {
            background: #f4f6f9;
            color: #263238;
        }

        header {
            background: #172b4d;
            color: white;
            padding: 20px 5%;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        header h2 { font-size: 22px; }

        header button {
            background: #dc2626;
            color: white;
            padding: 10px 18px;
            border: 0;
            border-radius: 7px;
            cursor: pointer;
        }

        .container {
            width: 90%;
            max-width: 1200px;
            margin: 30px auto;
        }

        .welcome { margin-bottom: 25px; }
        .welcome h1 { margin-bottom: 8px; }

        .section {
            background: white;
            padding: 22px;
            margin-bottom: 25px;
            border-radius: 9px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
        }

        .section h2 {
            margin-bottom: 18px;
            color: #172b4d;
            font-size: 20px;
        }

        .profile-grid, .account-grid, .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 16px;
        }

        .info-card, .account-card, .stat-card {
            background: #f7f9fc;
            padding: 18px;
            border-radius: 7px;
            border: 1px solid #e5eaf0;
        }

        .info-card p, .account-card p { margin-top: 9px; overflow-wrap: anywhere; }

        .stat-card h3 {
            font-size: 14px;
            color: #68778a;
            margin-bottom: 10px;
        }

        .stat-card p {
            font-size: 23px;
            font-weight: bold;
            color: #172b4d;
            overflow-wrap: anywhere;
        }

        button {
            background: #1769aa;
            color: white;
            border: none;
            padding: 10px 15px;
            border-radius: 5px;
            cursor: pointer;
            margin: 5px 5px 5px 0;
        }

        button:hover { background: #105487; }

        button:disabled {
            background: #9ca3af;
            cursor: not-allowed;
        }

        input, select, textarea {
            width: 100%;
            padding: 11px;
            margin: 8px 0 15px;
            border: 1px solid #ccd3dc;
            border-radius: 5px;
        }

        label { font-weight: bold; font-size: 14px; }

        table {
            width: 100%;
            border-collapse: collapse;
            min-width: 650px;
        }

        th, td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #e5eaf0;
        }

        th { background: #f1f4f8; }
        .table-container { overflow-x: auto; }

        /* Transaction table: compact and tidy */
        .txn-table th, .txn-table td {
            padding: 8px 10px;
            font-size: 13px;
            vertical-align: middle;
            white-space: nowrap;
        }
        .txn-table th {
            font-size: 12px;
            text-transform: uppercase;
            color: #4b5563;
        }
        .txn-table td.desc {
            white-space: normal;
            min-width: 140px;
        }
        .txn-ref {
            font-family: monospace;
            font-size: 12px;
            color: #374151;
        }
        .acc-name { display: block; }
        .acc-no { display: block; font-size: 11px; color: #68778a; }
        .amount { text-align: right; }
        .badge {
            display: inline-block;
            padding: 2px 8px;
            border-radius: 10px;
            font-size: 11px;
            font-weight: bold;
            background: #e5eaf0;
            color: #374151;
        }
        .badge.SUCCESS { background: #dcfce7; color: #15803d; }
        .badge.FAILED { background: #fee2e2; color: #dc2626; }
        .badge.PENDING { background: #fef9c3; color: #a16207; }

        .message {
            margin: 12px 0;
            color: #1769aa;
        }

        .empty {
            color: #68778a;
            padding: 12px 0;
        }

        .form-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 10px;
        }

        .recipient-box {
            background: #f0fdf4;
            border: 1px solid #bbf7d0;
            padding: 14px;
            border-radius: 7px;
            margin: 12px 0;
        }

        .success-message { color: #15803d; }
        .error-message { color: #dc2626; }

        @media (max-width: 600px) {
            header { padding: 18px 5%; }
            .container { width: 94%; }
            .section { padding: 16px; }
        }
    </style>
</head>

<body>

<header>
    <h2>WebDisplay | Customer Portal</h2>

    <form action="<%= request.getContextPath() %>/logout"
          method="post"
          style="display:inline;">
        <input type="hidden" name="csrfToken"
               value="${sessionScope.csrfToken}">
        <button type="submit">Logout</button>
    </form>
</header>

<div class="container">

    <div class="welcome">
        <h1>Welcome, <span id="customerName">Customer</span></h1>
        <p>Manage your accounts, transactions and support requests.</p>
    </div>

    <!-- PROFILE -->
    <div class="section">
        <h2>My Profile</h2>
        <div class="profile-grid" id="profileDetails">
            <p>Loading profile...</p>
        </div>
    </div>

    <!-- ACCOUNTS -->
    <div class="section">
        <h2>My Accounts</h2>
        <div class="account-grid" id="accountList">
            <p>Loading accounts...</p>
        </div>
    </div>

    <!-- ACCOUNT BALANCE -->
    <div class="section">
        <h2>Account Balance</h2>
        <div id="balanceDetails" class="empty">
            Select an account to view its balance.
        </div>
    </div>

    <!-- MONEY TRANSFER -->
    <div class="section">
        <h2>Money Transfer</h2>
        <p>Transfer money to another customer account.</p>

        <label for="senderAccount">Select Your Account</label>
        <select id="senderAccount">
            <option value="">Select Account</option>
        </select>

        <label for="recipientAccountNumber">
            Recipient Account Number
        </label>
        <input type="text"
               id="recipientAccountNumber"
               placeholder="Enter recipient account number"
               autocomplete="off">

        <button type="button" id="verifyButton"
                onclick="verifyRecipient()">
            Verify Recipient
        </button>

        <div id="recipientDetails"></div>

        <label for="transferAmount">Transfer Amount (₹)</label>
        <input type="number"
               id="transferAmount"
               min="0.01"
               step="0.01"
               placeholder="Enter amount">

        <button type="button"
                id="transferButton"
                onclick="transferMoney()"
                disabled>
            Confirm Transfer
        </button>

        <p id="transferMessage" class="message"></p>
    </div>

    <!-- TRANSACTIONS -->
    <div class="section">
        <h2>Transaction History</h2>

        <div class="table-container">
            <table class="txn-table">
                <thead>
                    <tr>
                        <th>Reference No.</th>
                        <th>Type</th>
                        <th>To Account</th>
                        <th>Amount</th>
                        <th>Status</th>
                        <th>Date</th>
                        <th>Balance After</th>
                        <th>Description</th>
                    </tr>
                </thead>

                <tbody id="transactionTable">
                    <tr>
                        <td colspan="8">
                            Select an account to view transactions.
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

    <!-- STATEMENT -->
    <div class="section">
        <h2>Account Statement</h2>
        <button onclick="loadStatement()">View Account Statement</button>
        <div id="statementDetails"></div>
    </div>

    <!-- RAISE SUPPORT TICKET -->
    <div class="section">
        <h2>Raise a Support Ticket</h2>

        <form id="ticketForm">
            <label>Category</label>
            <select id="category" required>
                <option value="">Select Category</option>
                <option value="ACCOUNT">Account Issue</option>
                <option value="TRANSACTION">Transaction Issue</option>
                <option value="LOGIN">Login Issue</option>
                <option value="OTHER">Other</option>
            </select>

            <label>Subject</label>
            <input type="text" id="subject" required maxlength="200"
                   placeholder="Enter ticket subject">

            <label>Description</label>
            <textarea id="description" rows="4" required
                      placeholder="Explain your issue"></textarea>

            <label>Priority</label>
            <select id="priority" required>
                <option value="LOW">Low</option>
                <option value="MEDIUM" selected>Medium</option>
                <option value="HIGH">High</option>
                <option value="URGENT">Urgent</option>
            </select>

            <button type="submit">Submit Ticket</button>
        </form>

        <p class="message" id="ticketMessage"></p>
    </div>

    <!-- SUPPORT TICKETS -->
    <div class="section">
        <h2>My Support Tickets</h2>
        <button onclick="loadTickets()">Refresh Tickets</button>

        <div class="table-container">
            <table>
                <thead>
                <tr>
                    <th>Ticket ID</th>
                    <th>Category</th>
                    <th>Subject</th>
                    <th>Priority</th>
                    <th>Status</th>
                    <th>Resolution Remarks</th>
                    <th>Created At</th>
                </tr>
                </thead>
                <tbody id="ticketTable">
                <tr><td colspan="7">Loading tickets...</td></tr>
                </tbody>
            </table>
        </div>
    </div>

</div>

<script>
    const API = "<%= request.getContextPath() %>/api";

    let selectedAccountId = null;
    let verifiedRecipient = null;

    // CSRF TOKEN
    function getCsrfToken() {
        return document.querySelector('meta[name="csrf-token"]').content;
    }

    // COMMON API REQUEST
    async function apiRequest(url, options = {}) {
        const response = await fetch(url, {
            ...options,
            credentials: "same-origin",
            headers: {
                ...(options.body ? {"Content-Type": "application/json"} : {}),
                ...(options.method && options.method !== "GET"
                    ? {"X-CSRF-TOKEN": getCsrfToken()} : {}),
                ...(options.headers || {})
            }
        });

        const text = await response.text();
        let data = null;

        try {
            data = text ? JSON.parse(text) : null;
        } catch (e) {
            data = text;
        }

        if (!response.ok) {
            const message = data && typeof data === "object"
                ? (data.message || data.error || JSON.stringify(data))
                : (data || "Request failed: " + response.status);

            throw new Error(message);
        }

        return data;
    }

    function escapeHtml(value) {
        return String(value ?? "").replace(/[&<>"']/g, char => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        })[char]);
    }

    function formatDate(value) {
        if (!value) return "-";
        const date = new Date(value);
        return isNaN(date.getTime())
            ? escapeHtml(value)
            : date.toLocaleString();
    }

    function showError(elementId, error) {
        document.getElementById(elementId).innerHTML =
            '<p class="empty">Unable to load data: ' +
            escapeHtml(error.message) + '</p>';
    }

    // LOAD CUSTOMER PROFILE
    async function loadProfile() {
        try {
            const customer = await apiRequest(API + "/customer/profile");

            document.getElementById("customerName").textContent =
                customer.customerName || "Customer";

            document.getElementById("profileDetails").innerHTML = `
                <div class="info-card">
                    <strong>Customer ID</strong>
                    <p>\${escapeHtml(customer.customerId)}</p>
                </div>
                <div class="info-card">
                    <strong>Name</strong>
                    <p>\${escapeHtml(customer.customerName)}</p>
                </div>
                <div class="info-card">
                    <strong>Phone</strong>
                    <p>\${escapeHtml(customer.phone)}</p>
                </div>
                <div class="info-card">
                    <strong>Address</strong>
                    <p>\${escapeHtml(customer.address)}</p>
                </div>
                <div class="info-card">
                    <strong>Date of Birth</strong>
                    <p>\${escapeHtml(customer.dateOfBirth)}</p>
                </div>
            `;
        } catch (error) {
            showError("profileDetails", error);
        }
    }

    // LOAD CUSTOMER ACCOUNTS AND POPULATE TRANSFER DROPDOWN
    async function loadAccounts() {
        try {
            const accounts = await apiRequest(API + "/customer/accounts");
            const container = document.getElementById("accountList");
            const senderSelect = document.getElementById("senderAccount");

            senderSelect.innerHTML =
                '<option value="">Select Account</option>';

            if (!accounts || accounts.length === 0) {
                container.innerHTML =
                    '<p class="empty">No accounts found.</p>';
                return;
            }

            accounts.forEach(account => {
                if (account.active) {
                    const option = document.createElement("option");
                    option.value = account.id;
                    option.textContent =
                        account.accountNumber + " - " +
                        account.accountHolderName;
                    senderSelect.appendChild(option);
                }
            });

            container.innerHTML = accounts.map(account => `
                <div class="account-card">
                    <h3>\${escapeHtml(account.accountNumber)}</h3>
                    <p><strong>Holder:</strong>
                        \${escapeHtml(account.accountHolderName)}</p>
                    <p><strong>Branch:</strong>
                        \${escapeHtml(account.branchName)}</p>
                    <p><strong>District:</strong>
                        \${escapeHtml(account.district)}</p>
                    <p><strong>Status:</strong>
                        \${account.active ? "Active" : "Inactive"}</p>
                    <button onclick="selectAccount('\${escapeHtml(account.id)}')">
                        View Account
                    </button>
                </div>
            `).join("");
        } catch (error) {
            showError("accountList", error);
        }
    }

    // SELECT ACCOUNT
    async function selectAccount(accountId) {
        selectedAccountId = accountId;

        document.getElementById("balanceDetails").innerHTML =
            "Loading account balance...";

        try {
            const statement = await apiRequest(
                API + "/customer/accounts/" +
                encodeURIComponent(accountId) + "/statement"
            );

            document.getElementById("balanceDetails").innerHTML = `
                <div class="stats-grid">
                    <div class="stat-card">
                        <h3>Account Number</h3>
                        <p>\${escapeHtml(statement.accountNumber)}</p>
                    </div>
                    <div class="stat-card">
                        <h3>Current Balance</h3>
                        <p>₹\${Number(statement.currentBalance).toLocaleString("en-IN", {
                            minimumFractionDigits: 2
                        })}</p>
                    </div>
                    <div class="stat-card">
                        <h3>Account Status</h3>
                        <p>\${statement.active ? "Active" : "Inactive"}</p>
                    </div>
                </div>
            `;

            await loadTransactions();
        } catch (error) {
            showError("balanceDetails", error);
        }
    }


    // ACCOUNT CELL: name on top, account number below in small grey text
    function accountCell(name, number) {
        if (!name && !number) return "-";
        return '<span class="acc-name">' + escapeHtml(name || "-") + '</span>' +
               '<span class="acc-no">' + escapeHtml(number || "") + '</span>';
    }

    // LOAD TRANSACTIONS
    async function loadTransactions() {

        if (!selectedAccountId) return;

        try {
            const transactions = await apiRequest(
                API + "/customer/accounts/" +
                encodeURIComponent(selectedAccountId) + "/transactions"
            );

            const tbody = document.getElementById("transactionTable");

            if (!transactions || transactions.length === 0) {
                tbody.innerHTML =
                    '<tr><td colspan="8">No transactions found.</td></tr>';
                return;
            }

            tbody.innerHTML = transactions.map(transaction => `
                 <tr>
                     <td><span class="txn-ref">\${escapeHtml(transaction.referenceNumber || "-")}</span></td>
                     <td>\${escapeHtml((transaction.transactionType || "-").replace(/_/g, " "))}</td>
                     <td>\${accountCell(transaction.toAccountName, transaction.toAccountNumber)}</td>
                     <td class="amount">₹\${Number(transaction.amount).toLocaleString("en-IN", {
                         minimumFractionDigits: 2
                     })}</td>
                     <td><span class="badge \${escapeHtml(transaction.status || "")}">
                         \${escapeHtml(transaction.status || "-")}
                     </span></td>
                     <td>\${formatDate(transaction.transactionDate)}</td>
                     <td class="amount">₹\${Number(transaction.balanceAfterTransaction).toLocaleString("en-IN", {
                         minimumFractionDigits: 2
                     })}</td>
                     <td class="desc">\${escapeHtml(transaction.description || "-")}</td>
                 </tr>
             `).join("");

        } catch (error) {
            showError("transactionTable", error);
        }
    }


    // LOAD ACCOUNT STATEMENT
    async function loadStatement() {
        const container = document.getElementById("statementDetails");

        if (!selectedAccountId) {
            container.innerHTML =
                '<p class="message">Please select an account first.</p>';
            return;
        }

        try {
            const statement = await apiRequest(
                API + "/customer/accounts/" +
                encodeURIComponent(selectedAccountId) + "/statement"
            );

            container.innerHTML = `
                <div class="info-card">
                    <p><strong>Account Number:</strong>
                        \${escapeHtml(statement.accountNumber)}</p>
                    <p><strong>Account Holder:</strong>
                        \${escapeHtml(statement.accountHolderName)}</p>
                    <p><strong>Branch:</strong>
                        \${escapeHtml(statement.branchName)}</p>
                    <p><strong>District:</strong>
                        \${escapeHtml(statement.district)}</p>
                    <p><strong>Current Balance:</strong>
                        ₹\${Number(statement.currentBalance)
                            .toLocaleString("en-IN")}</p>
                </div>
            `;
        } catch (error) {
            showError("statementDetails", error);
        }
    }

    // VERIFY RECIPIENT
    async function verifyRecipient() {
        const accountNumber =
            document.getElementById("recipientAccountNumber").value.trim();

        const details = document.getElementById("recipientDetails");
        const button = document.getElementById("transferButton");

        verifiedRecipient = null;
        button.disabled = true;
        details.innerHTML = "";

        if (!accountNumber) {
            details.textContent = "Enter recipient account number.";
            return;
        }

        try {
            const data = await apiRequest(
                API + "/accounts/recipient/" +
                encodeURIComponent(accountNumber)
            );

            verifiedRecipient = {
                accountNumber: accountNumber,
                name: data.recipientName
            };

            details.innerHTML = `
                <div class="recipient-box">
                    <strong>Recipient verified</strong>
                    <p>Name: \${escapeHtml(data.recipientName)}</p>
                    <p>Account: \${escapeHtml(accountNumber)}</p>
                </div>
            `;

            button.disabled = false;

        } catch (error) {
            details.innerHTML =
                '<p class="error-message">' +
                escapeHtml(error.message) + '</p>';
        }
    }

    // INVALIDATE VERIFICATION WHEN RECIPIENT NUMBER CHANGES
    document.getElementById("recipientAccountNumber")
        .addEventListener("input", function() {
            verifiedRecipient = null;
            document.getElementById("recipientDetails").innerHTML = "";
            document.getElementById("transferButton").disabled = true;
        });

    // TRANSFER MONEY
    async function transferMoney() {
        if (!verifiedRecipient) {
            alert("Please verify the recipient first.");
            return;
        }

        const senderId =
            document.getElementById("senderAccount").value;

        const amount =
            Number(document.getElementById("transferAmount").value);

        if (!senderId) {
            alert("Please select your sender account.");
            return;
        }

        if (!Number.isFinite(amount) || amount <= 0) {
            alert("Enter a valid transfer amount.");
            return;
        }

        const confirmed = confirm(
            "Transfer ₹" + amount.toFixed(2) +
            " to " + verifiedRecipient.name + "?"
        );

        if (!confirmed) return;

        const button = document.getElementById("transferButton");
        const message = document.getElementById("transferMessage");

        button.disabled = true;
        message.textContent = "Processing transfer...";

        try {
            await apiRequest(
                API + "/accounts/" +
                encodeURIComponent(senderId) + "/transfer",
                {
                    method: "PUT",
                    body: JSON.stringify({
                        recipientAccountNumber:
                            verifiedRecipient.accountNumber,
                        amount: amount
                    })
                }
            );

            message.className = "message success-message";
            message.textContent = "Money transferred successfully!";

            verifiedRecipient = null;
            document.getElementById("recipientDetails").innerHTML = "";
            document.getElementById("recipientAccountNumber").value = "";
            document.getElementById("transferAmount").value = "";
            button.disabled = true;

            // Refresh account cards and dropdown
            await loadAccounts();

            // Refresh selected sender account balance and transactions
            await selectAccount(senderId);

            // Refresh statement if one is displayed
            document.getElementById("statementDetails").innerHTML = "";

        } catch (error) {
            message.className = "message error-message";
            message.textContent = "Transfer failed: " + error.message;
            button.disabled = false;
        }
    }

    // CREATE SUPPORT TICKET
    document.getElementById("ticketForm").addEventListener(
        "submit",
        async function(event) {
            event.preventDefault();

            const ticket = {
                category: document.getElementById("category").value,
                subject: document.getElementById("subject").value.trim(),
                description: document.getElementById("description").value.trim(),
                priority: document.getElementById("priority").value
            };

            const message = document.getElementById("ticketMessage");

            try {
                const result = await apiRequest(
                    API + "/customer/tickets",
                    {
                        method: "POST",
                        body: JSON.stringify(ticket)
                    }
                );

                message.textContent =
                    "Ticket submitted successfully. Ticket ID: " +
                    result.ticketId;

                this.reset();
                await loadTickets();

            } catch (error) {
                message.textContent =
                    "Unable to submit ticket: " + error.message;
            }
        }
    );

    // LOAD CUSTOMER TICKETS
    async function loadTickets() {
        try {
            const tickets = await apiRequest(API + "/customer/tickets");
            const tbody = document.getElementById("ticketTable");

            if (!tickets || tickets.length === 0) {
                tbody.innerHTML =
                    '<tr><td colspan="7">No support tickets found.</td></tr>';
                return;
            }

            tbody.innerHTML = tickets.map(ticket => `
                <tr>
                    <td>\${escapeHtml(ticket.ticketId)}</td>
                    <td>\${escapeHtml(ticket.category)}</td>
                    <td>\${escapeHtml(ticket.subject)}</td>
                    <td>\${escapeHtml(ticket.priority)}</td>
                    <td>\${escapeHtml(ticket.status)}</td>
                    <td>\${escapeHtml(ticket.resolutionRemarks || "Not provided yet")}</td>
                    <td>\${formatDate(ticket.createdAt)}</td>
                </tr>
            `).join("");

        } catch (error) {
            showError("ticketTable", error);
        }
    }

    // INITIAL LOAD
    loadProfile();
    loadAccounts();
    loadTickets();

</script>

</body>
</html>
