<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MyBank - Account Management</title>

    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: Arial, sans-serif;
        }

        body {
            min-height: 100vh;
            background: #f3f6fb;
            color: #17243b;
        }

        header {
            background: #102747;
            color: white;
            padding: 22px 8%;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        header span {
            font-size: 13px;
            color: #cbd5e1;
        }

        nav {
            background: white;
            padding: 15px 8%;
            display: flex;
            gap: 15px;
            align-items: center;
            border-bottom: 1px solid #e2e8f0;
            flex-wrap: wrap;
        }

        nav button {
            background: transparent;
            color: #334155;
            padding: 12px 16px;
            border-radius: 7px;
        }

        nav button:hover,
        nav button.active {
            background: #eaf1ff;
            color: #1d4ed8;
        }

        .container {
            max-width: 1100px;
            margin: 50px auto;
            padding: 0 20px;
        }

        .page-section { display: none; }
        .page-section.active { display: block; }

        .intro { margin-bottom: 28px; }
        .intro h1 { font-size: 30px; margin-bottom: 10px; }
        .intro p { color: #64748b; font-size: 15px; }

        .card {
            background: white;
            padding: 30px;
            border-radius: 14px;
            box-shadow: 0 5px 25px rgba(0,0,0,0.06);
            margin-bottom: 25px;
        }

        .card h3 {
            margin-bottom: 20px;
            font-size: 19px;
        }

        .form-group { margin-bottom: 18px; }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-size: 14px;
            font-weight: bold;
        }

        input {
            width: 100%;
            padding: 14px;
            border: 1px solid #dbe2ea;
            border-radius: 8px;
            font-size: 15px;
            outline: none;
        }

        input:focus { border-color: #2563eb; }

        .search-box {
            display: flex;
            gap: 12px;
        }

        button {
            padding: 14px 23px;
            background: #1d4ed8;
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: bold;
            cursor: pointer;
        }

        button:hover { background: #1e40af; }

        button:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }

        .detail-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 18px;
        }

        .detail-item {
            background: #f5f8fc;
            padding: 20px;
            border-radius: 10px;
        }

        .detail-item p {
            color: #64748b;
            font-size: 13px;
            margin-bottom: 9px;
        }

        .detail-item h2 {
            font-size: 21px;
            overflow-wrap: anywhere;
        }

        .balance { color: #15803d; }

        .status {
            margin-top: 20px;
            color: #15803d;
            font-size: 13px;
        }

        .error {
            color: #dc2626;
            margin-top: 15px;
            font-size: 14px;
        }

        .success {
            color: #15803d;
            margin-top: 15px;
            font-size: 14px;
        }

        .table-container { overflow-x: auto; }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            padding: 14px;
            text-align: left;
            border-bottom: 1px solid #e2e8f0;
            font-size: 14px;
            white-space: nowrap;
        }

        th {
            background: #f1f5f9;
            color: #334155;
        }

        .action-button {
            padding: 9px 13px;
            font-size: 12px;
            margin-bottom: 4px;
        }

        .deactivate-button { background: #dc2626; }
        .deactivate-button:hover { background: #b91c1c; }

        .activate-button { background: #15803d; }
        .activate-button:hover { background: #166534; }

        .active-status {
            color: #15803d;
            font-weight: bold;
        }

        .inactive-status {
            color: #dc2626;
            font-weight: bold;
        }

        .deposit-type {
            color: #15803d;
            font-weight: bold;
        }

        .withdrawal-type {
            color: #dc2626;
            font-weight: bold;
        }

        footer {
            text-align: center;
            color: #94a3b8;
            font-size: 13px;
            padding: 25px;
        }

        .header-right {
            display: flex;
            align-items: center;
            gap: 20px;
        }

        .logout-button {
            background: #dc2626;
            color: white;
            padding: 10px 18px;
            border-radius: 7px;
            text-decoration: none;
            font-size: 14px;
            font-weight: bold;
        }

        .logout-button:hover { background: #b91c1c; }

        .bank-logo {
            height: 45px;
            width: auto;
            object-fit: contain;
            display: block;
        }

        @media (max-width: 600px) {
            .search-box { flex-direction: column; }
            .detail-grid { grid-template-columns: 1fr; }
            nav { gap: 8px; padding: 15px 5%; }
            nav button { padding: 10px; font-size: 12px; }
        }
    </style>
</head>

<body>

<header>
    <img src="/images/banklogo.png"
         alt="MyBank Logo"
         class="bank-logo">

    <div class="header-right">
        <span>ACCOUNT MANAGEMENT</span>
        <a href="${pageContext.request.contextPath}/logout"
           class="logout-button">Logout</a>
    </div>
</header>

<nav>
    <button type="button" class="active" onclick="showSection('home', this)">
        Home
    </button>

    <button type="button" onclick="showSection('create', this)">
        Create Account
    </button>

    <button type="button" onclick="showSection('search', this)">
        Search Account
    </button>

    <button type="button" onclick="showSection('all', this)">
        All Accounts
    </button>
</nav>

<div class="container">

    <!-- HOME -->
    <div id="home" class="page-section active">

        <div class="intro">
            <h1>Welcome to MyBank</h1>
            <p>Manage your bank accounts easily and securely.</p>
        </div>

        <div class="card">
            <h3>Account Management Dashboard</h3>
            <p>
                Use the navigation menu to create new accounts,
                search existing accounts and view all account details.
            </p>
        </div>

    </div>

    <!-- CREATE ACCOUNT -->
    <div id="create" class="page-section">

        <div class="card">
            <h3>Create New Account</h3>

            <div class="form-group">
                <label>Account Holder Name</label>
                <input type="text"
                       id="newAccountName"
                       placeholder="Enter account holder name">
            </div>

            <div class="form-group">
                <label>Initial Balance</label>
                <input type="number"
                       id="newAccountBalance"
                       min="0"
                       step="any"
                       placeholder="Enter initial balance">
            </div>

            <div class="form-group">
                <label>Branch Name</label>
                <input type="text"
                       id="newBranchName"
                       placeholder="Enter branch name">
            </div>

            <div class="form-group">
                <label>District</label>
                <input type="text"
                       id="newDistrict"
                       placeholder="Enter district">
            </div>

            <button id="addButton" onclick="addAccount()">
                Create Account
            </button>

            <p id="addMessage"></p>
        </div>

    </div>

    <!-- SEARCH ACCOUNT -->
    <div id="search" class="page-section">

        <div class="card">
            <h3>Find an Account</h3>

            <div class="search-box">
                <input type="text"
                       id="accountNumber"
                       placeholder="Enter account number">

                <button onclick="getAccount()">
                    Search Account
                </button>
            </div>

            <div id="message"></div>
        </div>

        <div class="card" id="result" style="display:none;">

            <h3>Account Details</h3>

            <div class="detail-grid">

                <div class="detail-item">
                    <p>Account Number</p>
                    <h2 id="displayId">-</h2>
                </div>

                <div class="detail-item">
                    <p>Account Holder</p>
                    <h2 id="accountName">-</h2>
                </div>

                <div class="detail-item">
                    <p>Available Balance</p>
                    <h2 class="balance" id="accountBalance">-</h2>
                </div>

                <div class="detail-item">
                    <p>Branch Name</p>
                    <h2 id="accountBranch">-</h2>
                </div>

                <div class="detail-item">
                    <p>District</p>
                    <h2 id="accountDistrict">-</h2>
                </div>

            </div>

            <div class="status">
                ● Account details retrieved successfully
            </div>

        </div>

    </div>

    <!-- ALL ACCOUNTS -->
    <div id="all" class="page-section">

        <div class="card">
            <h3>All Account Details</h3>

            <div class="table-container">
                <table>
                    <thead>
                        <tr>

                            <th>Account Number</th>
                            <th>Account Holder</th>
                            <th>Balance</th>
                            <th>Branch</th>
                            <th>District</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>

                    <tbody id="accountsTable"></tbody>
                </table>
            </div>

            <p id="allAccountsMessage"></p>
        </div>

        <!-- EDIT ACCOUNT -->
        <div class="card" id="editAccountCard" style="display:none;">

            <h3>Edit Account</h3>

            <input type="hidden" id="editAccountId">

            <div class="form-group">
                <label>Account Holder Name</label>
                <input type="text"
                       id="editAccountHolderName"
                       placeholder="Enter account holder name">
            </div>

            <div class="form-group">
                <label>Branch Name</label>
                <input type="text"
                       id="editBranchName"
                       placeholder="Enter branch name">
            </div>

            <div class="form-group">
                <label>District</label>
                <input type="text"
                       id="editDistrict"
                       placeholder="Enter district">
            </div>

            <button type="button" onclick="saveAccountChanges()">
                Save Changes
            </button>

            <button type="button"
                    onclick="cancelAccountEdit()"
                    style="background:#64748b;">
                Cancel
            </button>

            <p id="editAccountMessage"></p>
        </div>

        <!-- TRANSACTION HISTORY -->
        <div class="card" id="transactionCard" style="display:none;">

            <h3>Transaction History</h3>

            <p id="transactionAccountNumber"
               style="margin-bottom:15px;color:#64748b;">
            </p>

            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Transaction ID</th>
                            <th>Transaction Date</th>
                            <th>Transaction Type</th>
                            <th>Amount</th>
                            <th>Balance After Transaction</th>
                        </tr>
                    </thead>

                    <tbody id="transactionsTable"></tbody>
                </table>
            </div>

            <p id="transactionMessage"></p>

        </div>

    </div>

</div>

<footer>
    MyBank Account Management System
</footer>

<script>

// SWITCH BETWEEN MENU SECTIONS

function showSection(sectionName, clickedButton) {

    const sections = document.querySelectorAll(".page-section");

    sections.forEach(section => {
        section.classList.remove("active");
    });

    document.getElementById(sectionName).classList.add("active");

    const buttons = document.querySelectorAll("nav button");

    buttons.forEach(button => {
        button.classList.remove("active");
    });

    clickedButton.classList.add("active");

    if (sectionName === "all") {
        getAllAccounts();
    }
}


// CREATE ACCOUNT

function addAccount() {

    const name = document.getElementById("newAccountName").value.trim();
    const balance = document.getElementById("newAccountBalance").value;
    const branchName = document.getElementById("newBranchName").value.trim();
    const district = document.getElementById("newDistrict").value.trim();

    const message = document.getElementById("addMessage");
    const button = document.getElementById("addButton");

    message.className = "";

    if (!name || balance === "" || !branchName || !district) {
        message.innerText = "Please fill all fields.";
        message.className = "error";
        return;
    }

    if (!Number.isFinite(Number(balance)) || Number(balance) <= 0) {
        message.innerText = "Please enter a valid initial balance.";
        message.className = "error";
        return;
    }

    button.disabled = true;
    message.innerText = "Creating account...";

    fetch("/api/accounts", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            accountHolderName: name,
            balance: Number(balance),
            branchName: branchName,
            district: district
        })
    })
    .then(async response => {

        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
            throw new Error(data.message || "Unable to create account.");
        }

        return data;
    })
    .then(() => {

        message.innerText = "Account created successfully!";
        message.className = "success";

        document.getElementById("newAccountName").value = "";
        document.getElementById("newAccountBalance").value = "";
        document.getElementById("newBranchName").value = "";
        document.getElementById("newDistrict").value = "";

    })
    .catch(error => {

        message.innerText = error.message;
        message.className = "error";

    })
    .finally(() => {

        button.disabled = false;

    });

}


// SEARCH ACCOUNT

function getAccount() {

    const accountNumber = document.getElementById("accountNumber").value.trim();
    const result = document.getElementById("result");
    const message = document.getElementById("message");

    result.style.display = "none";
    message.className = "";

    if (!accountNumber) {
        message.innerText = "Please enter a valid account number.";
        message.className = "error";
        return;
    }

    message.innerText = "Fetching account details...";

    fetch("/api/accounts/number/" + encodeURIComponent(accountNumber))
    .then(response => {

        if (!response.ok) {
            throw new Error("Account not found.");
        }

        return response.json();

    })
    .then(data => {

        document.getElementById("displayId").textContent =
            data.accountNumber;

        document.getElementById("accountName").textContent =
            data.accountHolderName;

        document.getElementById("accountBalance").textContent =
            "\u20B9 " + Number(data.balance).toLocaleString("en-IN");

        document.getElementById("accountBranch").textContent =
            data.branchName || "-";

        document.getElementById("accountDistrict").textContent =
            data.district || "-";

        result.style.display = "block";
        message.innerText = "";

    })
    .catch(error => {

        message.innerText = error.message;
        message.className = "error";

    });

}


// GET ALL ACCOUNTS

function getAllAccounts() {

    const table = document.getElementById("accountsTable");
    const message = document.getElementById("allAccountsMessage");

    table.innerHTML = "";
    message.className = "";
    message.innerText = "Loading accounts...";

    fetch("/api/accounts")
    .then(response => {

        if (!response.ok) {
            throw new Error("Unable to fetch accounts.");
        }

        return response.json();

    })
    .then(accounts => {

        message.innerText = "";

        if (!Array.isArray(accounts) || accounts.length === 0) {
            message.innerText = "No accounts found.";
            return;
        }

        accounts.forEach(account => {

            const row = document.createElement("tr");



            const numberCell = document.createElement("td");
            numberCell.textContent = account.accountNumber ?? "";

            const nameCell = document.createElement("td");
            nameCell.textContent = account.accountHolderName ?? "";

            const balanceCell = document.createElement("td");
            balanceCell.textContent =
                "\u20B9 " + Number(account.balance).toLocaleString("en-IN");

            const branchCell = document.createElement("td");
            branchCell.textContent = account.branchName ?? "-";

            const districtCell = document.createElement("td");
            districtCell.textContent = account.district ?? "-";

            const statusCell = document.createElement("td");
            statusCell.textContent = account.active ? "Active" : "Inactive";
            statusCell.className =
                account.active ? "active-status" : "inactive-status";

            const actionCell = document.createElement("td");
            const actionButton = document.createElement("button");

            actionButton.classList.add("action-button");

            if (account.active) {

                const editButton = document.createElement("button");

                editButton.textContent = "Edit";
                editButton.classList.add("action-button");
                editButton.style.marginRight = "8px";

                editButton.onclick = () => {
                    editAccount(account);
                };

                actionCell.appendChild(editButton);

                actionButton.textContent = "Deactivate";
                actionButton.classList.add("deactivate-button");

                actionButton.onclick = () => {
                    changeAccountStatus(account.id, "deactivate");
                };

            } else {

                actionButton.textContent = "Activate";
                actionButton.classList.add("activate-button");

                actionButton.onclick = () => {
                    changeAccountStatus(account.id, "activate");
                };

            }

            actionCell.appendChild(actionButton);

            const historyButton = document.createElement("button");

            historyButton.textContent = "History";
            historyButton.classList.add("action-button");
            historyButton.style.background = "#475569";
            historyButton.style.marginLeft = "8px";

            historyButton.onclick = () => {
                getTransactionHistory(account.id, account.accountNumber);
            };

            actionCell.appendChild(historyButton);

            row.appendChild(numberCell);
            row.appendChild(nameCell);
            row.appendChild(balanceCell);
            row.appendChild(branchCell);
            row.appendChild(districtCell);
            row.appendChild(statusCell);
            row.appendChild(actionCell);

            table.appendChild(row);

        });

    })
    .catch(error => {

        message.innerText = error.message;
        message.className = "error";

    });

}


// EDIT ACCOUNT

function editAccount(account) {

    document.getElementById("editAccountId").value = account.id;

    document.getElementById("editAccountHolderName").value =
        account.accountHolderName || "";

    document.getElementById("editBranchName").value =
        account.branchName || "";

    document.getElementById("editDistrict").value =
        account.district || "";

    const message = document.getElementById("editAccountMessage");

    message.innerText = "";
    message.className = "";

    const editCard = document.getElementById("editAccountCard");

    editCard.style.display = "block";

    editCard.scrollIntoView({
        behavior: "smooth"
    });
}


function saveAccountChanges() {

    const id = document.getElementById("editAccountId").value;

    const name = document.getElementById("editAccountHolderName")
        .value.trim();

    const branchName = document.getElementById("editBranchName")
        .value.trim();

    const district = document.getElementById("editDistrict")
        .value.trim();

    const message = document.getElementById("editAccountMessage");

    if (!name || !branchName || !district) {
        message.innerText = "Please fill all fields.";
        message.className = "error";
        return;
    }

    fetch("/api/accounts/" + encodeURIComponent(id) + "/edit", {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            accountHolderName: name,
            branchName: branchName,
            district: district
        })
    })
    .then(async response => {

        const data = await response.json().catch(() => ({}));

        if (response.status === 401) {
            expireSession();
            throw new Error("Session expired.");
        }

        if (!response.ok) {
            throw new Error(data.message || "Unable to update account.");
        }

        return data;
    })
    .then(() => {

        message.innerText = "Account updated successfully!";
        message.className = "success";

        getAllAccounts();

        setTimeout(() => {
            document.getElementById("editAccountCard").style.display = "none";
        }, 1000);

    })
    .catch(error => {

        message.innerText = error.message;
        message.className = "error";

    });

}


