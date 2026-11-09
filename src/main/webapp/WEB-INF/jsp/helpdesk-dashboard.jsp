<%@ page isELIgnored="true" %>
<!DOCTYPE html>
<html>
<head>
    <title>Helpdesk Dashboard</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f6f9;
            color: #263238;
        }

        header {
            background: #183153;
            color: white;
            padding: 20px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        header h1 {
            margin: 0;
            font-size: 23px;
        }

        header a {
            color: white;
            text-decoration: none;
            background: #d9534f;
            padding: 9px 16px;
            border-radius: 5px;
        }

        .container {
            padding: 25px;
            max-width: 1250px;
            margin: auto;
        }

        .section {
            background: white;
            padding: 22px;
            margin-bottom: 22px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
        }

        h2 {
            margin-top: 0;
            color: #183153;
            font-size: 19px;
        }

        input, select, textarea {
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 5px;
            margin: 5px;
            min-width: 180px;
        }

        textarea {
            width: 90%;
            min-height: 75px;
            display: block;
        }

        button {
            padding: 10px 16px;
            border: none;
            background: #183153;
            color: white;
            border-radius: 5px;
            cursor: pointer;
            margin: 5px;
        }

        button:hover {
            background: #294d78;
        }

        .result {
            margin-top: 15px;
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 12px;
        }

        th, td {
            padding: 11px;
            border: 1px solid #ddd;
            text-align: left;
        }

        th {
            background: #eaf0f7;
        }

        .message {
            margin-top: 10px;
            color: #183153;
        }

        .muted {
            color: #666;
        }

        .stats-container {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
            gap: 15px;
            margin: 20px 0;
        }

        .stat-card {
            background: white;
            padding: 20px;
            border-radius: 8px;
            border-left: 5px solid #183153;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
        }

        .stat-card h3 {
            margin: 0 0 10px;
            font-size: 15px;
            color: #666;
        }

        .stat-card p {
            font-size: 27px;
            font-weight: bold;
            margin: 0;
        }
    </style>
</head>

<body>

<header>
    <h1>Helpdesk Customer Care Portal</h1>
    <a href="<%= request.getContextPath() %>/logout">Logout</a>
</header>

<div class="container">

    <h2>Welcome to Helpdesk</h2>
    <p class="muted">
        Search customers, investigate transactions and manage customer support tickets.
    </p>
    <!-- DASHBOARD STATISTICS -->
    <div class="section">
        <h2>Dashboard Statistics</h2>

        <div class="stats-container">
            <div class="stat-card">
                <h3>Total Tickets</h3>
                <p id="totalTickets">0</p>
            </div>

            <div class="stat-card">
                <h3>Open</h3>
                <p id="openTickets">0</p>
            </div>

            <div class="stat-card">
                <h3>In Progress</h3>
                <p id="progressTickets">0</p>
            </div>

            <div class="stat-card">
                <h3>Resolved</h3>
                <p id="resolvedTickets">0</p>
            </div>

            <div class="stat-card">
                <h3>Closed</h3>
                <p id="closedTickets">0</p>
            </div>
        </div>
    </div>

    <!-- CUSTOMER SEARCH -->
    <div class="section">
        <h2>1. Customer Search</h2>

        <input type="text" id="searchName" placeholder="Customer name">
        <input type="text" id="searchPhone" placeholder="Phone number">

        <button onclick="searchCustomers()">Search Customer</button>

        <div id="customerResults" class="result"></div>
    </div>

    <!-- CUSTOMER DETAILS -->
    <div class="section">
        <h2>2. Customer Details</h2>
        <div id="customerDetails" class="result">
            <p class="muted">Select a customer from the search results.</p>
        </div>
    </div>

    <!-- ACCOUNT DETAILS -->
    <div class="section">
        <h2>3. Account Information</h2>
        <div id="accountResults" class="result">
            <p class="muted">Select a customer to view account information.</p>
        </div>
    </div>

    <!-- TRANSACTION INVESTIGATION -->
    <div class="section">
        <h2>4. Transaction Investigation</h2>
        <div id="transactionResults" class="result">
            <p class="muted">Select a customer to investigate transactions.</p>
        </div>
    </div>

    <!-- TICKET FILTERS -->
    <div class="section">
        <h2>Filter Support Tickets</h2>

        <select id="filterStatus">
            <option value="">All Statuses</option>
            <option value="OPEN">Open</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="RESOLVED">Resolved</option>
            <option value="CLOSED">Closed</option>
        </select>

        <select id="filterPriority">
            <option value="">All Priorities</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="URGENT">Urgent</option>
        </select>

        <button onclick="filterTickets()">Apply Filters</button>
        <button onclick="resetTicketFilters()">Reset</button>

        <div id="filteredTickets" class="result"></div>
    </div>

    <!-- ALL TICKETS -->
    <div class="section">
        <h2>5. All Support Tickets</h2>

        <button onclick="loadAllTickets()">Refresh All Tickets</button>

        <div id="allTickets" class="result"></div>
    </div>

    <!-- MY ASSIGNED TICKETS -->
    <div class="section">
        <h2>6. My Assigned Tickets</h2>

        <button onclick="loadMyTickets()">Refresh My Tickets</button>

        <div id="myTickets" class="result"></div>
    </div>

    <!-- TICKET MANAGEMENT -->
    <div class="section">
        <h2>7. Manage Ticket</h2>

        <input type="number" id="ticketId" placeholder="Ticket ID">

        <select id="ticketStatus">
            <option value="OPEN">OPEN</option>
            <option value="IN_PROGRESS">IN_PROGRESS</option>
            <option value="RESOLVED">RESOLVED</option>
            <option value="CLOSED">CLOSED</option>
        </select>

        <textarea id="resolutionRemarks"
                  placeholder="Resolution remarks"></textarea>

        <button onclick="updateTicket()">Update Ticket</button>

        <hr>

        <h3>Assign Ticket</h3>

        <input type="number" id="assignTicketId"
               placeholder="Ticket ID">

        <input type="number" id="employeeId"
               placeholder="Helpdesk Employee ID">

        <button onclick="assignTicket()">Assign Ticket</button>

        <div id="ticketMessage" class="message"></div>
    </div>

    <!-- INVESTIGATION REMARKS -->
    <div class="section">
        <h2>8. Investigation History</h2>

        <input type="number" id="remarkTicketId"
               placeholder="Ticket ID">

        <textarea id="investigationRemark"
                  placeholder="Enter investigation remark"></textarea>

        <button onclick="addRemark()">Add Remark</button>
        <button onclick="loadRemarks()">View History</button>

        <div id="remarkResults" class="result"></div>
    </div>

</div>

<script>

    const api = "<%= request.getContextPath() %>/api";

    // Common API request function
    async function apiRequest(url, options = {}) {

        const response = await fetch(api + url, {
            credentials: "same-origin",
            ...options,
            headers: {
                "Content-Type": "application/json",
                ...(options.headers || {})
            }
        });

        const text = await response.text();

        if (!response.ok) {
            throw new Error(text || "Request failed: " + response.status);
        }

        return text ? JSON.parse(text) : null;
    }

    // Safe HTML display
    function escapeHtml(value) {
        return String(value ?? "").replace(/[&<>"']/g, char => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        })[char]);
    }

    // CUSTOMER SEARCH
    async function searchCustomers() {

        const name = document.getElementById("searchName").value.trim();
        const phone = document.getElementById("searchPhone").value.trim();

        if (!name && !phone) {
            alert("Enter a customer name or phone number.");
            return;
        }

        try {
            const params = new URLSearchParams();

            if (name) params.append("name", name);
            if (phone) params.append("phone", phone);

            const customers = await apiRequest(
                "/helpdesk/customers/search?" + params.toString()
            );

            let html = "";

            if (!customers.length) {
                html = "<p>No customers found.</p>";
            } else {
                html = `
                    <table>
                        <tr>
                            <th>Customer ID</th>
                            <th>Name</th>
                            <th>Phone</th>
                            <th>Action</th>
                        </tr>
                `;

                customers.forEach(customer => {
                    html += `
                        <tr>
                            <td>${escapeHtml(customer.customerId)}</td>
                            <td>${escapeHtml(customer.customerName)}</td>
                            <td>${escapeHtml(customer.phone)}</td>
                            <td>
                                <button onclick="selectCustomer(${Number(customer.customerId)})">
                                    View Details
                                </button>
                            </td>
                        </tr>
                    `;
                });

                html += "</table>";
            }

            document.getElementById("customerResults").innerHTML = html;

        } catch (error) {
            alert("Customer search failed: " + error.message);
        }
    }

    // CUSTOMER DETAILS
    async function selectCustomer(customerId) {

        try {
            const customer = await apiRequest(
                "/helpdesk/customers/" + customerId
            );

            document.getElementById("customerDetails").innerHTML = `
                <p><b>Customer ID:</b> ${escapeHtml(customer.customerId)}</p>
                <p><b>Name:</b> ${escapeHtml(customer.customerName)}</p>
                <p><b>Phone:</b> ${escapeHtml(customer.phone)}</p>
                <p><b>Address:</b> ${escapeHtml(customer.address)}</p>
            `;

            await loadCustomerAccounts(customerId);
            await loadCustomerTransactions(customerId);

        } catch (error) {
            alert("Unable to load customer: " + error.message);
        }
    }

    // ACCOUNT INFORMATION
    async function loadCustomerAccounts(customerId) {

        try {
            const accounts = await apiRequest(
                "/helpdesk/customers/" + customerId + "/accounts"
            );

            let html = "";

            if (!accounts.length) {
                html = "<p>No accounts found.</p>";
            } else {
                html = `
                    <table>
                        <tr>
                            <th>Account Number</th>
                            <th>Account Holder</th>
                            <th>Branch</th>
                            <th>District</th>
                            <th>Status</th>
                        </tr>
                `;

                accounts.forEach(account => {
                    html += `
                        <tr>
                            <td>${escapeHtml(account.accountNumber)}</td>
                            <td>${escapeHtml(account.accountHolderName)}</td>
                            <td>${escapeHtml(account.branchName)}</td>
                            <td>${escapeHtml(account.district)}</td>
                            <td>${account.active ? "Active" : "Inactive"}</td>
                        </tr>
                    `;
                });

                html += "</table>";
            }

            document.getElementById("accountResults").innerHTML = html;

        } catch (error) {
            document.getElementById("accountResults").innerText =
                "Unable to load accounts: " + error.message;
        }
    }

    // TRANSACTION INVESTIGATION
    async function loadCustomerTransactions(customerId) {

        try {
            const transactions = await apiRequest(
                "/helpdesk/customers/" + customerId + "/transactions"
            );

            let html = "";

            if (!transactions.length) {
                html = "<p>No transactions found.</p>";
            } else {
                html = `
                    <table>
                        <tr>
                            <th>Transaction ID</th>
                            <th>Account</th>
                            <th>Type</th>
                            <th>Amount</th>
                            <th>Date</th>
                            <th>Balance After Transaction</th>
                        </tr>
                `;

                transactions.forEach(transaction => {
                    html += `
                        <tr>
                            <td>${escapeHtml(transaction.transactionId)}</td>
                            <td>${escapeHtml(transaction.accountNumber)}</td>
                            <td>${escapeHtml(transaction.transactionType)}</td>
                            <td>${escapeHtml(transaction.amount)}</td>
                            <td>${escapeHtml(transaction.transactionDate)}</td>
                            <td>${escapeHtml(transaction.balanceAfterTransaction)}</td>
                        </tr>
                    `;
                });

                html += "</table>";
            }

            document.getElementById("transactionResults").innerHTML = html;

        } catch (error) {
            document.getElementById("transactionResults").innerText =
                "Unable to load transactions: " + error.message;
        }
    }

    // ALL TICKETS
    async function loadAllTickets() {

        try {
            const tickets = await apiRequest("/helpdesk/tickets");

            displayTickets(tickets, "allTickets");

        } catch (error) {
            alert("Unable to load tickets: " + error.message);
        }
    }

    // MY ASSIGNED TICKETS
    async function loadMyTickets() {

        try {
            const tickets = await apiRequest("/helpdesk/my-tickets");

            displayTickets(tickets, "myTickets");

        } catch (error) {
            alert("Unable to load assigned tickets: " + error.message);
        }
    }

    // DISPLAY TICKETS
    function displayTickets(tickets, elementId) {

        let html = "";

        if (!tickets.length) {
            html = "<p>No tickets found.</p>";
        } else {
            html = `
                <table>
                    <tr>
                        <th>Ticket ID</th>
                        <th>Customer ID</th>
                        <th>Category</th>
                        <th>Subject</th>
                        <th>Priority</th>
                        <th>Status</th>
                    </tr>
            `;

            tickets.forEach(ticket => {
                html += `
                    <tr>
                        <td>${escapeHtml(ticket.ticketId)}</td>
                        <td>${escapeHtml(ticket.customerId)}</td>
                        <td>${escapeHtml(ticket.category)}</td>
                        <td>${escapeHtml(ticket.subject)}</td>
                        <td>${escapeHtml(ticket.priority)}</td>
                        <td>${escapeHtml(ticket.status)}</td>
                    </tr>
                `;
            });

            html += "</table>";
        }

        document.getElementById(elementId).innerHTML = html;
    }

    // UPDATE TICKET
    async function updateTicket() {

        const ticketId = document.getElementById("ticketId").value;
        const status = document.getElementById("ticketStatus").value;
        const remarks = document.getElementById("resolutionRemarks").value;

        if (!ticketId) {
            alert("Enter ticket ID.");
            return;
        }

        try {
            const result = await apiRequest(
                "/helpdesk/tickets/" + ticketId,
                {
                    method: "PUT",
                    body: JSON.stringify({ status, remarks })
                }
            );

            document.getElementById("ticketMessage").innerText =
                "Ticket updated successfully.";

            loadAllTickets();
            loadMyTickets();
            loadTicketStatistics();
            filterTickets();

        } catch (error) {
            alert("Ticket update failed: " + error.message);
        }
    }

    // ASSIGN TICKET
    async function assignTicket() {

        const ticketId = document.getElementById("assignTicketId").value;
        const employeeId = document.getElementById("employeeId").value;

        if (!ticketId || !employeeId) {
            alert("Enter both ticket ID and employee ID.");
            return;
        }

        try {
            await apiRequest(
                "/helpdesk/tickets/" + ticketId +
                "/assign/" + employeeId,
                { method: "PUT" }
            );

            document.getElementById("ticketMessage").innerText =
                "Ticket assigned successfully.";

            loadAllTickets();
            loadMyTickets();

        } catch (error) {
            alert("Ticket assignment failed: " + error.message);
        }
    }

    // ADD INVESTIGATION REMARK
    async function addRemark() {

        const ticketId = document.getElementById("remarkTicketId").value;
        const remarks = document.getElementById("investigationRemark").value;

        if (!ticketId || !remarks.trim()) {
            alert("Enter ticket ID and investigation remark.");
            return;
        }

        try {
            await apiRequest(
                "/helpdesk/tickets/" + ticketId + "/remarks",
                {
                    method: "POST",
                    body: JSON.stringify({ remarks })
                }
            );

            document.getElementById("investigationRemark").value = "";

            alert("Investigation remark added successfully.");

            loadRemarks();

        } catch (error) {
            alert("Unable to add remark: " + error.message);
        }
    }

    // VIEW INVESTIGATION HISTORY
    async function loadRemarks() {

        const ticketId = document.getElementById("remarkTicketId").value;

        if (!ticketId) {
            alert("Enter ticket ID.");
            return;
        }

        try {
            const remarks = await apiRequest(
                "/helpdesk/tickets/" + ticketId + "/remarks"
            );

            let html = "";

            if (!remarks.length) {
                html = "<p>No investigation remarks found.</p>";
            } else {
                remarks.forEach(remark => {
                    html += `
                        <div style="border-bottom:1px solid #ddd;padding:12px;">
                            <b>${escapeHtml(remark.employeeName)}</b>
                            <p>${escapeHtml(remark.remarks)}</p>
                            <small>${escapeHtml(remark.createdAt)}</small>
                        </div>
                    `;
                });
            }

            document.getElementById("remarkResults").innerHTML = html;

        } catch (error) {
            alert("Unable to load remarks: " + error.message);
        }
    }

    // Load ticket lists when dashboard opens
    loadAllTickets();
    loadMyTickets();
    loadTicketStatistics();

    // LOAD DASHBOARD STATISTICS
    async function loadTicketStatistics() {

        try {
            const stats = await apiRequest(
                "/helpdesk/tickets/statistics"
            );

            document.getElementById("totalTickets").innerText = stats.total;
            document.getElementById("openTickets").innerText = stats.open;
            document.getElementById("progressTickets").innerText = stats.inProgress;
            document.getElementById("resolvedTickets").innerText = stats.resolved;
            document.getElementById("closedTickets").innerText = stats.closed;

        } catch (error) {
            console.error("Statistics loading failed:", error);
        }
    }

    // FILTER TICKETS
    async function filterTickets() {

        const status = document.getElementById("filterStatus").value;
        const priority = document.getElementById("filterPriority").value;

        const params = new URLSearchParams();

        if (status) params.append("status", status);
        if (priority) params.append("priority", priority);

        try {
            const tickets = await apiRequest(
                "/helpdesk/tickets/filter?" + params.toString()
            );

            displayTickets(tickets, "filteredTickets");

        } catch (error) {
            alert("Filtering failed: " + error.message);
        }
    }

    // RESET FILTERS
    function resetTicketFilters() {

        document.getElementById("filterStatus").value = "";
        document.getElementById("filterPriority").value = "";

        filterTickets();
    }

</script>

</body>
</html>
