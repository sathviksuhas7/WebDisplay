
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Employee Dashboard | WebDisplay</title>
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
            background: #f3f5f9;
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

        header a {
            color: white;
            text-decoration: none;
            background: #d9534f;
            padding: 10px 16px;
            border-radius: 5px;
        }

        .container {
            width: 90%;
            max-width: 1250px;
            margin: 30px auto;
        }

        .welcome {
            margin-bottom: 25px;
        }

        .welcome h1 {
            margin-bottom: 8px;
        }

        .section {
            background: white;
            padding: 22px;
            margin-bottom: 25px;
            border-radius: 9px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        }

        .section h2 {
            color: #172b4d;
            margin-bottom: 18px;
            font-size: 20px;
        }

        .card {
            background: #f7f9fc;
            border: 1px solid #e1e6ed;
            border-radius: 7px;
            padding: 18px;
            margin-top: 15px;
        }

        .card p {
            margin-top: 9px;
            overflow-wrap: anywhere;
        }

        input,
        select,
        textarea {
            width: 100%;
            padding: 11px;
            margin: 8px 0 15px;
            border: 1px solid #ccd3dc;
            border-radius: 5px;
        }

        label {
            font-weight: bold;
            font-size: 14px;
        }

        button {
            background: #1769aa;
            color: white;
            border: none;
            padding: 10px 15px;
            border-radius: 5px;
            cursor: pointer;
            margin: 4px 4px 4px 0;
        }

        button:hover {
            background: #105487;
        }

        .secondary {
            background: #526477;
        }

        .danger {
            background: #c74646;
        }

        .success {
            background: #26834a;
        }

        .message {
            margin: 12px 0;
            color: #1769aa;
        }

        .error {
            color: #c62828;
        }

        .muted {
            color: #68778a;
        }

        .table-container {
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            min-width: 700px;
        }

        th,
        td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #e5eaf0;
        }

        th {
            background: #f1f4f8;
        }

        .form-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 10px;
        }

        /* RESULT DISPLAY */

        .hidden {
            display: none !important;
        }

        .result-message {
            margin-top: 15px;
            padding: 12px;
            border-radius: 5px;
            color: #526477;
            background: #f1f4f8;
        }

        .result-message.error {
            color: #c62828;
            background: #fff1f0;
        }

        @media (max-width: 600px) {
            .container {
                width: 94%;
            }

            .section {
                padding: 16px;
            }

            header {
                padding: 18px 4%;
            }
        }
    </style>
</head>

<body>

<header>
    <h2>WebDisplay | Employee Portal</h2>
    <form action="<%= request.getContextPath() %>/logout"
          method="post"
          style="display:inline;">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit"
                style="background:#dc2626; color:white; padding:10px 18px;
                       border:0; border-radius:7px; cursor:pointer;">
            Logout
        </button>
    </form>
</header>