function cancelAccountEdit() {

    document.getElementById("editAccountCard").style.display = "none";

    document.getElementById("editAccountHolderName").value = "";
    document.getElementById("editBranchName").value = "";
    document.getElementById("editDistrict").value = "";

    const message = document.getElementById("editAccountMessage");

    message.innerText = "";
    message.className = "";
}


// TRANSACTION HISTORY

function getTransactionHistory(accountId, accountNumber) {

    const card = document.getElementById("transactionCard");
    const table = document.getElementById("transactionsTable");
    const message = document.getElementById("transactionMessage");

    table.innerHTML = "";

    message.innerText = "Loading transactions...";
    message.className = "";

    document.getElementById("transactionAccountNumber").innerText =
        "Account Number: " + accountNumber;

    card.style.display = "block";

    card.scrollIntoView({
        behavior: "smooth"
    });

    fetch("/api/accounts/" + encodeURIComponent(accountId) + "/transactions")
    .then(async response => {

        const data = await response.json().catch(() => []);

        if (response.status === 401) {
            expireSession();
            throw new Error("Session expired. Please log in again.");
        }

        if (!response.ok) {
            throw new Error(data.message || "Unable to fetch transactions.");
        }

        return data;
    })
    .then(transactions => {

        table.innerHTML = "";
        message.className = "";

        if (!Array.isArray(transactions) || transactions.length === 0) {
            message.innerText = "No transactions found for this account.";
            return;
        }

        transactions.forEach(transaction => {

            const row = document.createElement("tr");

            const idCell = document.createElement("td");
            idCell.textContent = transaction.id ?? "";

            const dateCell = document.createElement("td");

            if (transaction.transactionDate) {
                const date = new Date(transaction.transactionDate);

                dateCell.textContent = Number.isNaN(date.getTime())
                    ? transaction.transactionDate
                    : date.toLocaleString("en-IN");
            } else {
                dateCell.textContent = "-";
            }

            const typeCell = document.createElement("td");
            const type = transaction.transactionType ?? "";

            typeCell.textContent = type;
            typeCell.className =
                type.toUpperCase() === "DEPOSIT"
                    ? "deposit-type"
                    : "withdrawal-type";

            const amountCell = document.createElement("td");

            amountCell.textContent =
                "\u20B9 " + Number(transaction.amount).toLocaleString("en-IN");

            const balanceCell = document.createElement("td");

            balanceCell.textContent =
                "\u20B9 " +
                Number(transaction.balanceAfterTransaction)
                    .toLocaleString("en-IN");

            row.appendChild(idCell);
            row.appendChild(dateCell);
            row.appendChild(typeCell);
            row.appendChild(amountCell);
            row.appendChild(balanceCell);

            table.appendChild(row);

        });

        message.innerText = transactions.length + " transaction(s) found.";

    })
    .catch(error => {

        message.innerText = error.message;
        message.className = "error";

    });

}


