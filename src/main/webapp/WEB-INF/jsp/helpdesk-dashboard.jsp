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
        .ticket-chart-section {
            margin-top: 25px;
            padding: 20px;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
        }

        .ticket-chart-layout {
            display: flex;
            align-items: center;
            justify-content: center;
            flex-wrap: wrap;
            gap: 35px;
            margin-top: 20px;
        }

        .ticket-pie-chart {
            width: 230px;
            height: 230px;
            border-radius: 50%;
            background: conic-gradient(#e5e7eb 0% 100%);
        }

        .ticket-chart-legend {
            display: grid;
            gap: 13px;
            min-width: 190px;
        }

        .ticket-legend-item {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .ticket-legend-color {
            width: 13px;
            height: 13px;
            border-radius: 3px;
            flex-shrink: 0;
        }

        .ticket-legend-count {
            margin-left: auto;
            font-weight: bold;
        }
        .sla-overdue { color: #b91c1c; font-weight: bold; }
        .sla-safe { color: #15803d; font-weight: 600; }
        .sla-closed { color: #666; }
        .sla-cell { min-width: 150px; }

        @media (max-width: 650px) {
            header { padding: 15px; gap: 12px; }
            header h1 { font-size: 18px; }
            .container { padding: 12px; }
            .section { padding: 15px; }
            input, select { width: 100%; margin: 5px 0; }
            textarea { width: 100%; }
        }

        .conversation-panel {
            background: #fff;
            border: 1px solid #d8e0ea;
            border-radius: 8px;
            padding: 18px;
            margin: 18px 0;
        }

        .conversation-message {
            padding: 12px 14px;
            margin: 10px 0;
            border-radius: 8px;
            max-width: 85%;
            white-space: pre-wrap;
            overflow-wrap: anywhere;
        }

        .conversation-message.customer {
            background: #eaf2ff;
            margin-right: auto;
        }

        .conversation-message.helpdesk {
            background: #eaf7ee;
            margin-left: auto;
        }

        .conversation-message .meta {
            display: block;
            font-size: 12px;
            color: #596579;
            margin-bottom: 6px;
        }

        .conversation-compose {
            margin-top: 16px;
        }

        .conversation-compose textarea {
            width: 100%;
            min-height: 85px;
        }

        .conversation-empty {
            color: #666;
            padding: 12px 0;
        }

        .daily-ticket-chart {
            margin-top: 25px;
            padding: 20px;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            background: #fff;
        }

        .daily-ticket-bars {
            display: flex;
            align-items: flex-end;
            justify-content: space-around;
            gap: 12px;
            height: 220px;
            padding: 20px 5px 0;
            border-bottom: 1px solid #cbd5e1;
        }

        .daily-ticket-bar-item {
            flex: 1;
            height: 100%;
            display: flex;
            flex-direction: column;
            justify-content: flex-end;
            align-items: center;
            min-width: 0;
        }

        .daily-ticket-count {
            font-size: 13px;
            font-weight: bold;
            margin-bottom: 6px;
            color: #183153;
        }

        .daily-ticket-bar {
            width: 65%;
            max-width: 55px;
            min-height: 3px;
            background: #3b82f6;
            border-radius: 5px 5px 0 0;
            transition: height 0.3s ease;
        }

        .daily-ticket-day {
            font-size: 12px;
            margin-top: 9px;
            color: #555;
            text-align: center;
        }

        @media (max-width: 600px) {
            .daily-ticket-bars { gap: 5px; }
            .daily-ticket-bar { width: 75%; }
            .daily-ticket-day { font-size: 10px; }
        }


        /* Raise a ticket for a customer */
        .rt-customer { margin: 12px 0 16px; padding: 12px 14px; border-radius: 10px; background: #eef4ff; border: 1px solid #c7d7f5; color: #17345d; }
        .rt-customer.rt-empty { background: #f8fafc; border-style: dashed; border-color: #cbd5e1; color: #475569; }
        .rt-customer .rt-change { margin-left: 10px; padding: 4px 10px; font-size: 13px; }
        .rt-fields { border: 0; margin: 0; padding: 0; min-width: 0; }
        .rt-fields[disabled] { opacity: 0.55; }
        .rt-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; }
        .rt-grid select { width: 100%; }
        .rt-fields label { display: block; margin-top: 10px; font-weight: 600; }
        .rt-fields input, .rt-fields textarea { width: 100%; box-sizing: border-box; }
        .rt-req { color: #dc2626; }
        .rt-message:empty { display: none; }
        .rt-message.success { background: #f0fdf4; color: #166534; padding: 10px 12px; border-radius: 8px; }
        .rt-message.error { background: #fef2f2; color: #b91c1c; padding: 10px 12px; border-radius: 8px; }
        .rt-raise-btn { margin-top: 10px; }
        html.dark-theme .rt-customer { background: #172a46 !important; border-color: #2f4c78 !important; color: #dbeafe !important; }
        html.dark-theme .rt-customer.rt-empty { background: #131c2e !important; border-color: #33445f !important; color: #9fb0c8 !important; }
    </style>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/site-theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/numeric-inputs.css">
    <script src="${pageContext.request.contextPath}/js/site-theme.js" data-context-path="${pageContext.request.contextPath}"></script>
    <script src="${pageContext.request.contextPath}/js/numeric-inputs.js" defer></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard-nav.css">
    <script src="${pageContext.request.contextPath}/js/dashboard-nav.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/session-guard.js" data-context-path="${pageContext.request.contextPath}"></script>
</head>

<body data-dashboard="helpdesk">

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
            <div class="stat-card" style="border-left-color:#dc2626;"><h3>Overdue Tickets</h3><p id="overdueTickets">0</p></div>
            <div class="stat-card"
                 id="escalatedStatCard"
                 style="border-left-color:#b91c1c;">
                <h3>Escalated Tickets</h3>
                <p id="escalatedTickets">0</p>
            </div>
        </div>

        <!-- MANAGER: TICKET STATUS PIE CHART -->
        <div id="managerTicketChartSection" class="ticket-chart-section hidden">
            <h2>Ticket Status Distribution</h2>

            <div class="ticket-chart-layout">
                <div id="ticketStatusPieChart" class="ticket-pie-chart"></div>

                <div id="ticketStatusLegend" class="ticket-chart-legend">
                    <p class="muted">Loading chart...</p>
                </div>
            </div>
        </div>
    </div>
    <div id="dailyTicketChartSection"
         class="daily-ticket-chart hidden">

        <h2>Daily Ticket Creation</h2>

        <p class="muted">
            Number of tickets created over the last 7 days.
        </p>

        <div id="dailyTicketChart"
             class="daily-ticket-bars">
            <p class="muted">Loading chart...</p>
        </div>

    </div>

    <div class="section">
        <h2>Customer Search</h2>
        <input type="text" id="searchName" placeholder="Customer name">
        <input type="text" id="searchPhone" placeholder="Phone number">
        <button onclick="searchCustomers()">Search Customer</button>
        <div id="customerResults" class="result"></div>
    </div>

    <div class="section">
        <h2>Customer Details</h2>
        <div id="customerDetails" class="result">
            <p class="muted">Select a customer from the search results.</p>
        </div>
    </div>

    <div class="section" id="createTicketForCustomerSection"
         style="${isHelpdeskManager ? 'display:none;' : ''}">
        <h2>Raise a Ticket for a Customer</h2>
        <p class="muted">Use this when a customer calls or visits the branch. The ticket is created in the customer's name and they can follow it from their own dashboard.</p>

        <div id="ticketForCustomerSelected" class="rt-customer rt-empty">
            No customer selected yet — search for the customer above and click <b>Select</b>.
        </div>

        <fieldset id="raiseTicketFields" class="rt-fields" disabled>
            <div class="rt-grid">
                <div>
                    <label for="phoneTicketChannel">Customer contacted us by</label>
                    <select id="phoneTicketChannel">
                        <option value="PHONE_CALL">Phone call</option>
                        <option value="BRANCH_VISIT">Branch visit</option>
                        <option value="EMAIL">Email</option>
                    </select>
                </div>
                <div>
                    <label for="phoneTicketCategory">Category</label>
                    <select id="phoneTicketCategory">
                        <option value="ACCOUNT">Account Issue</option>
                        <option value="TRANSACTION">Transaction Issue</option>
                        <option value="LOGIN">Login Issue</option>
                        <option value="OTHER">Other</option>
                    </select>
                </div>
                <div>
                    <label for="phoneTicketPriority">Priority</label>
                    <select id="phoneTicketPriority">
                        <option value="LOW">Low</option>
                        <option value="MEDIUM" selected>Medium</option>
                        <option value="HIGH">High</option>
                        <option value="URGENT">Urgent</option>
                    </select>
                </div>
            </div>

            <label for="phoneTicketSubject">Subject <span class="rt-req">*</span></label>
            <input id="phoneTicketSubject" type="text" maxlength="200" placeholder="e.g. Money debited but transfer failed">

            <label for="phoneTicketDescription">Customer's issue <span class="rt-req">*</span></label>
            <textarea id="phoneTicketDescription" maxlength="4000" rows="4" placeholder="Write the problem as the customer explained it"></textarea>

            <label for="phoneTicketRemarks">Additional remarks (optional)</label>
            <textarea id="phoneTicketRemarks" maxlength="2000" rows="2" placeholder="Reference numbers, best time to call back, etc."></textarea>

            <button type="button" id="createPhoneTicketButton" onclick="createTicketForSelectedCustomer()">Create Ticket for Customer</button>
        </fieldset>
        <p id="phoneTicketMessage" class="message rt-message" role="status"></p>
    </div>

    <div class="section">
        <h2>Account Information</h2>
        <div id="accountResults" class="result">
            <p class="muted">Select a customer to view account information.</p>
        </div>
    </div>

    <div class="section">
        <h2>Transaction Investigation</h2>
        <div id="transactionResults" class="result">
            <p class="muted">Select a customer to investigate transactions.</p>
        </div>
    </div>

    <!-- TICKET FILTERS -->
    <div class="section" id="ticketFilterSection">
        <h2>Filter Support Tickets</h2>

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


    <div class="section" id="newTicketsSection">
        <h2>New Tickets</h2>
        <p>Customer tickets waiting for their first assignment.</p>
        <button onclick="loadNewTickets()">Refresh New Tickets</button>
        <div id="newTickets" class="result">Loading new tickets...</div>
    </div>

    <div class="section" id="agentRaisedTicketsSection">
        <h2>Agent-Raised Tickets</h2>
        <p>Tickets created by Helpdesk employees on behalf of customers. Each ticket is initially assigned to the employee who raised it.</p>
        <button onclick="loadAgentRaisedTickets()">Refresh Agent-Raised Tickets</button>
        <div id="agentRaisedTickets" class="result">Loading agent-raised tickets...</div>
    </div>

    <div class="section" id="allTicketsSection">
        <h2>Other Customer Tickets</h2>
        <button onclick="loadAllTickets()">Refresh Other Tickets</button>
        <div id="allTickets" class="result">Loading tickets...</div>
    </div>


    <div class="section" id="myTicketsSection">
        <h2>My Assigned Tickets</h2>
        <button onclick="loadMyTickets()">Refresh My Tickets</button>
        <div id="myTickets" class="result">Loading assigned tickets...</div>
    </div>


    <div class="section" id="employeeTicketSection">
        <h2>Work on Assigned Ticket</h2>

        <p class="muted">
            Send progress updates to customers and submit completed work
            to the Manager for approval.
        </p>

        <label for="ticketId">Ticket ID</label>
        <input type="text"
               id="ticketId"
               inputmode="numeric"
               pattern="[0-9]*"
               autocomplete="off"
               placeholder="Enter assigned Ticket ID">

        <h3>Customer Update</h3>
        <p class="muted">
            This message will be visible to the customer in My Tickets.
        </p>

        <textarea id="customerUpdateMessage"
                  placeholder="Write an update for the customer..."></textarea>

        <button type="button" onclick="sendCustomerUpdate()">
            Send Customer Update
        </button>

        <hr>

        <h3>Final Resolution Summary</h3>
        <p class="muted">
            Describe the work completed. This summary will be reviewed
            by the Helpdesk Manager. Enter at least 20 characters.
        </p>

        <textarea id="resolutionRemarks"
                  placeholder="Explain the issue, investigation, actions taken and resolution..."></textarea>

        <button type="button" onclick="submitForReview()">
            Submit for Manager Review
        </button>

        <div id="ticketMessage" class="message"></div>
    </div>


    <div class="section" id="managerReviewSection">
        <h2>Manager Ticket Review</h2>
        <p class="muted">
            Review tickets submitted by employees. Close approved tickets
            or return tickets for further investigation.
        </p>
        <button onclick="loadPendingReviewTickets()">Refresh Pending Reviews</button>
        <div id="pendingReviewTickets" class="result">Loading pending reviews...</div>
    </div>

    <div class="section" id="investigationSection">
        <h2>Investigation History</h2>
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

    <div class="section" id="ticketConversationSection" style="display:none;">

        <h2 id="ticketConversationTitle">Ticket Conversation</h2>

        <p class="muted">
            Customer-facing messages only. Internal investigation remarks
            and manager review history are not shown here.
        </p>

        <div id="ticketConversationMessages" class="conversation-panel">
            <p class="conversation-empty">
                Select a ticket to view its conversation.
            </p>
        </div>

        <div id="helpdeskConversationReply"
             class="conversation-compose"
             style="display:none;">

            <label for="helpdeskConversationMessage">
                Reply to customer
            </label>

            <textarea id="helpdeskConversationMessage"
                      maxlength="4000"
                      placeholder="Write a customer-visible reply..."></textarea>

            <button type="button"
                    id="endConversationButton"
                    onclick="endCustomerConversation()"
                    style="background:#dc2626;">
                End Chat
            </button>

            <p id="conversationEndedMessage"
               style="display:none; color:#b91c1c; font-weight:bold;">
                This conversation has ended. No further replies are allowed.
            </p>

            <button type="button"
                    id="sendHelpdeskConversationButton"
                    onclick="sendHelpdeskConversationReply()">
                Send Reply
            </button>
        </div>

        <button type="button" onclick="closeTicketConversation()">
            Close Conversation
        </button>

    </div>

</div>

<script>

    const api = "<%= request.getContextPath() %>/api";
    let currentDesignation = "";
    let helpdeskEmployees = [];
    let selectedHelpdeskCustomerId = null;

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
            selectedHelpdeskCustomerId = Number(customer.customerId || customerId);

            document.getElementById("customerDetails").innerHTML =
                "<p><b>Customer ID:</b> " + escapeHtml(customer.customerId) + "</p>" +
                "<p><b>Name:</b> " + escapeHtml(customer.customerName) + "</p>" +
                "<p><b>Phone:</b> " + escapeHtml(customer.phone) + "</p>" +
                "<p><b>Address:</b> " + escapeHtml(customer.address) + "</p>" +
                "<button type=\"button\" class=\"rt-raise-btn\" onclick=\"openRaiseTicketForm()\">Raise a ticket for this customer</button>";

            showRaiseTicketCustomer(customer);
            await loadCustomerAccounts(customerId);
            await loadCustomerTransactions(customerId);
        } catch (error) {
            alert("Unable to load customer: " + error.message);
        }
    }

    function showRaiseTicketCustomer(customer) {
        const box = document.getElementById("ticketForCustomerSelected");
        box.classList.remove("rt-empty");
        box.innerHTML = "Raising ticket for <b>" + escapeHtml(customer.customerName || "Customer") +
            "</b> (Customer ID " + escapeHtml(customer.customerId) + ")" +
            (customer.phone ? " · " + escapeHtml(customer.phone) : "");
        document.getElementById("raiseTicketFields").disabled = false;
        const message = document.getElementById("phoneTicketMessage");
        message.textContent = "";
        message.className = "message rt-message";
    }

    function openRaiseTicketForm() {
        const field = document.getElementById("phoneTicketSubject");
        document.getElementById("createTicketForCustomerSection")
            .scrollIntoView({ behavior: "smooth", block: "start" });
        setTimeout(function () { field.focus({ preventScroll: true }); }, 300);
    }

    async function createTicketForSelectedCustomer() {
        const message = document.getElementById("phoneTicketMessage");
        const button = document.getElementById("createPhoneTicketButton");

        function show(text, type) {
            message.textContent = text;
            message.className = "message rt-message " + type;
            message.style.display = "block";
        }

        if (!selectedHelpdeskCustomerId) {
            show("Search for the customer and click Select first.", "error");
            return;
        }

        const subject = document.getElementById("phoneTicketSubject").value.trim();
        const description = document.getElementById("phoneTicketDescription").value.trim();

        if (!subject || !description) {
            show("Please fill in the subject and the customer's issue.", "error");
            (subject ? document.getElementById("phoneTicketDescription")
                     : document.getElementById("phoneTicketSubject")).focus();
            return;
        }

        const label = button.textContent;
        button.disabled = true;
        button.textContent = "Creating ticket...";

        try {
            const channel = document.getElementById("phoneTicketChannel").value;
            const created = await apiRequest(
                "/helpdesk/customers/" + selectedHelpdeskCustomerId +
                "/tickets?channel=" + encodeURIComponent(channel), {
                method: "POST",
                body: JSON.stringify({
                    category: document.getElementById("phoneTicketCategory").value,
                    priority: document.getElementById("phoneTicketPriority").value,
                    subject: subject,
                    description: description,
                    customerRemarks: document.getElementById("phoneTicketRemarks").value.trim()
                })
            });

            show("Ticket #" + created.ticketId + " created for the customer. Share this ticket number with them.", "success");
            document.getElementById("phoneTicketSubject").value = "";
            document.getElementById("phoneTicketDescription").value = "";
            document.getElementById("phoneTicketRemarks").value = "";
            document.getElementById("phoneTicketPriority").value = "MEDIUM";

            // Refresh the lists this user can see.
            try {
                await loadTicketStatistics();
                if (currentDesignation === "HELPDESK_MANAGER") {
                    await Promise.all([loadNewTickets(), loadAgentRaisedTickets(), loadAllTickets()]);
                }
            } catch (refreshError) {
                console.warn("Ticket lists could not be refreshed:", refreshError);
            }
        } catch (error) {
            show("Unable to create the ticket: " + error.message, "error");
        } finally {
            button.disabled = false;
            button.textContent = label;
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

    // SLA DISPLAY: supports ISO strings and Jackson LocalDateTime arrays.
    function parseSlaDate(value) {
        if (!value) return null;
        if (Array.isArray(value)) {
            const [year, month, day, hour = 0, minute = 0, second = 0, nano = 0] = value;
            return new Date(year, month - 1, day, hour, minute, second, Math.floor(nano / 1000000));
        }
        const date = new Date(value);
        return Number.isNaN(date.getTime()) ? null : date;
    }

    function getTicketSlaHtml(ticket) {
        const status = (ticket.status || "").toUpperCase();
        if (status === "CLOSED" || status === "RESOLVED") {
            return '<span class="sla-closed">Not applicable (' + escapeHtml(status) + ')</span>';
        }
        const deadline = parseSlaDate(ticket.slaDeadline);
        if (!deadline) return '<span class="muted">Not available</span>';
        const overdue = Date.now() > deadline.getTime();
        const escalationDate = ticket.escalatedAt ? parseSlaDate(ticket.escalatedAt) : null;
        const escalationHtml = ticket.escalated
            ? '<div class="sla-escalated-at">Escalated at: ' +
                escapeHtml(escalationDate ? escalationDate.toLocaleString() : 'Timestamp unavailable') +
              '</div>'
            : '';
        return '<div class="sla-cell"><div>' + escapeHtml(deadline.toLocaleString()) + '</div>' +
            '<small class="sla-countdown ' + (overdue ? 'sla-overdue' : 'sla-safe') + '" data-deadline="' +
            deadline.getTime() + '">' + (overdue ? 'OVERDUE' : 'Calculating...') + '</small>' +
            escalationHtml + '</div>';
    }

    function updateSlaCountdowns() {
        document.querySelectorAll('.sla-countdown[data-deadline]').forEach(function(element) {
            const remaining = Number(element.dataset.deadline) - Date.now();
            if (remaining <= 0) {
                element.textContent = 'OVERDUE';
                element.className = 'sla-countdown sla-overdue';
                return;
            }
            const totalMinutes = Math.floor(remaining / 60000);
            const days = Math.floor(totalMinutes / 1440);
            const hours = Math.floor((totalMinutes % 1440) / 60);
            const minutes = totalMinutes % 60;
            let label = '';
            if (days) label += days + 'd ';
            if (hours || days) label += hours + 'h ';
            label += minutes + 'm remaining';
            element.textContent = label;
        });
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
            "<th>Subject</th><th>Description</th><th>Customer Remarks</th>" +
            "<th>Priority</th><th>Status</th><th>SLA Deadline / Remaining Time</th>" +
            "<th>Assigned Employee</th><th>Employee Resolution</th><th>History</th>" + "<th>Conversation</th>";;
        if (currentDesignation === "HELPDESK_MANAGER") {
            html += "<th>Assign Employee</th>";
        }

        html += "</tr>";

        tickets.forEach(function(ticket) {
            const ticketId = Number(ticket.ticketId);

            html += "<tr>" +
                "<td>" + escapeHtml(ticket.ticketId) + "</td>" +
                "<td>" + escapeHtml(ticket.customerId) + "</td>" +
                "<td>" + escapeHtml(ticket.category) + "</td>" +
                "<td>" + escapeHtml(ticket.subject) + "</td>" +
                "<td>" + escapeHtml(ticket.description || "-") + "</td>" +
                "<td>" + escapeHtml(ticket.customerRemarks || "-") + "</td>" +
                "<td>" + escapeHtml(ticket.priority) + "</td>" +
                "<td>" + escapeHtml(ticket.status) +
                    (ticket.escalated
                        ? '<br><span style="display:inline-block;margin-top:5px;padding:4px 8px;background:#fee2e2;color:#991b1b;border-radius:5px;font-weight:bold;">ESCALATED</span>'
                        : '') +
                    "</td>" +
                "<td>" + getTicketSlaHtml(ticket) + "</td>" +
                "<td>" + escapeHtml(ticket.assignedEmployeeName || "Unassigned") + "</td>" +
                "<td>" + escapeHtml(ticket.resolutionRemarks || "-") + "</td>" +
                "<td><button onclick=\"viewTicketHistory(" +
                ticketId + ")\">View History</button></td>" +
                "<td><button onclick=\"viewTicketConversation(" +
                ticketId + ")\">View Conversation</button></td>";

            if (currentDesignation === "HELPDESK_MANAGER") {
                let options = '<option value="">Choose employee</option>';

                helpdeskEmployees.forEach(function(employee) {
                    options += '<option value="' + employee.employeeId + '">' +
                        escapeHtml(employee.employeeName) +
                        ' (ID: ' + employee.employeeId + ')' +
                        '</option>';
                });

                html += '<td><select id="assignEmployee_' + ticketId + '">' +
                    options +
                    '</select><button onclick="assignTicketFromRow(' +
                    ticketId + ')">Assign</button></td>';
            }

            html += "</tr>";
        });

        html += "</table>";
        container.innerHTML = html;
        updateSlaCountdowns();
    }

    // Open the selected ticket's investigation and manager review history.
    function viewTicketHistory(ticketId) {
        document.getElementById("remarkTicketId").value = ticketId;
        document.getElementById("investigationSection").scrollIntoView({
            behavior: "smooth"
        });
        loadRemarks();
    }



    // MANAGER: LOAD ALL TICKETS
    async function loadNewTickets() {
        if (currentDesignation !== "HELPDESK_MANAGER") return;

        const container = document.getElementById("newTickets");

        try {
            const tickets = await apiRequest("/helpdesk/tickets");

            const newTickets = (tickets || []).filter(ticket =>
                (ticket.createdVia || "CUSTOMER").toUpperCase() === "CUSTOMER"
                && !ticket.assignedEmployeeId
                && (ticket.status || "").toUpperCase() === "OPEN"
            );

            displayTickets(newTickets, "newTickets");
        } catch (error) {
            container.innerText = "Unable to load new tickets: " + error.message;
        }
    }

    async function loadAgentRaisedTickets() {
        if (currentDesignation !== "HELPDESK_MANAGER") return;

        const container = document.getElementById("agentRaisedTickets");

        try {
            const tickets = await apiRequest("/helpdesk/tickets");
            const agentRaised = (tickets || []).filter(ticket =>
                (ticket.createdVia || "").toUpperCase() === "HELPDESK_AGENT"
            );

            displayTickets(agentRaised, "agentRaisedTickets");
        } catch (error) {
            container.innerText = "Unable to load agent-raised tickets: " + error.message;
        }
    }


    async function loadAllTickets() {
        if (currentDesignation !== "HELPDESK_MANAGER") return;

        const container = document.getElementById("allTickets");

        try {
            const tickets = await apiRequest("/helpdesk/tickets");

            const otherTickets = (tickets || []).filter(ticket =>
                (ticket.createdVia || "CUSTOMER").toUpperCase() === "CUSTOMER"
                && !(
                    !ticket.assignedEmployeeId
                    && (ticket.status || "").toUpperCase() === "OPEN"
                )
            );

            displayTickets(otherTickets, "allTickets");
        } catch (error) {
            container.innerText = "Unable to load tickets: " + error.message;
        }
    }


    async function assignTicketFromRow(ticketId) {
        if (currentDesignation !== "HELPDESK_MANAGER") {
            alert("Only the Helpdesk Manager can assign tickets.");
            return;
        }

        const dropdown = document.getElementById(
            "assignEmployee_" + ticketId
        );

        const employeeId = dropdown.value;

        if (!employeeId) {
            alert("Please select an employee.");
            return;
        }

        const employeeName =
            dropdown.options[dropdown.selectedIndex].text;

        if (!confirm(
            "Assign ticket #" + ticketId + " to " + employeeName + "?"
        )) {
            return;
        }

        try {
            await apiRequest(
                "/helpdesk/tickets/" + ticketId + "/assign/" + employeeId,
                { method: "PUT" }
            );

            alert("Ticket assigned successfully.");

            await refreshTicketData();

        } catch (error) {
            alert("Assignment failed: " + error.message);
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

    // EMPLOYEE: SEND CUSTOMER-VISIBLE UPDATE
    async function sendCustomerUpdate() {

        if (currentDesignation === "HELPDESK_MANAGER") {
            alert("Managers cannot send employee customer updates.");
            return;
        }

        const ticketId =
            document.getElementById("ticketId").value.trim();

        const message =
            document.getElementById("customerUpdateMessage").value.trim();

        if (!validateTicketId(ticketId)) return;

        if (!message) {
            alert("Please enter an update for the customer.");
            return;
        }

        try {
            await apiRequest(
                "/helpdesk/tickets/" + ticketId + "/customer-update",
                {
                    method: "PUT",
                    body: JSON.stringify({
                        message: message
                    })
                }
            );

            document.getElementById("customerUpdateMessage").value = "";

            document.getElementById("ticketMessage").innerText =
                "Customer update sent successfully.";

            await refreshTicketData();

        } catch (error) {
            alert("Unable to send customer update: " + error.message);
        }
    }



    // EMPLOYEE: SUBMIT COMPLETED WORK FOR MANAGER REVIEW
    async function submitForReview() {

        if (currentDesignation === "HELPDESK_MANAGER") {
            alert("Only Helpdesk employees can submit tickets for review.");
            return;
        }

        const ticketId =
            document.getElementById("ticketId").value.trim();

        const remarks =
            document.getElementById("resolutionRemarks").value.trim();

        if (!validateTicketId(ticketId)) return;

        if (remarks.length < 20) {
            alert(
                "Please provide a detailed final resolution summary " +
                "of at least 20 characters."
            );
            return;
        }

        if (!confirm(
            "Submit ticket #" + ticketId + " for Manager review?"
        )) {
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

       try {
           helpdeskEmployees = await apiRequest("/helpdesk/employees");
       } catch (error) {
           console.error("Unable to load employees:", error);
           helpdeskEmployees = [];
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
                "<th>Customer Description</th><th>Customer Remarks</th>" +
                "<th>Priority</th><th>SLA Deadline / Remaining Time</th><th>Assigned Employee</th>" +
                "<th>Resolution Remarks</th><th>History</th>" +
                "<th>Conversation</th><th>Return To</th><th>Action</th></tr>";

            tickets.forEach(function(ticket) {
                const ticketId = Number(ticket.ticketId);

                let options = '<option value="">Select employee</option>';

                helpdeskEmployees.forEach(function(employee) {
                    options += '<option value="' + employee.employeeId + '">' +
                        escapeHtml(employee.employeeName) +
                        ' (ID: ' + employee.employeeId + ')' +
                        '</option>';
                });

                html += "<tr>" +
                    "<td>" + escapeHtml(ticket.ticketId) + "</td>" +
                    "<td>" + escapeHtml(ticket.customerId) + "</td>" +
                    "<td>" + escapeHtml(ticket.subject) + "</td>" +
                    "<td>" + escapeHtml(ticket.description || "-") + "</td>" +
                    "<td>" + escapeHtml(ticket.customerRemarks || "-") + "</td>" +
                    "<td>" + escapeHtml(ticket.priority) + "</td>" +
                    "<td>" + getTicketSlaHtml(ticket) + "</td>" +
                    "<td>" + escapeHtml(ticket.assignedEmployeeName || "Unassigned") + "</td>" +
                    "<td>" + escapeHtml(ticket.resolutionRemarks || "-") + "</td>" +


                    "<td><button onclick=\"viewTicketHistory(" +
                    ticketId + ")\">View History</button></td>" +
                    "<td><button onclick=\"viewTicketConversation(" +
                    ticketId + ")\">View Conversation</button></td>" +
                    '<td><select id="returnEmployee_' + ticketId + '">' +
                    options + '</select></td>' +


                    "<td>" +
                    "<button class=\"close-button\" onclick=\"reviewTicket(" +
                    ticketId + ", 'CLOSE')\">Close</button> " +
                    "<button class=\"return-button\" onclick=\"reviewTicket(" +
                    ticketId + ", 'RETURN')\">Return</button>" +
                    "</td></tr>";
            });

            html += "</table>";
            container.innerHTML = html;
            updateSlaCountdowns();

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

        let employeeId = null;

        if (decision === "RETURN") {
            const dropdown = document.getElementById(
                "returnEmployee_" + ticketId
            );

            employeeId = dropdown ? dropdown.value : "";

            if (!employeeId) {
                alert("Please select an employee from the Return To dropdown.");
                return;
            }
        }

        const remarks = prompt(
            decision === "CLOSE"
                ? "Enter manager review remarks (optional):"
                : "Enter the reason for returning this ticket:"
        );

        if (remarks === null) return;

        if (decision === "RETURN" && !remarks.trim()) {
            alert("Please provide a reason before returning the ticket.");
            return;
        }

        if (!confirm(
            "Are you sure you want to " +
            (decision === "CLOSE" ? "close" : "return") +
            " ticket #" + ticketId + "?"
        )) {
            return;
        }

        try {
            await apiRequest(
                "/helpdesk/tickets/" + ticketId + "/review",
                {
                    method: "PUT",
                    body: JSON.stringify({
                        decision: decision,
                        remarks: remarks.trim(),
                        employeeId: employeeId
                    })
                }
            );

            alert(
                decision === "CLOSE"
                    ? "Ticket #" + ticketId + " closed successfully."
                    : "Ticket #" + ticketId + " returned successfully."
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

    // MANAGER: RENDER TICKET STATUS PIE CHART
    function renderTicketStatusChart(stats) {
        const chart = document.getElementById("ticketStatusPieChart");
        const legend = document.getElementById("ticketStatusLegend");

        if (!chart || !legend) {
            console.error("Ticket chart HTML elements not found.");
            return;
        }

        const statusData = [
            { label: "Open", value: Number(stats.open) || 0, color: "#2563eb" },
            { label: "In Progress", value: Number(stats.inProgress) || 0, color: "#f59e0b" },
            { label: "Pending Review", value: Number(stats.pendingReview) || 0, color: "#8b5cf6" },
            { label: "Resolved", value: Number(stats.resolved) || 0, color: "#16a34a" },
            { label: "Closed", value: Number(stats.closed) || 0, color: "#64748b" }
        ];

        const total = statusData.reduce((sum, item) => sum + item.value, 0);

        if (total === 0) {
            chart.style.background = "conic-gradient(#e5e7eb 0% 100%)";
            legend.innerHTML = "<p class='muted'>No ticket data available.</p>";
            return;
        }

        let currentPercentage = 0;
        const segments = [];

        statusData.forEach(function(item) {
            const percentage = (item.value / total) * 100;
            const nextPercentage = currentPercentage + percentage;

            segments.push(
                item.color + " " + currentPercentage + "% " + nextPercentage + "%"
            );

            currentPercentage = nextPercentage;
        });

        chart.style.background = "conic-gradient(" + segments.join(", ") + ")";

        legend.innerHTML = statusData.map(function(item) {
            const percentage = ((item.value / total) * 100).toFixed(1);

            return `
                <div class="ticket-legend-item">
                    <span class="ticket-legend-color" style="background:\${item.color}"></span>
                    <span>\${item.label}</span>
                    <span class="ticket-legend-count">\${item.value} (\${percentage}%)</span>
                </div>
            `;
        }).join("");
    }

    async function loadDailyTicketChart() {
        const chart = document.getElementById("dailyTicketChart");

        if (!chart) return;

        try {
            const tickets = await apiRequest("/helpdesk/tickets");

            const dailyCounts = {};
            const today = new Date();

            // Prepare the last 7 days
            for (let i = 6; i >= 0; i--) {
                const date = new Date(today);
                date.setDate(today.getDate() - i);

                const key =
                    date.getFullYear() + "-" +
                    String(date.getMonth() + 1).padStart(2, "0") + "-" +
                    String(date.getDate()).padStart(2, "0");

                dailyCounts[key] = {
                    count: 0,
                    label: date.toLocaleDateString("en-IN", {
                        day: "2-digit",
                        month: "short"
                    })
                };
            }

            // Count tickets using createdAt
            (tickets || []).forEach(ticket => {
                if (!ticket.createdAt) return;

                const createdDate = ticket.createdAt.substring(0, 10);

                if (dailyCounts[createdDate]) {
                    dailyCounts[createdDate].count++;
                }
            });

            const values = Object.values(dailyCounts);
            const maxCount = Math.max(1, ...values.map(day => day.count));

            let html = "";

            values.forEach(day => {
                const barHeight = (day.count / maxCount) * 150;

                html += `
                    <div class="daily-ticket-bar-item">
                        <span class="daily-ticket-count">
                            \${day.count}
                        </span>

                        <div class="daily-ticket-bar"
                             style="height: \${barHeight}px;">
                        </div>

                        <span class="daily-ticket-day">
                            \${day.label}
                        </span>
                    </div>
                `;
            });

            chart.innerHTML = html;

        } catch (error) {
            console.error("Daily ticket chart loading failed:", error);
            chart.innerHTML =
                "<p class='muted'>Unable to load daily ticket statistics.</p>";
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
            if (currentDesignation === "HELPDESK_MANAGER") {
                renderTicketStatusChart(stats);

                const dailyChartSection =
                    document.getElementById("dailyTicketChartSection");

                if (dailyChartSection) {
                    dailyChartSection.classList.remove("hidden");
                }

                loadDailyTicketChart();
            }

            document.getElementById("overdueTickets").innerText = stats.overdue ?? 0;
            const escalatedElement =
                document.getElementById("escalatedTickets");

            if (escalatedElement) {
                escalatedElement.innerText = stats.escalated ?? 0;
            }
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
        await loadTicketStatistics();

        if (currentDesignation === "HELPDESK_MANAGER") {
            // Employees must load BEFORE ticket rows are rendered.
            await loadHelpdeskEmployees();

            await Promise.all([
                loadNewTickets(),
                loadAgentRaisedTickets(),
                loadAllTickets(),
                loadPendingReviewTickets()
            ]);
        } else {
            await loadMyTickets();
        }
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
            document.getElementById("managerTicketChartSection")
                .classList.toggle("hidden", !isManager);

            document.getElementById("escalatedStatCard")
                .classList.toggle("hidden", !isManager);


            document.getElementById("managerReviewSection")
                .classList.toggle("hidden", !isManager);

            document.getElementById("allTicketsSection")
                .classList.toggle("hidden", !isManager);

            document.getElementById("newTicketsSection")
                .classList.toggle("hidden", !isManager);

            document.getElementById("agentRaisedTicketsSection")
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
                await loadHelpdeskEmployees();

                await Promise.all([
                    loadNewTickets(),
                    loadAgentRaisedTickets(),
                    loadAllTickets(),
                    loadPendingReviewTickets(),
                ]);
            } else {
                await loadMyTickets();
            }

            if (isManager) {
                window.setInterval(async function() {
                    try {
                        await refreshTicketData();
                    } catch (error) {
                        console.error(
                            "Automatic SLA dashboard refresh failed:",
                            error
                        );
                    }
                }, 60000);
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

    let activeConversationTicketId = null;

    async function viewTicketConversation(ticketId) {

        activeConversationTicketId = Number(ticketId);

        const section =
            document.getElementById("ticketConversationSection");

        const messagesContainer =
            document.getElementById("ticketConversationMessages");

        const replySection =
            document.getElementById("helpdeskConversationReply");

        document.getElementById("ticketConversationTitle").innerText =
            "Conversation - Ticket #" + activeConversationTicketId;

        messagesContainer.innerHTML =
            "<p class='conversation-empty'>Loading conversation...</p>";

        document.getElementById("helpdeskConversationMessage").value = "";

        replySection.style.display =
            currentDesignation === "HELPDESK_MANAGER" ? "none" : "block";

        section.style.display = "block";

        section.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });

        try {

            const messages = await apiRequest(
                "/helpdesk/tickets/" +
                activeConversationTicketId +
                "/conversation"
            );

            renderHelpdeskConversation(messages || []);

        } catch (error) {

            messagesContainer.innerHTML = "";

            const errorMessage = document.createElement("p");

            errorMessage.className = "conversation-empty";

            errorMessage.textContent =
                "Unable to load conversation: " + error.message;

            messagesContainer.appendChild(errorMessage);
        }
    }


    function renderHelpdeskConversation(messages) {

        const container =
            document.getElementById("ticketConversationMessages");

        container.innerHTML = "";

        if (!messages.length) {

            const empty = document.createElement("p");

            empty.className = "conversation-empty";

            empty.textContent =
                "No conversation messages found for this ticket.";

            container.appendChild(empty);

            return;
        }

        messages.forEach(function(message) {

            const senderType =
                (message.senderType || "").toUpperCase();

            const item = document.createElement("div");

            item.className =
                "conversation-message " +
                (senderType === "CUSTOMER" ? "customer" : "helpdesk");

            const meta = document.createElement("span");

            meta.className = "meta";

            meta.textContent =
                (message.senderName || senderType || "Unknown") +
                " • " +
                formatConversationDate(message.createdAt);

            const body = document.createElement("div");

            body.textContent = message.message || "";

            item.appendChild(meta);

            item.appendChild(body);

            container.appendChild(item);
        });

        container.scrollTop = container.scrollHeight;
    }


    function formatConversationDate(value) {

        if (!value) return "";

        if (Array.isArray(value)) {

            const [
                year,
                month,
                day,
                hour = 0,
                minute = 0,
                second = 0
            ] = value;

            return new Date(
                year,
                month - 1,
                day,
                hour,
                minute,
                second
            ).toLocaleString();
        }

        const date = new Date(value);

        return Number.isNaN(date.getTime())
            ? String(value)
            : date.toLocaleString();
    }


    async function sendHelpdeskConversationReply() {

        if (currentDesignation === "HELPDESK_MANAGER") {
            return;
        }

        if (!activeConversationTicketId) {
            return;
        }

        const input =
            document.getElementById("helpdeskConversationMessage");

        const message = input.value.trim();

        if (!message) {
            alert("Please enter a reply.");
            return;
        }

        const button =
            document.getElementById("sendHelpdeskConversationButton");

        button.disabled = true;

        try {

            await apiRequest(
                "/helpdesk/tickets/" +
                activeConversationTicketId +
                "/customer-update",
                {
                    method: "PUT",
                    body: JSON.stringify({
                        message: message
                    })
                }
            );

            input.value = "";

            await viewTicketConversation(activeConversationTicketId);

        } catch (error) {

            alert("Unable to send reply: " + error.message);

        } finally {

            button.disabled = false;
        }
    }


    function closeTicketConversation() {

        document.getElementById(
            "ticketConversationSection"
        ).style.display = "none";

        activeConversationTicketId = null;
    }

    //End Customer Conversation
    async function endCustomerConversation() {

        if (!activeConversationTicketId) return;

        const confirmed = confirm(
            "Are you sure you want to end this conversation? " +
            "The customer will no longer be able to reply."
        );

        if (!confirmed) return;

        const button = document.getElementById("endConversationButton");

        button.disabled = true;

        try {
            await apiRequest(
                "/helpdesk/tickets/" +
                activeConversationTicketId +
                "/conversation/end",
                { method: "PUT" }
            );

            document.getElementById("helpdeskConversationReply")
                .style.display = "none";

            button.style.display = "none";

            document.getElementById("conversationEndedMessage")
                .style.display = "block";

            await viewTicketConversation(activeConversationTicketId);

        } catch (error) {
            alert("Unable to end conversation: " + error.message);
            button.disabled = false;
        }
    }

    // Refresh SLA countdowns every 30 seconds.
    setInterval(updateSlaCountdowns, 30000);

    // START DASHBOARD
    initializeDashboard();
</script>

</body>
</html>