<div class="container">

    <div class="welcome">

        <p id="welcomeMessage">Loading welcome message...</p>
        <p>Manage customers, accounts, transactions and banking operations.</p>
    </div>

    <!-- CREATE CUSTOMER -->
    <div class="section">
        <h2>Create Customer</h2>

        <form id="customerForm">
            <div class="form-grid">
                <div>
                    <label>Username</label>
                    <input id="username" required>
                </div>

                <div>
                    <label>Email</label>
                    <input id="email" type="email" required>
                </div>

                <div>
                    <label>Password</label>
                    <input id="password" type="password" required>
                </div>

                <div>
                    <label>Customer Name</label>
                    <input id="customerName" required>
                </div>

                <div>
                    <label>Phone</label>
                    <input id="phone" required>
                </div>

                <div>
                    <label>Date of Birth</label>
                    <input id="dateOfBirth" type="date" required>
                </div>
            </div>

            <label>Address</label>
            <textarea id="address" rows="2" required></textarea>

            <button type="submit">Create Customer</button>
        </form>

        <p id="customerMessage" class="message"></p>
    </div>

    <!-- SEARCH CUSTOMERS -->
    <div class="section">
        <h2>Search Customers</h2>

        <div class="form-grid">
            <div>
                <label>Customer Name</label>
                <input id="searchName" placeholder="Enter customer name">
            </div>

            <div>
                <label>Phone Number</label>
                <input id="searchPhone" placeholder="Enter phone number">
            </div>
        </div>

        <button onclick="searchCustomers()">Search</button>
        <button class="secondary" onclick="clearCustomerSearch()">Clear</button>

        <p id="customerSearchMessage" class="result-message hidden"></p>

        <div id="customerResultsContainer" class="table-container hidden">
            <table>
                <thead>
                <tr>
                    <th>Customer ID</th>
                    <th>Name</th>
                    <th>Phone</th>
                    <th>Address</th>
                    <th>Action</th>
                </tr>
                </thead>

                <tbody id="customerResults"></tbody>
            </table>
        </div>
    </div>

    <!-- CREATE ACCOUNT -->
    <div class="section">
        <h2>Create Account</h2>

        <form id="accountForm">
            <label>Customer ID</label>
            <input id="accountCustomerId" type="number" required>

            <div class="form-grid">
                <div>
                    <label>Branch Name</label>
                    <input id="branchName" required>
                </div>

                <div>
                    <label>District</label>
                    <input id="district" required>
                </div>
            </div>

            <button type="submit">Create Account</button>
        </form>

        <p id="accountMessage" class="message"></p>
    </div>

    <!-- ACCOUNT LOOKUP -->
    <div class="section">
        <h2>Account Management</h2>

        <label>Account Number</label>
        <input id="lookupAccountNumber" placeholder="Enter account number">

        <button onclick="searchAccount()">Search Account</button>
        <button class="secondary" onclick="clearAccountSearch()">Clear</button>
        <button class="secondary" onclick="loadAllAccounts()">View All Accounts</button>

        <p id="accountSearchMessage" class="result-message hidden"></p>

        <div id="accountDetails" class="hidden"></div>
    </div>

    <!-- EDIT ACCOUNT -->
    <div class="section">
        <h2>Edit Account Details</h2>

        <label>Account ID</label>
        <input type="text" id="editAccountId" placeholder="Enter account ID">

        <label>Account Holder Name</label>
        <input type="text" id="editHolderName" placeholder="Enter account holder name">

        <label>Branch Name</label>
        <input type="text" id="editBranchName" placeholder="Enter branch name">

        <label>District</label>
        <input type="text" id="editDistrict" placeholder="Enter district">

        <button onclick="editAccount()">Update Account</button>

        <p id="editMessage" class="message"></p>
    </div>

    <!-- DEPOSIT AND WITHDRAWAL -->
    <div class="section">
        <h2>Deposit / Withdrawal</h2>

        <label>Account ID</label>
        <input id="operationAccountId" type="text" placeholder="Enter account ID">

        <label>Amount (₹)</label>
        <input id="operationAmount" type="number" min="0.01" step="0.01"
               placeholder="Enter amount">

        <button class="success" onclick="performTransaction('deposit')">Deposit</button>
        <button class="danger" onclick="performTransaction('withdraw')">Withdraw</button>

        <p id="operationMessage" class="message"></p>
    </div>

    <!-- ALL ACCOUNTS -->
    <div class="section">
        <h2>All Accounts</h2>

        <button onclick="loadAllAccounts()">Refresh Accounts</button>

        <p id="allAccountsMessage" class="result-message hidden"></p>

        <div id="accountTableContainer" class="table-container hidden">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Account Number</th>
                    <th>Holder</th>
                    <th>Balance</th>
                    <th>Branch</th>
                    <th>District</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
                </thead>

                <tbody id="accountTable"></tbody>
            </table>
        </div>
    </div>

    <!-- TRANSACTION HISTORY -->
    <div class="section">
        <h2>Transaction History</h2>

        <label>Account ID</label>
        <input id="historyAccountId" type="text" placeholder="Enter account ID">

        <button onclick="loadTransactions()">View Transactions</button>
        <button class="secondary" onclick="clearTransactionHistory()">Clear</button>

        <p id="transactionMessage" class="result-message hidden"></p>

        <div id="transactionTableContainer" class="table-container hidden">
            <table>
                <thead>
                <tr>
                    <th>Transaction ID</th>
                    <th>Type</th>
                    <th>Amount</th>
                    <th>Date</th>
                    <th>Balance After</th>
                </tr>
                </thead>

                <tbody id="transactionTable"></tbody>
            </table>
        </div>
    </div>