// ACTIVATE / DEACTIVATE ACCOUNT

function changeAccountStatus(id, action) {

    const url = "/api/accounts/" + String(id) + "/" + action;

    if (!confirm("Are you sure you want to " + action + " this account?")) {
        return;
    }

    fetch(url, {
        method: "PUT"
    })
    .then(async response => {

        const result = await response.text();

        if (!response.ok) {
            throw new Error(result);
        }

        alert(result);
        getAllAccounts();

    })
    .catch(error => {

        console.error("Request error:", error);
        alert(error.message);

    });

}


// AUTOMATIC SESSION TIMEOUT

const SESSION_TIMEOUT = 35 * 1000;
const HEARTBEAT_INTERVAL = 5 * 1000;

const LAST_ACTIVITY_KEY = "webdisplayLastActivity";
const SESSION_EXPIRED_KEY = "webdisplaySessionExpired";

let lastHeartbeat = 0;
let isRedirecting = false;


// REDIRECT WHEN SESSION EXPIRES

function expireSession() {

    if (isRedirecting) {
        return;
    }

    isRedirecting = true;

    localStorage.setItem(SESSION_EXPIRED_KEY, Date.now());

    alert("Your session has expired. Please log in again.");

    window.location.href = "/login?expired=true";
}


// CHECK SERVER SESSION

