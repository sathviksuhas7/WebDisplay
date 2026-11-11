<!DOCTYPE html>
<html>
<head>
    <title>Helpdesk Dashboard</title>
    <meta name="csrf-token" content="${sessionScope.csrfToken}">

    <style>
        * { box-sizing: border-box; }
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
        header h1 { margin: 0; font-size: 23px; }
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
        h2 { margin-top: 0; color: #183153; font-size: 19px; }
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
        button:hover { background: #294d78; }
        button:disabled { opacity: 0.6; cursor: not-allowed; }
        .logout-button { background: #dc2626; }
        .close-button { background: #198754; }
        .return-button { background: #d97706; }
        .result { margin-top: 15px; overflow-x: auto; }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 12px;
            min-width: 750px;
        }
        th, td {
            padding: 11px;
            border: 1px solid #ddd;
            text-align: left;
            vertical-align: middle;
        }
        th { background: #eaf0f7; }
        .message { margin-top: 10px; color: #183153; }
        .muted { color: #666; }
        .stats-container {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
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
        .hidden { display: none !important; }

        @media (max-width: 650px) {
            header { padding: 15px; gap: 12px; }
            header h1 { font-size: 18px; }
            .container { padding: 12px; }
            .section { padding: 15px; }
            input, select { width: 100%; margin: 5px 0; }
            textarea { width: 100%; }
        }
    </style>
</head>

<body>

<header>
    <h1>Helpdesk Customer Care Portal</h1>

    <form action="<%= request.getContextPath() %>/logout"
          method="post" style="display:inline;">
        <input type="hidden" name="csrfToken"
               value="${sessionScope.csrfToken}">
        <button type="submit" class="logout-button">Logout</button>
    </form>
</header>

<div class="container">

    <h2 id="welcomeMessage">Hi, welcome to Helpdesk!</h2>
    <p class="muted">
        Search customers, investigate transactions and manage customer support tickets.
    </p>

    <div class="section" id="statisticsSection">
        <h2>Dashboard Statistics</h2>
        <div class="stats-container">
            <div class="stat-card"><h3>Total Tickets</h3><p id="totalTickets">0</p></div>
            <div class="stat-card"><h3>Open</h3><p id="openTickets">0</p></div>
            <div class="stat-card"><h3>In Progress</h3><p id="progressTickets">0</p></div>
            <div class="stat-card"><h3>Pending Review</h3><p id="pendingReviewTicketsCount">0</p></div>
            <div class="stat-card"><h3>Resolved</h3><p id="resolvedTickets">0</p></div>
            <div class="stat-card"><h3>Closed</h3><p id="closedTickets">0</p></div>
        </div>
    </div>

    <div class="section">
        <h2>1. Customer Search</h2>
        <input type="text" id="searchName" placeholder="Customer name">
        <input type="text" id="searchPhone" placeholder="Phone number">
        <button onclick="searchCustomers()">Search Customer</button>
        <div id="customerResults" class="result"></div>
    </div>

    <div class="section">
        <h2>2. Customer Details</h2>
        <div id="customerDetails" class="result">
            <p class="muted">Select a customer from the search results.</p>
        </div>
    </div>

    <div class="section">
        <h2>3. Account Information</h2>
        <div id="accountResults" class="result">
            <p class="muted">Select a customer to view account information.</p>
        </div>
    </div>

    <div class="section">
        <h2>4. Transaction Investigation</h2>
        <div id="transactionResults" class="result">
            <p class="muted">Select a customer to investigate transactions.</p>
        </div>
    </div>

    <!-- TICKET FILTERS -->
    <div class="section" id="ticketFilterSection">
        <h2>5. Filter Support Tickets</h2>

        <select id="filterStatus" onchange="clearFilteredTickets()">
            <option value="">Select Status</option>
            <option value="OPEN">Open</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="PENDING_REVIEW">Pending Review</option>
            <option value="RESOLVED">Resolved</option>
            <option value="CLOSED">Closed</option>
        </select>

        <select id="filterPriority" onchange="clearFilteredTickets()">
            <option value="">Select Priority</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="URGENT">Urgent</option>
        </select>

        <button onclick="filterTickets()">
            Apply Filters
        </button>

        <button onclick="resetTicketFilters()">
            Reset
        </button>

        <div id="filteredTickets"
             class="result"
             style="display: none;">
        </div>
    </div>

    <div class="section" id="allTicketsSection">
        <h2>6. All Support Tickets</h2>
        <button onclick="loadAllTickets()">Refresh All Tickets</button>
        <div id="allTickets" class="result">Loading tickets...</div>
    </div>

    <div class="section" id="myTicketsSection">
        <h2>7. My Assigned Tickets</h2>
        <button onclick="loadMyTickets()">Refresh My Tickets</button>
        <div id="myTickets" class="result">Loading assigned tickets...</div>
    </div>

    <div class="section" id="employeeTicketSection">
        <h2>8. Update Assigned Ticket</h2>
        <p class="muted">
            Update the progress of a ticket assigned to you.
            Completed work must be submitted for Manager review.
        </p>

        <input type="text"
               id="ticketId"
               inputmode="numeric"
               pattern="[0-9]*"
               autocomplete="off"
               placeholder="Ticket ID">
        <select id="ticketStatus">
            <option value="IN_PROGRESS">IN_PROGRESS</option>
        </select>
        <textarea id="resolutionRemarks"
                  placeholder="Enter progress or resolution remarks"></textarea>

        <button onclick="updateTicket()">Update Ticket</button>
        <button onclick="submitForReview()">Submit for Manager Review</button>
        <div id="ticketMessage" class="message"></div>
    </div>

    <div class="section" id="managerAssignmentSection">
        <h2>9. Assign / Reassign Ticket</h2>
        <p class="muted">Select a Helpdesk employee to assign or reassign a ticket.</p>

        <input type="text"
               id="assignTicketId"
               inputmode="numeric"
               pattern="[0-9]*"
               autocomplete="off"
               placeholder="Ticket ID">
        <select id="employeeId">
            <option value="">Loading Helpdesk employees...</option>
        </select>
        <button onclick="assignTicket()">Assign / Reassign Ticket</button>
        <div id="assignmentMessage" class="message"></div>
    </div>

    <div class="section" id="managerReviewSection">
        <h2>10. Manager Ticket Review</h2>
        <p class="muted">
            Review tickets submitted by employees. Close approved tickets
            or return tickets for further investigation.
        </p>
        <button onclick="loadPendingReviewTickets()">Refresh Pending Reviews</button>
        <div id="pendingReviewTickets" class="result">Loading pending reviews...</div>
    </div>

    <div class="section" id="investigationSection">
        <h2>11. Investigation History</h2>
        <input type="text"
               id="remarkTicketId"
               inputmode="numeric"
               pattern="[0-9]*"
               autocomplete="off"
               placeholder="Ticket ID">
        <textarea id="investigationRemark"
                  placeholder="Enter investigation remark"></textarea>
        <button id="addRemarkButton" onclick="addRemark()">Add Remark</button>
        <button onclick="loadRemarks()">View History</button>
        <div id="remarkResults" class="result"></div>
    </div>

</div>

<script>
    const api = "<%= request.getContextPath() %>/api";
    let currentDesignation = "";

    async function apiRequest(url, options = {}) {
        const csrfElement = document.querySelector('meta[name="csrf-token"]');

        const headers = {
            "Content-Type": "application/json",
            ...(options.headers || {})
        };

        if (csrfElement && csrfElement.content) {
            headers["X-CSRF-TOKEN"] = csrfElement.content;
        }

        const response = await fetch(api + url, {
            credentials: "same-origin",
            ...options,
            headers: headers
        });

        const responseText = await response.text();
        let data = null;

        if (responseText) {
            try {
                data = JSON.parse(responseText);
            } catch (error) {
                data = responseText;
            }
        }

        if (!response.ok) {
            let message = "Request failed: " + response.status;

            if (data && typeof data === "object") {
                message = data.message || data.error || message;
            } else if (data) {
                message = data;
            }

            throw new Error(message);
        }

        return data;
    }

    function escapeHtml(value) {
        return String(value ?? "").replace(/[&<>"']/g, function(char) {
            return {
                "&": "&amp;",
                "<": "&lt;",
                ">": "&gt;",
                '"': "&quot;",
                "'": "&#39;"
            }[char];
        });
    }

    function loadLoggedInEmployeeName(employee) {
        const displayName =
            currentDesignation === "HELPDESK_MANAGER"
                ? "Helpdesk Manager"
                : employee.employeeName;

        document.getElementById("welcomeMessage").innerText =
            "Hi " + displayName +
            ", welcome to the Helpdesk Customer Care Portal!";
    }

    // CUSTOMER SEARCH
    async function searchCustomers() {
        const name = document.getElementById("searchName").value.trim();
        const phone = document.getElementById("searchPhone").value.trim();
        document.getElementById("customerDetails").innerHTML =
            "<p class='muted'>Select a customer from the search results.</p>";

        document.getElementById("accountResults").innerHTML =
            "<p class='muted'>Select a customer to view account information.</p>";

        document.getElementById("transactionResults").innerHTML =
            "<p class='muted'>Select a customer to investigate transactions.</p>";

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

            if (!customers || customers.length === 0) {
                html = "<p>No customers found.</p>";
            } else {
                html = "<table><tr>" +
                    "<th>Customer ID</th><th>Name</th><th>Phone</th><th>Action</th>" +
                    "</tr>";

                customers.forEach(function(customer) {
                    html += "<tr>" +
                        "<td>" + escapeHtml(customer.customerId) + "</td>" +
                        "<td>" + escapeHtml(customer.customerName) + "</td>" +
                        "<td>" + escapeHtml(customer.phone) + "</td>" +
                        "<td><button onclick=\"selectCustomer(" +
                        Number(customer.customerId) + ")\">View Details</button></td>" +
                        "</tr>";
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

            document.getElementById("customerDetails").innerHTML =
                "<p><b>Customer ID:</b> " + escapeHtml(customer.customerId) + "</p>" +
                "<p><b>Name:</b> " + escapeHtml(customer.customerName) + "</p>" +
                "<p><b>Phone:</b> " + escapeHtml(customer.phone) + "</p>" +
                "<p><b>Address:</b> " + escapeHtml(customer.address) + "</p>";

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

            if (!accounts || accounts.length === 0) {
                html = "<p>No accounts found.</p>";
            } else {
                html = "<table><tr>" +
                    "<th>Account Number</th><th>Account Holder</th>" +
                    "<th>Branch</th><th>District</th><th>Status</th>" +
                    "</tr>";

                accounts.forEach(function(account) {
                    html += "<tr>" +
                        "<td>" + escapeHtml(account.accountNumber) + "</td>" +
                        "<td>" + escapeHtml(account.accountHolderName) + "</td>" +
                        "<td>" + escapeHtml(account.branchName) + "</td>" +
                        "<td>" + escapeHtml(account.district) + "</td>" +
                        "<td>" + (account.active ? "Active" : "Inactive") + "</td>" +
                        "</tr>";
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

            if (!transactions || transactions.length === 0) {
                html = "<p>No transactions found.</p>";
            } else {
                html = "<table><tr>" +
                    "<th>Transaction ID</th><th>Account</th><th>Type</th>" +
                    "<th>Amount</th><th>Date</th><th>Balance After Transaction</th>" +
                    "</tr>";

                transactions.forEach(function(transaction) {
                    html += "<tr>" +
                        "<td>" + escapeHtml(transaction.transactionId) + "</td>" +
                        "<td>" + escapeHtml(transaction.accountNumber) + "</td>" +
                        "<td>" + escapeHtml(transaction.transactionType) + "</td>" +
                        "<td>" + escapeHtml(transaction.amount) + "</td>" +
                        "<td>" + escapeHtml(transaction.transactionDate) + "</td>" +
                        "<td>" + escapeHtml(transaction.balanceAfterTransaction) + "</td>" +
                        "</tr>";
                });

                html += "</table>";
            }

            document.getElementById("transactionResults").innerHTML = html;
        } catch (error) {
            document.getElementById("transactionResults").innerText =
                "Unable to load transactions: " + error.message;
        }
    }

    // DISPLAY TICKETS
    function displayTickets(tickets, elementId) {
        const container = document.getElementById(elementId);

        if (!tickets || tickets.length === 0) {
            container.innerHTML = "<p>No tickets found.</p>";
            return;
        }

        let html = "<table><tr>" +
            "<th>Ticket ID</th><th>Customer ID</th><th>Category</th>" +
            "<th>Subject</th><th>Priority</th><th>Status</th>" +
            "<th>Assigned Employee</th><th>Latest Remarks</th><th>History</th>";

        if (currentDesignation === "HELPDESK_MANAGER") {
            html += "<th>Action</th>";
        }

        html += "</tr>";

        tickets.forEach(function(ticket) {
            const ticketId = Number(ticket.ticketId);

            html += "<tr>" +
                "<td>" + escapeHtml(ticket.ticketId) + "</td>" +
                "<td>" + escapeHtml(ticket.customerId) + "</td>" +
                "<td>" + escapeHtml(ticket.category) + "</td>" +
                "<td>" + escapeHtml(ticket.subject) + "</td>" +
                "<td>" + escapeHtml(ticket.priority) + "</td>" +
                "<td>" + escapeHtml(ticket.status) + "</td>" +
                "<td>" + escapeHtml(ticket.assignedEmployeeName || "Unassigned") + "</td>" +
                "<td>" + escapeHtml(ticket.resolutionRemarks || "-") + "</td>" +
                "<td><button onclick=\"viewTicketHistory(" +
                ticketId + ")\">View History</button></td>";

            if (currentDesignation === "HELPDESK_MANAGER") {
                html += "<td><button onclick=\"prepareAssignment(" +
                    ticketId + ")\">Assign</button></td>";
            }

            html += "</tr>";
        });

        html += "</table>";
        container.innerHTML = html;
    }

    // Open the selected ticket's investigation and manager review history.
    function viewTicketHistory(ticketId) {
        document.getElementById("remarkTicketId").value = ticketId;
        document.getElementById("investigationSection").scrollIntoView({
            behavior: "smooth"
        });
        loadRemarks();
    }

    function prepareAssignment(ticketId) {
        document.getElementById("assignTicketId").value = ticketId;
        document.getElementById("managerAssignmentSection").scrollIntoView({
            behavior: "smooth"
        });
    }

    // MANAGER: LOAD ALL TICKETS
    async function loadAllTickets() {
        if (currentDesignation !== "HELPDESK_MANAGER") return;

        try {
            const tickets = await apiRequest("/helpdesk/tickets");
            displayTickets(tickets, "allTickets");
        } catch (error) {
            document.getElementById("allTickets").innerText =
                "Unable to load tickets: " + error.message;
        }
    }

    // EMPLOYEE: LOAD ASSIGNED TICKETS
    async function loadMyTickets() {
        if (currentDesignation === "HELPDESK_MANAGER") return;

        try {
            const tickets = await apiRequest("/helpdesk/my-tickets");
            displayTickets(tickets, "myTickets");
        } catch (error) {
            document.getElementById("myTickets").innerText =
                "Unable to load assigned tickets: " + error.message;
        }
    }

    // EMPLOYEE: UPDATE TICKET
    async function updateTicket() {

        if (currentDesignation === "HELPDESK_MANAGER") {
            alert("Managers cannot use employee ticket updates.");
            return;
        }

        const ticketId = document.getElementById("ticketId").value.trim();
        const status = document.getElementById("ticketStatus").value;
        const remarks = document.getElementById("resolutionRemarks").value;

        if (!validateTicketId(ticketId)) return;

        try {
            await apiRequest(
                "/helpdesk/tickets/" + ticketId,
                {
                    method: "PUT",
                    body: JSON.stringify({
                        status: status,
                        remarks: remarks
                    })
                }
            );

            document.getElementById("ticketMessage").innerText =
                "Ticket updated successfully.";

            document.getElementById("resolutionRemarks").value = "";

            await refreshTicketData();

        } catch (error) {
            alert("Ticket update failed: " + error.message);
        }
    }

    // EMPLOYEE: SUBMIT FOR MANAGER REVIEW
    async function submitForReview() {

        if (currentDesignation === "HELPDESK_MANAGER") {
            alert("Only Helpdesk employees can submit tickets for review.");
            return;
        }

        const ticketId = document.getElementById("ticketId").value.trim();
        const remarks = document.getElementById("resolutionRemarks").value;

        if (!validateTicketId(ticketId)) return;

        if (!confirm("Submit ticket #" + ticketId + " for Manager review?")) {
            return;
        }

        try {
            await apiRequest(
                "/helpdesk/tickets/" + ticketId + "/submit-review",
                {
                    method: "PUT",
                    body: JSON.stringify({
                        remarks: remarks
                    })
                }
            );

            document.getElementById("ticketMessage").innerText =
                "Ticket submitted for Manager review successfully.";

            document.getElementById("ticketId").value = "";
            document.getElementById("resolutionRemarks").value = "";

            await refreshTicketData();

        } catch (error) {
            alert("Ticket submission failed: " + error.message);
        }
    }

    // MANAGER: LOAD HELPDESK EMPLOYEES
    async function loadHelpdeskEmployees() {
        if (currentDesignation !== "HELPDESK_MANAGER") return;

        const dropdown = document.getElementById("employeeId");
        dropdown.innerHTML =
            '<option value="">Loading Helpdesk employees...</option>';

        try {
            const employees = await apiRequest("/helpdesk/employees");

            dropdown.innerHTML =
                '<option value="">Select Helpdesk Employee</option>';

            if (!employees || employees.length === 0) {
                dropdown.innerHTML =
                    '<option value="">No assignable Helpdesk employees found</option>';
                return;
            }

            employees.forEach(function(employee) {
                const option = document.createElement("option");
                option.value = employee.employeeId;
                option.textContent =
                    employee.employeeName + " (ID: " + employee.employeeId + ")";
                dropdown.appendChild(option);
            });
        } catch (error) {
            dropdown.innerHTML =
                '<option value="">Unable to load employees</option>';
            document.getElementById("assignmentMessage").innerText =
                "Employee loading failed: " + error.message;
            console.error("Employee loading failed:", error);
        }
    }

    // MANAGER: ASSIGN OR REASSIGN TICKET
   async function assignTicket() {

       if (currentDesignation !== "HELPDESK_MANAGER") {
           alert("Only the Helpdesk Manager can assign tickets.");
           return;
       }

       const ticketId = document.getElementById("assignTicketId").value.trim();
       const employeeId = document.getElementById("employeeId").value;
       const dropdown = document.getElementById("employeeId");

       if (!validateTicketId(ticketId)) return;

       if (!employeeId) {
           alert("Please select a Helpdesk employee.");
           return;
       }

       const employeeName =
           dropdown.options[dropdown.selectedIndex].text.trim();

       document.getElementById("assignmentMessage").innerText = "";

       try {
           await apiRequest(
               "/helpdesk/tickets/" + ticketId +
               "/assign/" + employeeId,
               {
                   method: "PUT"
               }
           );

           document.getElementById("assignmentMessage").innerText =
               "Ticket #" + ticketId +
               " successfully assigned to " + employeeName + ".";

           document.getElementById("assignTicketId").value = "";
           dropdown.value = "";

           await refreshTicketData();

       } catch (error) {
           alert("Ticket assignment failed: " + error.message);
       }
   }

    // MANAGER: LOAD TICKETS PENDING REVIEW
    async function loadPendingReviewTickets() {
        if (currentDesignation !== "HELPDESK_MANAGER") return;

        const container = document.getElementById("pendingReviewTickets");

        try {
            const tickets = await apiRequest(
                "/helpdesk/tickets/filter?status=PENDING_REVIEW"
            );

            if (!tickets || tickets.length === 0) {
                container.innerHTML =
                    "<p>No tickets are currently awaiting review.</p>";
                return;
            }

            let html = "<table><tr>" +
                "<th>Ticket ID</th><th>Customer ID</th><th>Subject</th>" +
                "<th>Priority</th><th>Assigned Employee</th>" +
                "<th>Resolution Remarks</th><th>History</th><th>Action</th></tr>";

            tickets.forEach(function(ticket) {
                const ticketId = Number(ticket.ticketId);

                html += "<tr>" +
                    "<td>" + escapeHtml(ticket.ticketId) + "</td>" +
                    "<td>" + escapeHtml(ticket.customerId) + "</td>" +
                    "<td>" + escapeHtml(ticket.subject) + "</td>" +
                    "<td>" + escapeHtml(ticket.priority) + "</td>" +
                    "<td>" + escapeHtml(ticket.assignedEmployeeName || "Unassigned") + "</td>" +
                    "<td>" + escapeHtml(ticket.resolutionRemarks || "-") + "</td>" +
                    "<td><button onclick=\"viewTicketHistory(" +
                    ticketId + ")\">View History</button></td>" +
                    "<td>" +
                    "<button class=\"close-button\" onclick=\"reviewTicket(" +
                    ticketId + ", 'CLOSE')\">Close</button>" +
                    "<button class=\"return-button\" onclick=\"reviewTicket(" +
                    ticketId + ", 'RETURN')\">Return</button>" +
                    "</td></tr>";
            });

            html += "</table>";
            container.innerHTML = html;
        } catch (error) {
            container.innerText =
                "Unable to load pending reviews: " + error.message;
        }
    }

    // MANAGER: REVIEW, CLOSE OR RETURN TICKET
    async function reviewTicket(ticketId, decision) {
        if (currentDesignation !== "HELPDESK_MANAGER") {
            alert("Only the Helpdesk Manager can review tickets.");
            return;
        }

        const action = decision === "CLOSE" ? "close" : "return";

        if (!confirm(
            "Are you sure you want to " + action +
            " ticket #" + ticketId + "?"
        )) {
            return;
        }

        const remarks = prompt(
            decision === "CLOSE"
                ? "Enter review remarks (optional):"
                : "Enter the reason for returning this ticket:"
        );

        if (remarks === null) return;

        if (decision === "RETURN" && !remarks.trim()) {
            alert("Please provide a reason before returning the ticket.");
            return;
        }

        try {
            await apiRequest(
                "/helpdesk/tickets/" + ticketId + "/review",
                {
                    method: "PUT",
                    body: JSON.stringify({
                        decision: decision,
                        remarks: remarks
                    })
                }
            );

            alert(
                "Ticket #" + ticketId +
                (decision === "CLOSE"
                    ? " has been closed successfully."
                    : " has been returned to the employee successfully.")
            );

            await refreshTicketData();
        } catch (error) {
            alert("Ticket review failed: " + error.message);
        }
    }

    // EMPLOYEE: ADD INVESTIGATION REMARK
    async function addRemark() {

        if (currentDesignation === "HELPDESK_MANAGER") {
            alert("Managers cannot add employee investigation remarks.");
            return;
        }

        const ticketId =
            document.getElementById("remarkTicketId").value.trim();

        const remarks =
            document.getElementById("investigationRemark").value;

        if (!validateTicketId(ticketId)) return;

        if (!remarks.trim()) {
            alert("Please enter an investigation remark.");
            return;
        }

        try {
            await apiRequest(
                "/helpdesk/tickets/" + ticketId + "/remarks",
                {
                    method: "POST",
                    body: JSON.stringify({
                        remarks: remarks
                    })
                }
            );

            document.getElementById("investigationRemark").value = "";

            alert("Investigation remark added successfully.");

            await loadRemarks();

        } catch (error) {
            alert("Unable to add remark: " + error.message);
        }
    }

    // VIEW INVESTIGATION HISTORY

async function loadRemarks() {

    const ticketId =
        document.getElementById("remarkTicketId").value.trim();

    if (!validateTicketId(ticketId)) {
        return;
    }

    try {
        const remarks = await apiRequest(
            "/helpdesk/tickets/" + ticketId + "/remarks"
        );

        const container =
            document.getElementById("remarkResults");

        container.innerHTML = "";

        if (!remarks || remarks.length === 0) {
            const message = document.createElement("p");
            message.textContent = "No investigation remarks found.";
            container.appendChild(message);
            return;
        }

        remarks.forEach(remark => {

            const item = document.createElement("div");
            item.style.borderBottom = "1px solid #ddd";
            item.style.padding = "12px";

            const employee = document.createElement("b");
            employee.textContent = remark.employeeName || "Unknown";

            const remarkText = document.createElement("p");
            remarkText.textContent = remark.remarks || "";

            const date = document.createElement("small");
            date.textContent = remark.createdAt || "";

            item.appendChild(employee);
            item.appendChild(remarkText);
            item.appendChild(date);

            container.appendChild(item);
        });

    } catch (error) {
        alert("Unable to load remarks: " + error.message);
    }
}


    // LOAD DASHBOARD STATISTICS
    async function loadTicketStatistics() {
        try {
            const stats = await apiRequest("/helpdesk/tickets/statistics");

            document.getElementById("totalTickets").innerText = stats.total ?? 0;
            document.getElementById("openTickets").innerText = stats.open ?? 0;
            document.getElementById("progressTickets").innerText = stats.inProgress ?? 0;
            document.getElementById("pendingReviewTicketsCount").innerText =
                stats.pendingReview ?? 0;
            document.getElementById("resolvedTickets").innerText = stats.resolved ?? 0;
            document.getElementById("closedTickets").innerText = stats.closed ?? 0;
        } catch (error) {
            console.error("Statistics loading failed:", error);
        }
    }

    // FILTER TICKETS
    // FILTER TICKETS
    async function filterTickets() {

        const status =
            document.getElementById("filterStatus").value;

        const priority =
            document.getElementById("filterPriority").value;

        const results = document.getElementById("filteredTickets");

        if (!status && !priority) {
            results.innerText =
                "Please select a status or priority first.";
            results.style.display = "block";
            return;
        }

        const params = new URLSearchParams();

        if (status) {
            params.append("status", status);
        }

        if (priority) {
            params.append("priority", priority);
        }

        results.style.display = "block";
        results.innerText = "Loading filtered tickets...";

        try {
            const tickets = await apiRequest(
                "/helpdesk/tickets/filter?" + params.toString()
            );

            displayTickets(tickets, "filteredTickets");

        } catch (error) {
            results.innerText =
                "Filtering failed: " + error.message;
        }
    }

    // CLEAR OLD FILTER RESULTS
    function clearFilteredTickets() {

        const results = document.getElementById("filteredTickets");

        results.innerHTML = "";
        results.style.display = "none";
    }

    // RESET FILTERS
    function resetTicketFilters() {

        document.getElementById("filterStatus").value = "";
        document.getElementById("filterPriority").value = "";

        clearFilteredTickets();
    }


    // REFRESH DATA AFTER TICKET CHANGES
    async function refreshTicketData() {

        const tasks = [
            loadTicketStatistics()
        ];

        if (currentDesignation === "HELPDESK_MANAGER") {

            tasks.push(
                loadAllTickets(),
                loadPendingReviewTickets(),
                loadHelpdeskEmployees()
            );

        } else {

            tasks.push(loadMyTickets());
        }

        await Promise.all(tasks);
    }

    // INITIALIZE DASHBOARD
    async function initializeDashboard() {
        try {
            const employee = await apiRequest("/helpdesk/me");

            currentDesignation =
                employee.designation || "HELPDESK_EMPLOYEE";

            loadLoggedInEmployeeName(employee);

            const isManager =
                currentDesignation === "HELPDESK_MANAGER";

            document.getElementById("managerAssignmentSection")
                .classList.toggle("hidden", !isManager);

            document.getElementById("managerReviewSection")
                .classList.toggle("hidden", !isManager);

            document.getElementById("allTicketsSection")
                .classList.toggle("hidden", !isManager);

            document.getElementById("employeeTicketSection")
                .classList.toggle("hidden", isManager);

            document.getElementById("myTicketsSection")
                .classList.toggle("hidden", isManager);

            document.getElementById("addRemarkButton")
                .classList.toggle("hidden", isManager);

            document.getElementById("investigationRemark")
                .classList.toggle("hidden", isManager);

            await loadTicketStatistics();

            if (isManager) {
                await Promise.all([
                    loadAllTickets(),
                    loadPendingReviewTickets(),
                    loadHelpdeskEmployees(),
                ]);
            } else {
                await Promise.all([
                    loadMyTickets(),
                ]);
            }
        } catch (error) {
            console.error("Dashboard initialization failed:", error);
            alert(
                "Unable to initialize Helpdesk dashboard: " +
                error.message
            );
        }
    }

    function validateTicketId(value) {
        const ticketId = value.trim();

        if (!/^[0-9]+$/.test(ticketId) || Number(ticketId) <= 0) {
            alert("Please enter a valid Ticket ID using numbers only.");
            return false;
        }

        return true;
    }

    // START DASHBOARD
    initializeDashboard();
</script>

</body>
</html>