</div>

<script>

    const API = "<%= request.getContextPath() %>/api";

    // COMMON API REQUEST FUNCTION
    async function apiRequest(url, options = {}) {
        const response = await fetch(url, {
            ...options,
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json",
                "X-CSRF-TOKEN": document.querySelector(
                    'meta[name="csrf-token"]'
                ).content,
                ...(options.headers || {})
            }
        });

        const responseText = await response.text();

        if (!response.ok) {
            throw new Error(responseText || "Request failed: " + response.status);
        }

        if (!responseText) {
            return null;
        }

        try {
            return JSON.parse(responseText);
        } catch {
            return responseText;
        }
    }

    // ESCAPE HTML
    function escapeHtml(value) {
        return String(value ?? "").replace(/[&<>"']/g, char => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        })[char]);
    }

    // DISPLAY MESSAGES
    function showMessage(id, message, error = false) {
        const element = document.getElementById(id);

        element.textContent = message;
        element.className = error ? "message error" : "message";
    }

    // SHOW RESULT TABLE OR CARD
    function showResults(containerId) {
        document.getElementById(containerId).classList.remove("hidden");
    }

    // HIDE RESULT TABLE OR CARD
    function hideResults(containerId) {
        document.getElementById(containerId).classList.add("hidden");
    }

    // SHOW RESULT MESSAGE
    function showResultMessage(id, message, error = false) {
        const element = document.getElementById(id);

        element.textContent = message;
        element.className = error
            ? "result-message error"
            : "result-message";

        element.classList.remove("hidden");
    }

    // HIDE RESULT MESSAGE
    function hideResultMessage(id) {
        const element = document.getElementById(id);

        element.textContent = "";
        element.classList.add("hidden");
    }

    // CREATE CUSTOMER
    document.getElementById("customerForm").addEventListener("submit",
        async function(event) {
            event.preventDefault();

            const customer = {
                username: document.getElementById("username").value.trim(),
                email: document.getElementById("email").value.trim(),
                password: document.getElementById("password").value,
                customerName: document.getElementById("customerName").value.trim(),
                phone: document.getElementById("phone").value.trim(),
                address: document.getElementById("address").value.trim(),
                dateOfBirth: document.getElementById("dateOfBirth").value
            };

            try {
                const result = await apiRequest(API + "/customers", {
                    method: "POST",
                    body: JSON.stringify(customer)
                });

                showMessage("customerMessage", result);
                this.reset();

            } catch (error) {
                showMessage("customerMessage", error.message, true);
            }
        }
    );

    // SEARCH CUSTOMERS
    async function searchCustomers() {
        const name = document.getElementById("searchName").value.trim();
        const phone = document.getElementById("searchPhone").value.trim();

        hideResults("customerResultsContainer");
        hideResultMessage("customerSearchMessage");

        if (!name && !phone) {
            showResultMessage(
                "customerSearchMessage",
                "Enter a customer name or phone number.",
                true
            );
            return;
        }

        const params = new URLSearchParams();

        if (name) {
            params.append("name", name);
        }

        if (phone) {
            params.append("phone", phone);
        }

        try {
            const customers = await apiRequest(
                API + "/customers/search?" + params.toString()
            );

            if (!customers || customers.length === 0) {
                showResultMessage(
                    "customerSearchMessage",
                    "No customers found matching your search."
                );
                return;
            }

            const tbody = document.getElementById("customerResults");

            tbody.innerHTML = customers.map(customer => `
                <tr>
                    <td>\${escapeHtml(customer.customerId)}</td>
                    <td>\${escapeHtml(customer.customerName)}</td>
                    <td>\${escapeHtml(customer.phone)}</td>
                    <td>\${escapeHtml(customer.address)}</td>
                    <td>
                        <button onclick="useCustomerId('\${escapeHtml(customer.customerId)}')">
                            Select
                        </button>
                    </td>
                </tr>
            `).join("");

            showResults("customerResultsContainer");

        } catch (error) {
            showResultMessage(
                "customerSearchMessage",
                error.message,
                true
            );
        }
    }

    // SELECT CUSTOMER FOR ACCOUNT CREATION
    function useCustomerId(id) {
        document.getElementById("accountCustomerId").value = id;

        document.getElementById("accountForm")
            .scrollIntoView({ behavior: "smooth" });
    }

    // CLEAR CUSTOMER SEARCH
    function clearCustomerSearch() {
        document.getElementById("searchName").value = "";
        document.getElementById("searchPhone").value = "";

        document.getElementById("customerResults").innerHTML = "";

        hideResults("customerResultsContainer");
        hideResultMessage("customerSearchMessage");
    }

    // CREATE ACCOUNT
    document.getElementById("accountForm").addEventListener("submit",
        async function(event) {
            event.preventDefault();

            const account = {
                customerId: Number(
                    document.getElementById("accountCustomerId").value
                ),
                branchName: document.getElementById("branchName").value.trim(),
                district: document.getElementById("district").value.trim()
            };

            try {
                const result = await apiRequest(API + "/accounts", {
                    method: "POST",
                    body: JSON.stringify(account)
                });

                showMessage(
                    "accountMessage",
                    "Account created successfully. Account number: " +
                    result.accountNumber
                );

                this.reset();
                await loadAllAccounts();

            } catch (error) {
                showMessage("accountMessage", error.message, true);
            }
        }
    );

    // SEARCH ACCOUNT BY ACCOUNT NUMBER
    async function searchAccount() {
        const number = document.getElementById("lookupAccountNumber").value.trim();

        hideResults("accountDetails");
        hideResultMessage("accountSearchMessage");

        if (!number) {
            showResultMessage(
                "accountSearchMessage",
                "Enter an account number.",
                true
            );
            return;
        }

        try {
            const account = await apiRequest(
                API + "/accounts/number/" + encodeURIComponent(number)
            );

            displayAccountDetails(account);

            document.getElementById("editAccountId").value = account.id;
            document.getElementById("editHolderName").value =
                account.accountHolderName || "";
            document.getElementById("editBranchName").value =
                account.branchName || "";
            document.getElementById("editDistrict").value =
                account.district || "";

            selectAccountForOperation(account.id);

        } catch (error) {
            showResultMessage(
                "accountSearchMessage",
                error.message,
                true
            );
        }
    }

    // CLEAR ACCOUNT SEARCH
    function clearAccountSearch() {
        document.getElementById("lookupAccountNumber").value = "";

        document.getElementById("accountDetails").innerHTML = "";

        hideResults("accountDetails");
        hideResultMessage("accountSearchMessage");
    }

    // DISPLAY ACCOUNT DETAILS
    function displayAccountDetails(account) {
        document.getElementById("accountDetails").innerHTML = `
            <div class="card">
                <h3>\${escapeHtml(account.accountNumber)}</h3>
                <p><strong>Account ID:</strong> \${escapeHtml(account.id)}</p>
                <p><strong>Holder:</strong> \${escapeHtml(account.accountHolderName)}</p>
                <p><strong>Balance:</strong> ₹\${Number(account.balance).toLocaleString("en-IN")}</p>
                <p><strong>Branch:</strong> \${escapeHtml(account.branchName)}</p>
                <p><strong>District:</strong> \${escapeHtml(account.district)}</p>
                <p><strong>Status:</strong> \${account.active ? "Active" : "Inactive"}</p>
            </div>
        `;

        showResults("accountDetails");
    }

    // LOAD ALL ACCOUNTS
    async function loadAllAccounts() {
        hideResults("accountTableContainer");
        hideResultMessage("allAccountsMessage");

        try {
            const accounts = await apiRequest(API + "/accounts");

            if (!accounts || accounts.length === 0) {
                showResultMessage(
                    "allAccountsMessage",
                    "No accounts found."
                );
                return;
            }

            const tbody = document.getElementById("accountTable");

            tbody.innerHTML = accounts.map(account => `
                <tr>
                    <td>\${escapeHtml(account.id)}</td>
                    <td>\${escapeHtml(account.accountNumber)}</td>
                    <td>\${escapeHtml(account.accountHolderName)}</td>
                    <td>₹\${Number(account.balance).toLocaleString("en-IN")}</td>
                    <td>\${escapeHtml(account.branchName)}</td>
                    <td>\${escapeHtml(account.district)}</td>
                    <td>\${account.active ? "Active" : "Inactive"}</td>
                    <td>
                        <button onclick="toggleAccount('\${escapeHtml(account.id)}', \${!account.active})">
                            \${account.active ? "Deactivate" : "Activate"}
                        </button>

                        <button class="secondary"
                            onclick="selectAccount('\${escapeHtml(account.id)}')">
                            Select
                        </button>
                    </td>
                </tr>
            `).join("");

            showResults("accountTableContainer");

        } catch (error) {
            showResultMessage(
                "allAccountsMessage",
                error.message,
                true
            );
        }
    }

    // SELECT ACCOUNT
    function selectAccount(id) {
        selectAccountForOperation(id);

        document.getElementById("editAccountId").value = id;

        document.getElementById("operationAccountId")
            .scrollIntoView({ behavior: "smooth" });
    }

    // FILL ACCOUNT ID IN BANKING AND HISTORY SECTIONS
    function selectAccountForOperation(id) {
        const accountId = String(id);

        document.getElementById("operationAccountId").value = accountId;
        document.getElementById("historyAccountId").value = accountId;
    }

    // EDIT ACCOUNT
    async function editAccount() {
        const id = document.getElementById("editAccountId").value.trim();

        const accountHolderName =
            document.getElementById("editHolderName").value.trim();

        const branchName =
            document.getElementById("editBranchName").value.trim();

        const district =
            document.getElementById("editDistrict").value.trim();

        if (!id || !accountHolderName || !branchName || !district) {
            showMessage("editMessage", "Please fill all fields.", true);
            return;
        }

        try {
            await apiRequest(
                API + "/accounts/" + encodeURIComponent(id) + "/edit",
                {
                    method: "PUT",
                    body: JSON.stringify({
                        accountHolderName: accountHolderName,
                        branchName: branchName,
                        district: district
                    })
                }
            );

            showMessage(
                "editMessage",
                "Account details updated successfully."
            );

            await loadAllAccounts();
            await searchAccountAfterEdit(id);

        } catch (error) {
            showMessage("editMessage", error.message, true);
        }
    }

    // REFRESH ACCOUNT DETAILS AFTER EDIT
    async function searchAccountAfterEdit(id) {
        try {
            const account = await apiRequest(
                API + "/accounts/" + encodeURIComponent(id)
            );

            displayAccountDetails(account);

        } catch (error) {
            showResultMessage(
                "accountSearchMessage",
                error.message,
                true
            );
        }
    }

    // DEPOSIT OR WITHDRAW
    async function performTransaction(type) {
        const id = document.getElementById("operationAccountId").value.trim();

        const amount = Number(
            document.getElementById("operationAmount").value
        );

        if (!id || !Number.isFinite(amount) || amount <= 0) {
            showMessage(
                "operationMessage",
                "Enter a valid account ID and positive amount.",
                true
            );
            return;
        }

        if (!confirm("Confirm " + type + " of ₹" + amount + "?")) {
            return;
        }

        try {
            const result = await apiRequest(
                API + "/accounts/" + encodeURIComponent(id) + "/" + type,
                {
                    method: "PUT",
                    body: JSON.stringify({ amount: amount })
                }
            );

            showMessage(
                "operationMessage",
                type.toUpperCase() + " successful. New balance: ₹" +
                Number(result.balance).toLocaleString("en-IN")
            );

            document.getElementById("operationAmount").value = "";

            await loadAllAccounts();
            await searchAccountAfterEdit(id);
            await loadTransactions();

        } catch (error) {
            showMessage("operationMessage", error.message, true);
        }
    }

    // ACTIVATE OR DEACTIVATE ACCOUNT
    async function toggleAccount(id, activate) {
        const action = activate ? "activate" : "deactivate";

        if (!confirm("Are you sure you want to " + action + " this account?")) {
            return;
        }

        try {
            const result = await apiRequest(
                API + "/accounts/" + encodeURIComponent(id) + "/" + action,
                {
                    method: "PUT"
                }
            );

            await loadAllAccounts();

            showResultMessage(
                "allAccountsMessage",
                result || "Account " + action + "d successfully."
            );

        } catch (error) {
            showResultMessage(
                "allAccountsMessage",
                error.message,
                true
            );
        }
    }

    // LOAD TRANSACTION HISTORY
    async function loadTransactions() {
        const id = document.getElementById("historyAccountId").value.trim();

        hideResults("transactionTableContainer");
        hideResultMessage("transactionMessage");

        if (!id) {
            showResultMessage(
                "transactionMessage",
                "Enter an account ID.",
                true
            );
            return;
        }

        try {
            const transactions = await apiRequest(
                API + "/accounts/" + encodeURIComponent(id) + "/transactions"
            );

            if (!transactions || transactions.length === 0) {
                showResultMessage(
                    "transactionMessage",
                    "No transactions found for this account."
                );
                return;
            }

            const tbody = document.getElementById("transactionTable");

            tbody.innerHTML = transactions.map(transaction => `
                <tr>
                    <td>\${escapeHtml(transaction.transactionId)}</td>
                    <td>\${escapeHtml(transaction.transactionType)}</td>
                    <td>₹\${Number(transaction.amount).toLocaleString("en-IN")}</td>
                    <td>\${escapeHtml(transaction.transactionDate)}</td>
                    <td>₹\${Number(transaction.balanceAfterTransaction).toLocaleString("en-IN")}</td>
                </tr>
            `).join("");

            showResults("transactionTableContainer");

        } catch (error) {
            showResultMessage(
                "transactionMessage",
                error.message,
                true
            );
        }
    }

    // CLEAR TRANSACTION HISTORY
    function clearTransactionHistory() {
        document.getElementById("historyAccountId").value = "";
        document.getElementById("transactionTable").innerHTML = "";

        hideResults("transactionTableContainer");
        hideResultMessage("transactionMessage");
    }

    async function loadLoggedInEmployeeName() {
        try {
            const employee = await apiRequest(API + "/accounts/me");

            if (!employee || !employee.employeeName) {
                throw new Error("Employee name not found in response.");
            }

            document.getElementById("welcomeMessage").innerText =
                "Hi " + employee.employeeName +
                ", welcome to the Employee Dashboard!";

        } catch (error) {
            document.getElementById("welcomeMessage").innerText =
                "Unable to load employee name.";

            console.error("Employee name loading failed:", error);
        }
    }

    loadLoggedInEmployeeName();

</script>

</body>
</html>