function checkServerSession() {

    return fetch("/api/session/heartbeat", {
        method: "POST"
    })
    .then(async response => {

        if (response.status === 401) {
            expireSession();
            return false;
        }

        return response.ok;

    })
    .catch(() => false);
}


// RECORD USER ACTIVITY

function recordActivity() {

    if (isRedirecting) {
        return;
    }

    const now = Date.now();

    localStorage.setItem(LAST_ACTIVITY_KEY, now);

    if (now - lastHeartbeat >= HEARTBEAT_INTERVAL) {

        lastHeartbeat = now;
        checkServerSession();

    }
}


// DETECT ACTIVITY

[
    "click",
    "keydown",
    "mousemove",
    "scroll",
    "touchstart",
    "pointerdown"
].forEach(eventName => {

    document.addEventListener(eventName, recordActivity, {
        passive: true
    });

});


// CHECK INACTIVITY EVERY SECOND

setInterval(() => {

    const lastActivity = Number(
        localStorage.getItem(LAST_ACTIVITY_KEY)
    );

    if (!lastActivity) {
        return;
    }

    if (Date.now() - lastActivity >= SESSION_TIMEOUT) {
        expireSession();
    }

}, 1000);


// SHARE EXPIRY BETWEEN TABS

window.addEventListener("storage", event => {

    if (event.key === SESSION_EXPIRED_KEY && event.newValue) {
        window.location.href = "/login?expired=true";
    }

});


// INITIALIZE ACTIVITY

localStorage.removeItem(SESSION_EXPIRED_KEY);
localStorage.setItem(LAST_ACTIVITY_KEY, Date.now());


// CHECK SESSION WHEN RETURNING TO TAB

document.addEventListener("visibilitychange", () => {

    if (!document.hidden) {

        const lastActivity = Number(
            localStorage.getItem(LAST_ACTIVITY_KEY)
        );

        if (Date.now() - lastActivity >= SESSION_TIMEOUT) {

            checkServerSession().then(active => {

                if (!active) {
                    expireSession();
                }

            });

        }
    }

});

</script>

</body>
</html>