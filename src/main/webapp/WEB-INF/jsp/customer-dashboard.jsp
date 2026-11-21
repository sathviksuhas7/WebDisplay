
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

        .info-card p, .account-card p {
            margin-top: 9px;
            overflow-wrap: anywhere;
        }

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

        label {
            font-weight: bold;
            font-size: 14px;
        }

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

        /* Transaction table */
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
        .acc-no {
            display: block;
            font-size: 11px;
            color: #68778a;
        }

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

        .customer-update-cell {
            white-space: pre-line;
            min-width: 240px;
            max-width: 400px;
            overflow-wrap: anywhere;
            line-height: 1.6;
        }

        /* Keep the complete customer dashboard aligned to the same page width. */
        .container {
            width: min(92%, 1200px);
            max-width: 1200px;
            margin: 30px auto;
        }

        /* Ticket conversation is a compact panel, shown only after View / Reply. */
        .conversation-panel {
            display: none;
            width: 100%;
            max-width: 900px;
            margin: 24px auto 0;
            padding: 22px;
            background: #fbfcfe;
            border: 1px solid #e2e8f0;
            border-radius: 10px;
        }

        .conversation-panel h3 {
            margin-bottom: 10px;
            color: #172b4d;
        }

        .conversation-list {
            display: flex;
            flex-direction: column;
            gap: 10px;
            max-height: 300px;
            overflow-y: auto;
            padding: 14px;
            margin: 14px 0 18px;
            background: #f7f9fc;
            border: 1px solid #e5eaf0;
            border-radius: 8px;
        }

        #customerReply {
            display: block;
            width: 100%;
            min-height: 90px;
            max-height: 150px;
            resize: vertical;
            margin: 8px 0 12px;
        }

        .conversation-actions {
            display: flex;
            gap: 8px;
            align-items: center;
        }

        @media (max-width: 700px) {
            .container { width: 94%; margin: 18px auto; }
            .section { padding: 16px; }
            .conversation-panel { padding: 15px; }
        }




    /* Chat messages - compact natural-height bubbles */
    .conversation-list .conversation-message {
        display: flex !important;
        flex-direction: column !important;

        justify-content: flex-start !important;
        align-items: flex-start !important;
        align-self: flex-start;

        width: fit-content !important;
        max-width: 55% !important;
        min-width: 0 !important;

        height: auto !important;
        min-height: 0 !important;
        max-height: none !important;

        flex: 0 0 auto !important;

        padding: 10px 14px !important;
        margin: 0 !important;

        text-align: left !important;
        border-radius: 10px;
        font-size: 14px;
        line-height: 1.45;

        white-space: pre-wrap;
        overflow-wrap: anywhere;
        box-sizing: border-box;
    }

    /* Customer - right */
    .conversation-list .conversation-message.customer {
        align-self: flex-end !important;
        margin-left: auto !important;

        background: #e8f1ff;
        border: 1px solid #cbdfff;
        border-bottom-right-radius: 3px;
    }

    /* Helpdesk - left */
    .conversation-list .conversation-message.helpdesk {
        align-self: flex-start !important;
        margin-right: auto !important;

        background: #e8f6ed;
        border: 1px solid #ccebd6;
        border-bottom-left-radius: 3px;
    }

    /* Sender and timestamp */
    .conversation-list .conversation-message small {
        display: block !important;
        width: 100%;
        margin: 0 0 5px !important;

        text-align: left !important;
        color: #64748b;
        font-size: 11px;
        font-weight: 600;
        line-height: 1.4;
    }

    @media (max-width: 600px) {
        .conversation-list .conversation-message {
            max-width: 85% !important;
        }
    }




    </style>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/site-theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/numeric-inputs.css">
    <script src="${pageContext.request.contextPath}/js/site-theme.js" data-context-path="${pageContext.request.contextPath}"></script>
    <script src="${pageContext.request.contextPath}/js/numeric-inputs.js" defer></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard-nav.css">
    <script src="${pageContext.request.contextPath}/js/dashboard-nav.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/session-guard.js" data-context-path="${pageContext.request.contextPath}"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/assistant-widget.css">
    <script src="${pageContext.request.contextPath}/js/assistant-widget.js" data-context-path="${pageContext.request.contextPath}" defer></script>
</head>

<body data-dashboard="customer">

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

       <button type="button" onclick="showProfileEdit()">
           Manage Profile Changes
       </button>
   </div>



   <!-- SEPARATE PROFILE CHANGE SECTION -->
   <div class="section" id="profileEditForm" style="display:none; margin-top:20px;">
       <h2>Profile & Contact Updates</h2>

       <h3>Change Phone Number</h3>
       <label for="newPhone">New Phone Number</label>
       <input type="tel" id="newPhone" maxlength="10" inputmode="numeric"
              pattern="[6-9][0-9]{9}" autocomplete="tel"
              placeholder="Enter 10-digit mobile number"
              oninput="this.value=this.value.replace(/[^0-9]/g,'').slice(0,10)">
       <button type="button" onclick="requestPhoneOtp()">Send Phone OTPs</button>
       <div id="phoneOtpSection" style="display:none; margin-top:10px;">
           <label for="oldPhoneOtp">OTP sent to existing phone</label>
           <input type="text" id="oldPhoneOtp" maxlength="6"
                  inputmode="numeric" autocomplete="one-time-code">
           <label for="newPhoneOtp">OTP sent to new phone</label>
           <input type="text" id="newPhoneOtp" maxlength="6"
                  inputmode="numeric" autocomplete="one-time-code">
           <button type="button" onclick="confirmPhoneChange()">Verify and Update Phone</button>
           <button type="button" id="phoneResendButton" onclick="requestPhoneOtp()">Resend OTPs</button>
           <span id="phoneResendCountdown" class="message" aria-live="polite"></span>
           <p class="empty">Enter the OTP again if it is incorrect. Resend only if you did not receive it.</p>
       </div>
       <p id="phoneUpdateMessage" class="message" aria-live="polite"></p>

       <hr>

       <h3>Change Email Address</h3>
       <label for="newEmail">New Email Address</label>
       <input type="email" id="newEmail" placeholder="Enter new email">
       <button type="button" onclick="requestEmailOtp()">Send Email OTPs</button>
       <div id="emailOtpSection" style="display:none; margin-top:10px;">
           <label for="oldEmailOtp">OTP sent to existing email</label>
           <input type="text" id="oldEmailOtp" maxlength="6"
                  inputmode="numeric" autocomplete="one-time-code">
           <label for="newEmailOtp">OTP sent to new email</label>
           <input type="text" id="newEmailOtp" maxlength="6"
                  inputmode="numeric" autocomplete="one-time-code">
           <label for="emailPhoneOtp">OTP sent to registered phone</label>
           <input type="text" id="emailPhoneOtp" maxlength="6"
                  inputmode="numeric" autocomplete="one-time-code">
           <button type="button" onclick="confirmEmailChange()">Verify and Update Email</button>
           <button type="button" id="emailResendButton" onclick="requestEmailOtp()">Resend OTPs</button>
           <span id="emailResendCountdown" class="message" aria-live="polite"></span>
           <p class="empty">Enter the OTP again if it is incorrect. Resend only if you did not receive it.</p>
       </div>
       <p id="emailUpdateMessage" class="message" aria-live="polite"></p>

       <hr>

       <h3>Request Address Change</h3>
       <p>Submit your new address and supporting proof. The address will change only after bank approval.</p>
       <label for="requestedAddress">New Address</label>
       <textarea id="requestedAddress" rows="3" maxlength="1000"
                 placeholder="Enter your complete new address"></textarea>
       <label for="addressProof">Address Proof (PDF, JPG or PNG)</label>
       <input type="file" id="addressProof" accept=".pdf,.jpg,.jpeg,.png">
       <button type="button" onclick="requestAddressOtp()">Send Address Verification OTP</button>
       <div id="addressOtpSection" style="display:none; margin-top:10px;">
           <label for="addressOtp">OTP sent to your registered phone</label>
           <input type="text" id="addressOtp" maxlength="6"
                  inputmode="numeric" autocomplete="one-time-code">
           <button type="button" onclick="submitAddressChange()">Verify OTP & Submit Request</button>
           <button type="button" id="addressResendButton" onclick="requestAddressOtp()">Resend OTP</button>
           <span id="addressResendCountdown" class="message" aria-live="polite"></span>
           <p class="empty">Enter the OTP again if it is incorrect. Resend only if you did not receive it.</p>
       </div>
       <div id="addressRequestStatus" class="message" aria-live="polite"></div>

       <h3>Address Request History</h3>
       <div id="addressRequestHistory">
           <p class="empty">No address requests loaded.</p>
       </div>

       <hr>
       <button type="button" onclick="cancelProfileEdit()">Close Profile Changes</button>
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


    <!-- SAVED BENEFICIARIES -->
    <div class="section">
        <h2>Saved Beneficiaries</h2>

        <p>Save frequently used recipient accounts for easier transfers.</p>

        <label for="beneficiaryAccountNumber">
            Recipient Account Number
        </label>
        <input type="text"
               id="beneficiaryAccountNumber"
               placeholder="Enter recipient account number">

        <label for="beneficiaryNickname">Nickname</label>
        <input type="text"
               id="beneficiaryNickname"
               maxlength="60"
               placeholder="Example: Friend, Family, Rent">

        <button type="button" onclick="saveBeneficiary()">
            Save Beneficiary
        </button>

        <p id="beneficiaryMessage" class="message"></p>

        <div id="beneficiaryList" class="account-grid">
            <p class="empty">Loading beneficiaries...</p>
        </div>
    </div>


    <!-- TRANSACTIONS -->
    <div class="section">
        <h2>Transaction History</h2>

        <label for="transactionTypeFilter">Filter transactions</label>
        <select id="transactionTypeFilter" onchange="renderFilteredTransactions()">
            <option value="ALL">All transactions</option>
            <option value="DEPOSIT">Deposits</option>
            <option value="WITHDRAWAL">Withdrawals</option>
            <option value="TRANSFER">Transfers</option>
        </select>
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
        <p>Select the date range for your statement.</p>
        <div class="form-grid">
            <div><label for="statementFromDate">From</label><input type="date" id="statementFromDate"></div>
            <div><label for="statementToDate">To</label><input type="date" id="statementToDate"></div>
        </div>
        <button type="button" onclick="loadStatement()">View Statement Details</button>
        <button type="button" onclick="openStatementPdf()">Open PDF Statement</button>
        <button type="button" onclick="downloadStatementPdf()">Download PDF</button>
        <p id="statementMessage" class="message" aria-live="polite"></p>
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

            <label>Additional Remarks (Optional)</label>
            <textarea id="customerRemarks" rows="3" maxlength="2000"
                      placeholder="Add any additional information (optional)"></textarea>

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
                        <th>Helpdesk Update</th>
                        <th>Created At</th>
                        <th>Conversation</th>
                    </tr>
                </thead>

                <tbody id="ticketTable">
                    <tr>
                        <td colspan="8">Loading tickets...</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- CONVERSATION PANEL -->
        <div id="conversationPanel" class="conversation-panel">
            <h3 id="conversationHeading">Ticket Conversation</h3>

            <div id="conversationMessages" class="conversation-list">
                <p class="empty">
                    Select a ticket to view its conversation.
                </p>
            </div>

            <label for="customerReply">Your Reply</label>
            <textarea id="customerReply"
                      maxlength="4000"
                      placeholder="Write your reply to Helpdesk..."></textarea>

            <div class="conversation-actions">
                <button type="button"
                        id="sendReplyButton"
                        onclick="sendCustomerReply()">
                    Send Reply
                </button>

                <button type="button"
                        class="conversation-close"
                        onclick="closeConversation()">
                    Close
                </button>
            </div>

            <p id="conversationMessage" class="message"></p>
        </div>
    </div>

</div>

<script>
    const API = "<%= request.getContextPath() %>/api";

    let selectedAccountId = null;
    let loadedCustomerTransactions = [];
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

    // PROFILE OTP STATE
    let phoneOtpChallenges = null;
    let emailOtpChallenges = null;
    let addressOtpChallengeId = null;
    let addressRequestCache = [];
    const otpResendTimers = {};
    const OTP_RESEND_COOLDOWN_SECONDS = 62; // Backend enforces 60 seconds; small buffer avoids boundary errors.

    function startOtpResendCooldown(buttonId, countdownId) {
        if (otpResendTimers[buttonId]) {
            clearInterval(otpResendTimers[buttonId]);
        }

        const button = document.getElementById(buttonId);
        const countdown = document.getElementById(countdownId);
        if (!button || !countdown) return;

        let remaining = OTP_RESEND_COOLDOWN_SECONDS;
        button.disabled = true;
        button.textContent = "Resend available soon";
        countdown.textContent = "You can resend in " + remaining + " seconds.";

        otpResendTimers[buttonId] = setInterval(() => {
            remaining--;
            if (remaining <= 0) {
                clearInterval(otpResendTimers[buttonId]);
                delete otpResendTimers[buttonId];
                button.disabled = false;
                button.textContent = buttonId === "addressResendButton" ? "Resend OTP" : "Resend OTPs";
                countdown.textContent = "You can request a new OTP now.";
                return;
            }
            countdown.textContent = "You can resend in " + remaining + " seconds.";
        }, 1000);
    }

    function resetOtpResendCooldown(buttonId, countdownId) {
        if (otpResendTimers[buttonId]) {
            clearInterval(otpResendTimers[buttonId]);
            delete otpResendTimers[buttonId];
        }
        const button = document.getElementById(buttonId);
        const countdown = document.getElementById(countdownId);
        if (button) {
            button.disabled = false;
            button.textContent = buttonId === "addressResendButton" ? "Resend OTP" : "Resend OTPs";
        }
        if (countdown) countdown.textContent = "";
    }

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

    // PROFILE EDIT CONTROLS

    function showProfileEdit() {
        document.getElementById("profileEditForm").style.display = "block";
        loadAddressRequests();
        document.getElementById("profileEditForm").scrollIntoView({ behavior: "smooth", block: "start" });
    }

    function cancelProfileEdit() {
        document.getElementById("profileEditForm").style.display = "none";
        ["phoneUpdateMessage", "emailUpdateMessage", "addressRequestStatus"].forEach(id => {
            const message = document.getElementById(id);
            message.textContent = "";
            message.className = "message";
        });

        phoneOtpChallenges = null;
        emailOtpChallenges = null;
        addressOtpChallengeId = null;

        document.getElementById("phoneOtpSection").style.display = "none";
        document.getElementById("emailOtpSection").style.display = "none";

        document.getElementById("newPhone").value = "";
        document.getElementById("newEmail").value = "";
        document.getElementById("oldPhoneOtp").value = "";
        document.getElementById("newPhoneOtp").value = "";
        document.getElementById("oldEmailOtp").value = "";
        document.getElementById("newEmailOtp").value = "";
        document.getElementById("emailPhoneOtp").value = "";
        document.getElementById("addressOtp").value = "";
        document.getElementById("addressOtpSection").style.display = "none";
        resetOtpResendCooldown("phoneResendButton", "phoneResendCountdown");
        resetOtpResendCooldown("emailResendButton", "emailResendCountdown");
        resetOtpResendCooldown("addressResendButton", "addressResendCountdown");
    }

    function showInlineProfileMessage(elementId, message, success) {
        const element = document.getElementById(elementId);
        element.className = success
            ? "message success-message"
            : "message error-message";
        element.textContent = message;
    }

    // REQUEST PHONE OTPs
    async function requestPhoneOtp() {
        const newPhone = document.getElementById("newPhone").value.trim();

        const normalizedPhone = newPhone;

        if (!/^[6-9][0-9]{9}$/.test(normalizedPhone)) {
            showInlineProfileMessage("phoneUpdateMessage", "Enter a valid 10-digit mobile number.", false);
            return;
        }

        try {
            const result = await apiRequest(
                API + "/customer/profile/phone/request-otp",
                {
                    method: "POST",
                    body: JSON.stringify({ newPhone: normalizedPhone })
                }
            );

            phoneOtpChallenges = result;
            document.getElementById("phoneOtpSection").style.display = "block";
            document.getElementById("oldPhoneOtp").value = "";
            document.getElementById("newPhoneOtp").value = "";
            startOtpResendCooldown("phoneResendButton", "phoneResendCountdown");

            showInlineProfileMessage("phoneUpdateMessage", "OTP sent. Enter both codes to verify your phone number.", true);
        } catch (error) {
            showInlineProfileMessage("phoneUpdateMessage", "Unable to send phone OTPs: " + error.message, false);
        }
    }

    // CONFIRM PHONE CHANGE
    async function confirmPhoneChange() {
        if (!phoneOtpChallenges) {
            showInlineProfileMessage("phoneUpdateMessage", "Request the verification OTPs first.", false);
            return;
        }

        const payload = {
            newPhone: document.getElementById("newPhone").value.trim(),
            oldChallengeId: phoneOtpChallenges.oldChallengeId,
            newChallengeId: phoneOtpChallenges.newChallengeId,
            oldOtp: document.getElementById("oldPhoneOtp").value.trim(),
            newOtp: document.getElementById("newPhoneOtp").value.trim()
        };

        if (!/^[0-9]{6}$/.test(payload.oldOtp) ||
            !/^[0-9]{6}$/.test(payload.newOtp)) {
            showInlineProfileMessage("phoneUpdateMessage", "Enter both valid 6-digit OTPs.", false);
            return;
        }

        try {
            await apiRequest(
                API + "/customer/profile/phone/confirm",
                {
                    method: "POST",
                    body: JSON.stringify(payload)
                }
            );

            phoneOtpChallenges = null;
            resetOtpResendCooldown("phoneResendButton", "phoneResendCountdown");
            document.getElementById("phoneOtpSection").style.display = "none";
            document.getElementById("newPhone").value = "";
            document.getElementById("oldPhoneOtp").value = "";
            document.getElementById("newPhoneOtp").value = "";

            showInlineProfileMessage("phoneUpdateMessage", "Phone number updated successfully.", true);
            await loadProfile();
        } catch (error) {
            showInlineProfileMessage("phoneUpdateMessage", "Phone update failed: " + error.message + " Correct the OTP and try again, or resend it if needed.", false);
        }
    }

    // REQUEST EMAIL OTPs
    async function requestEmailOtp() {
        const newEmail = document.getElementById("newEmail").value.trim();

        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(newEmail)) {
            showInlineProfileMessage("emailUpdateMessage", "Enter a valid email address.", false);
            return;
        }

        try {
            const result = await apiRequest(
                API + "/customer/profile/email/request-otp",
                {
                    method: "POST",
                    body: JSON.stringify({ newEmail: newEmail })
                }
            );

            emailOtpChallenges = result;
            document.getElementById("emailOtpSection").style.display = "block";
            document.getElementById("oldEmailOtp").value = "";
            document.getElementById("newEmailOtp").value = "";
            document.getElementById("emailPhoneOtp").value = "";
            startOtpResendCooldown("emailResendButton", "emailResendCountdown");

            showInlineProfileMessage("emailUpdateMessage", "OTP sent. Enter the codes to verify your email address.", true);
        } catch (error) {
            showInlineProfileMessage("emailUpdateMessage", "Unable to send email OTPs: " + error.message, false);
        }
    }

    // CONFIRM EMAIL CHANGE
    async function confirmEmailChange() {
        if (!emailOtpChallenges) {
            showInlineProfileMessage("emailUpdateMessage", "Request the email verification OTPs first.", false);
            return;
        }

        const payload = {
            newEmail: document.getElementById("newEmail").value.trim(),
            oldEmailChallengeId: emailOtpChallenges.oldEmailChallengeId,
            newEmailChallengeId: emailOtpChallenges.newEmailChallengeId,
            phoneChallengeId: emailOtpChallenges.phoneChallengeId,
            oldEmailOtp: document.getElementById("oldEmailOtp").value.trim(),
            newEmailOtp: document.getElementById("newEmailOtp").value.trim(),
            phoneOtp: document.getElementById("emailPhoneOtp").value.trim()
        };

        if (!/^[0-9]{6}$/.test(payload.oldEmailOtp) ||
            !/^[0-9]{6}$/.test(payload.newEmailOtp) ||
            !/^[0-9]{6}$/.test(payload.phoneOtp)) {
            showInlineProfileMessage("emailUpdateMessage", "Enter all three valid 6-digit OTPs.", false);
            return;
        }

        try {
            await apiRequest(
                API + "/customer/profile/email/confirm",
                {
                    method: "POST",
                    body: JSON.stringify(payload)
                }
            );

            emailOtpChallenges = null;
            resetOtpResendCooldown("emailResendButton", "emailResendCountdown");
            document.getElementById("emailOtpSection").style.display = "none";
            document.getElementById("newEmail").value = "";
            document.getElementById("oldEmailOtp").value = "";
            document.getElementById("newEmailOtp").value = "";
            document.getElementById("emailPhoneOtp").value = "";

            showInlineProfileMessage("emailUpdateMessage", "Email address updated successfully.", true);
        } catch (error) {
            showInlineProfileMessage("emailUpdateMessage", "Email update failed: " + error.message + " Correct the OTP and try again, or resend it if needed.", false);
        }
    }

    // REQUEST OTP FOR ADDRESS CHANGE
    async function requestAddressOtp() {
        const status = document.getElementById("addressRequestStatus");

        try {
            const result = await apiRequest(
                API + "/customer/profile/address/request-otp",
                { method: "POST" }
            );

            addressOtpChallengeId = result.challengeId;
            document.getElementById("addressOtpSection").style.display = "block";
            document.getElementById("addressOtp").value = "";
            startOtpResendCooldown("addressResendButton", "addressResendCountdown");

            status.className = "message success-message";
            status.textContent =
                "OTP sent. Enter it to verify your address-change request.";
        } catch (error) {
            status.className = "message error-message";
            status.textContent = "Unable to send address OTP: " + error.message;
        }
    }

    // SUBMIT ADDRESS CHANGE REQUEST
    async function submitAddressChange() {
        const address = document.getElementById("requestedAddress").value.trim();
        const proof = document.getElementById("addressProof").files[0];
        const status = document.getElementById("addressRequestStatus");

        if (!address) {
            status.className = "message error-message";
            status.textContent = "Enter the new address.";
            return;
        }

        const otp = document.getElementById("addressOtp").value.trim();

        if (!addressOtpChallengeId) {
            status.className = "message error-message";
            status.textContent = "Send and verify the address OTP first.";
            return;
        }

        if (!/^[0-9]{6}$/.test(otp)) {
            status.className = "message error-message";
            status.textContent = "Enter the valid 6-digit address OTP.";
            return;
        }

        if (!proof) {
            status.className = "message error-message";
            status.textContent = "Upload an address proof document.";
            return;
        }

        const allowedTypes = [
            "application/pdf",
            "image/jpeg",
            "image/png"
        ];

        if (!allowedTypes.includes(proof.type)) {
            status.className = "message error-message";
            status.textContent = "Only PDF, JPG and PNG documents are allowed.";
            return;
        }

        if (proof.size > 5 * 1024 * 1024) {
            status.className = "message error-message";
            status.textContent = "The proof document must be 5 MB or smaller.";
            return;
        }

        const formData = new FormData();
        formData.append("requestedAddress", address);
        formData.append("proof", proof);
        formData.append("challengeId", addressOtpChallengeId);
        formData.append("otp", otp);

        try {
            const response = await fetch(
                API + "/customer/profile/address/request",
                {
                    method: "POST",
                    credentials: "same-origin",
                    headers: {
                        "X-CSRF-TOKEN": getCsrfToken()
                    },
                    body: formData
                }
            );

            const responseText = await response.text();
            let data = null;

            try {
                data = responseText ? JSON.parse(responseText) : null;
            } catch (e) {
                data = responseText;
            }

            if (!response.ok) {
                throw new Error(
                    data && typeof data === "object"
                        ? (data.message || data.error || JSON.stringify(data))
                        : (data || "Request failed: " + response.status)
                );
            }

            status.className = "message success-message";
            status.textContent =
                "Address change request submitted. It will take effect only after bank approval.";

            document.getElementById("requestedAddress").value = "";
            document.getElementById("addressProof").value = "";
            document.getElementById("addressOtp").value = "";
            document.getElementById("addressOtpSection").style.display = "none";
            resetOtpResendCooldown("addressResendButton", "addressResendCountdown");
            addressOtpChallengeId = null;

            await loadAddressRequests();
        } catch (error) {
            status.className = "message error-message";
            status.textContent = "Unable to submit request: " + error.message + " Correct the OTP and try again, or resend it if needed.";
        }
    }

    function prepareAddressResubmission(requestId) {
        const previousRequest = addressRequestCache.find(item => Number(item.id) === Number(requestId));
        if (!previousRequest || String(previousRequest.status).toUpperCase() !== "REJECTED") {
            return;
        }

        document.getElementById("requestedAddress").value = previousRequest.requestedAddress || "";
        document.getElementById("addressProof").value = "";
        document.getElementById("addressOtp").value = "";
        document.getElementById("addressOtpSection").style.display = "none";
        resetOtpResendCooldown("addressResendButton", "addressResendCountdown");
        addressOtpChallengeId = null;

        const status = document.getElementById("addressRequestStatus");
        status.className = "message success-message";
        status.textContent = "Previous request was rejected. Review or edit the address, upload valid proof, request a new OTP, and submit it again. A new request will be created; the rejected request remains in history.";

        document.getElementById("requestedAddress").scrollIntoView({ behavior: "smooth", block: "center" });
        document.getElementById("requestedAddress").focus();
    }

    async function loadAddressRequests() {
        const container = document.getElementById("addressRequestHistory");

        try {
            const requests = await apiRequest(
                API + "/customer/profile/address/requests"
            );

            addressRequestCache = Array.isArray(requests) ? requests : [];

            if (!requests || requests.length === 0) {
                container.innerHTML =
                    '<p class="empty">No address change requests found.</p>';
                return;
            }

            container.innerHTML = requests.map(request => `
                <div class="info-card" style="margin-bottom:12px;">
                    <p><strong>Requested Address:</strong></p>
                    <p>\${escapeHtml(request.requestedAddress)}</p>

                    <p><strong>Status:</strong>
                        <span class="badge \${escapeHtml(request.status)}">
                            \${escapeHtml(request.status)}
                        </span>
                    </p>

                    <p><strong>Submitted:</strong>
                        \${formatDate(request.createdAt)}
                    </p>

                    <p><strong>Review Remarks:</strong>
                        \${escapeHtml(request.reviewReason || "No remarks yet")}
                    </p>

                    \${request.reviewedAt
                        ? "<p><strong>Reviewed:</strong> " + formatDate(request.reviewedAt) + "</p>"
                        : ""}

                    \${String(request.status).toUpperCase() === "REJECTED"
                        ? "<button type=\"button\" onclick=\"prepareAddressResubmission(" + Number(request.id) + ")\">Resubmit Request</button>"
                        : ""}
                </div>
            `).join("");

        } catch (error) {
            container.innerHTML =
                '<p class="error-message">Unable to load address requests: ' +
                escapeHtml(error.message) + '</p>';
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
                    <p><strong>Account Type:</strong>
                        \${escapeHtml(account.accountType || "SAVINGS")}</p>
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
        document.getElementById("transactionTypeFilter").value = "ALL";
        loadedCustomerTransactions = [];
        document.getElementById("transactionTable").innerHTML =
            '<tr><td colspan="8">Loading transactions...</td></tr>';

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
                        <h3>Account Type</h3>
                        <p>\${escapeHtml(statement.accountType || "SAVINGS")}</p>
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

    // ACCOUNT CELL
    function accountCell(name, number) {
        if (!name && !number) return "-";

        return '<span class="acc-name">' +
               escapeHtml(name || "-") +
               '</span>' +
               '<span class="acc-no">' +
               escapeHtml(number || "") +
               '</span>';
    }

    // LOAD TRANSACTIONS
    async function loadTransactions() {
        if (!selectedAccountId) return;
        const tbody = document.getElementById("transactionTable");
        tbody.innerHTML = '<tr><td colspan="8">Loading transactions...</td></tr>';
        try {
            loadedCustomerTransactions = await apiRequest(
                API + "/customer/accounts/" +
                encodeURIComponent(selectedAccountId) + "/transactions"
            );
            renderFilteredTransactions();
        } catch (error) {
            loadedCustomerTransactions = [];
            showError("transactionTable", error);
        }
    }

    function renderFilteredTransactions() {
        const tbody = document.getElementById("transactionTable");
        const filter = document.getElementById("transactionTypeFilter").value;
        const rows = (loadedCustomerTransactions || []).filter(transaction => {
            const type = String(transaction.transactionType || "").toUpperCase();
            if (filter === "ALL") return true;
            if (filter === "DEPOSIT") return type === "DEPOSIT";
            if (filter === "WITHDRAWAL") return type === "WITHDRAWAL";
            return type.startsWith("TRANSFER");
        });
        if (!rows.length) {
            tbody.innerHTML = '<tr><td colspan="8">No transactions match this filter.</td></tr>';
            return;
        }
        tbody.innerHTML = rows.map(transaction => `
            <tr>
                <td><span class="txn-ref">\${escapeHtml(transaction.referenceNumber || "-")}</span></td>
                <td>\${escapeHtml((transaction.transactionType || "-").replace(/_/g, " "))}</td>
                <td>\${accountCell(transaction.toAccountName, transaction.toAccountNumber)}</td>
                <td class="amount">₹\${Number(transaction.amount).toLocaleString("en-IN", { minimumFractionDigits: 2 })}</td>
                <td><span class="badge \${escapeHtml(transaction.status || "")}">\${escapeHtml(transaction.status || "-")}</span></td>
                <td>\${formatDate(transaction.transactionDate)}</td>
                <td class="amount">₹\${Number(transaction.balanceAfterTransaction).toLocaleString("en-IN", { minimumFractionDigits: 2 })}</td>
                <td class="desc">\${escapeHtml(transaction.description || "-")}</td>
            </tr>
        `).join("");
    }

    function getStatementDates() {
        const from = document.getElementById("statementFromDate").value;
        const to = document.getElementById("statementToDate").value;
        if (!from || !to) throw new Error("Select both start and end dates.");
        if (from > to) throw new Error("Start date must not be after end date.");
        return { fromDate: from, toDate: to };
    }

    function statementPdfUrl() {
        if (!selectedAccountId) throw new Error("Select an account first.");
        const dates = getStatementDates();
        return API + "/customer/accounts/" + encodeURIComponent(selectedAccountId)
            + "/statement/pdf?fromDate=" + encodeURIComponent(dates.fromDate)
            + "&toDate=" + encodeURIComponent(dates.toDate);
    }

    function openStatementPdf() {
        const message = document.getElementById("statementMessage");
        message.textContent = "";
        try { window.open(statementPdfUrl(), "_blank", "noopener"); }
        catch (error) { message.textContent = error.message; }
    }

    async function downloadStatementPdf() {
        const message = document.getElementById("statementMessage");
        message.textContent = "";
        try {
            const response = await fetch(statementPdfUrl(), {
                credentials: "same-origin",
                cache: "no-store"
            });
            if (!response.ok) throw new Error("Unable to generate statement. Please check the selected dates.");
            const blob = await response.blob();
            const url = URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = url;
            link.download = "WebDisplay-Account-Statement.pdf";
            document.body.appendChild(link);
            link.click();
            link.remove();
            URL.revokeObjectURL(url);
            message.textContent = "Statement downloaded.";
        } catch (error) { message.textContent = error.message; }
    }

    // LOAD ACCOUNT STATEMENT
    async function loadStatement() {
        const container = document.getElementById("statementDetails");
        document.getElementById("statementMessage").textContent = "";

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
                    <p><strong>Account Type:</strong>
                        \${escapeHtml(statement.accountType || "SAVINGS")}</p>
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

    document.getElementById("transferAmount").addEventListener("input", function () {
        const message = document.getElementById("transferMessage");
        message.textContent = "";
        message.className = "message";
    });

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
               customerRemarks: document.getElementById("customerRemarks").value.trim(),
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
                    '<tr><td colspan="8">No support tickets found.</td></tr>';

                closeConversation();
                return;
            }

            tbody.innerHTML = tickets.map(ticket => `
                <tr>
                    <td>\${escapeHtml(ticket.ticketId)}</td>
                    <td>\${escapeHtml(ticket.category)}</td>
                    <td>\${escapeHtml(ticket.subject)}</td>
                    <td>\${escapeHtml(ticket.priority)}</td>
                    <td>\${escapeHtml(ticket.status)}</td>
                    <td class="customer-update-cell">\${escapeHtml(ticket.customerRemarks || "No update from Helpdesk yet")}</td>
                    <td>\${formatDate(ticket.createdAt)}</td>
                    <td>
                        <button type="button"
                                onclick="openConversation(\${Number(ticket.ticketId)})">
                            View / Reply
                        </button>
                    </td>
                </tr>
            `).join("");

        } catch (error) {
            showError("ticketTable", error);
        }
    }

    // CURRENTLY OPEN CONVERSATION
    let activeConversationTicketId = null;

    // OPEN A TICKET CONVERSATION
    async function openConversation(ticketId) {
        activeConversationTicketId = ticketId;

        document.getElementById("conversationHeading").textContent =
            "Conversation - Ticket #" + ticketId;

        document.getElementById("conversationPanel").style.display = "block";
        document.getElementById("conversationMessage").textContent = "";
        document.getElementById("customerReply").value = "";

        await loadConversation();

        document.getElementById("conversationPanel").scrollIntoView({
            behavior: "smooth",
            block: "nearest"
        });
    }

    // LOAD CONVERSATION MESSAGES
    async function loadConversation() {
        if (!activeConversationTicketId) return;

        const container =
            document.getElementById("conversationMessages");

        container.innerHTML =
            '<p class="empty">Loading conversation...</p>';

        try {
            const messages = await apiRequest(
                API + "/customer/tickets/" +
                encodeURIComponent(activeConversationTicketId) +
                "/conversation"
            );

            if (!messages || messages.length === 0) {
                container.innerHTML =
                    '<p class="empty">No conversation messages yet.</p>';
                return;
            }

            container.innerHTML = messages.map(item => {
                const isCustomer = item.senderType === "CUSTOMER";

                const messageClass = isCustomer
                    ? "customer"
                    : "helpdesk";

                const senderLabel = isCustomer
                    ? "You"
                    : "Helpdesk - " + (item.senderName || "Support Team");

                return `
                    <div class="conversation-message \${messageClass}">
                        <small>
                            \${escapeHtml(senderLabel)}
                            · \${formatDate(item.createdAt)}
                        </small>
                        \${escapeHtml(item.message)}
                    </div>
                `;
            }).join("");

            container.scrollTop = container.scrollHeight;

        } catch (error) {
            container.innerHTML =
                '<p class="error-message">Unable to load conversation: ' +
                escapeHtml(error.message) + '</p>';
        }
    }




    // SEND CUSTOMER REPLY
    async function sendCustomerReply() {
        if (!activeConversationTicketId) return;

        const replyField =
            document.getElementById("customerReply");

        const message = replyField.value.trim();

        const status =
            document.getElementById("conversationMessage");

        const button =
            document.getElementById("sendReplyButton");

        if (!message) {
            status.className = "message error-message";
            status.textContent = "Please enter a reply.";
            return;
        }

        button.disabled = true;
        status.className = "message";
        status.textContent = "Sending reply...";

        try {
            await apiRequest(
                API + "/customer/tickets/" +
                encodeURIComponent(activeConversationTicketId) +
                "/conversation",
                {
                    method: "POST",
                    body: JSON.stringify({
                        message: message
                    })
                }
            );

            replyField.value = "";

            status.className = "message success-message";
            status.textContent = "Reply sent successfully.";

            await loadConversation();

        } catch (error) {
            status.className = "message error-message";
            status.textContent =
                "Unable to send reply: " + error.message;

        } finally {
            button.disabled = false;
        }
    }

    // CLOSE CONVERSATION PANEL
    function closeConversation() {
        const panel =
            document.getElementById("conversationPanel");

        if (panel) {
            panel.style.display = "none";
        }

        activeConversationTicketId = null;

        const replyField =
            document.getElementById("customerReply");

        if (replyField) {
            replyField.value = "";
        }
    }


    // LOAD SAVED BENEFICIARIES
    async function loadBeneficiaries() {
        const container =
            document.getElementById("beneficiaryList");

        try {
            const beneficiaries = await apiRequest(
                API + "/customer/beneficiaries"
            );

            if (!beneficiaries || beneficiaries.length === 0) {
                container.innerHTML =
                    '<p class="empty">No saved beneficiaries yet.</p>';
                return;
            }

            container.innerHTML = beneficiaries.map(item => `
                <div class="account-card">
                    <h3>\${escapeHtml(item.nickname)}</h3>

                    <p><strong>Account Holder:</strong>
                        \${escapeHtml(item.accountHolderName)}</p>

                    <p><strong>Account Number:</strong>
                        \${escapeHtml(item.accountNumber)}</p>

                    <button type="button"
                        onclick="useBeneficiary('\${escapeHtml(item.accountNumber)}')">
                        Use for Transfer
                    </button>

                    <button type="button"
                        onclick="removeBeneficiary(\${Number(item.id)})">
                        Remove
                    </button>
                </div>
            `).join("");

        } catch (error) {
            container.innerHTML =
                '<p class="error-message">' +
                escapeHtml(error.message) + '</p>';
        }
    }

    // SAVE BENEFICIARY
    async function saveBeneficiary() {
        const accountNumber =
            document.getElementById("beneficiaryAccountNumber")
                .value.trim();

        const nickname =
            document.getElementById("beneficiaryNickname")
                .value.trim();

        const message =
            document.getElementById("beneficiaryMessage");

        if (!accountNumber || !nickname) {
            message.className = "message error-message";
            message.textContent =
                "Enter both recipient account number and nickname.";
            return;
        }

        message.className = "message";
        message.textContent = "Verifying and saving beneficiary...";

        try {
            // Verify that the recipient account exists first.
            await apiRequest(
                API + "/accounts/recipient/" +
                encodeURIComponent(accountNumber)
            );

            await apiRequest(
                API + "/customer/beneficiaries",
                {
                    method: "POST",
                    body: JSON.stringify({
                        accountNumber: accountNumber,
                        nickname: nickname
                    })
                }
            );

            message.className = "message success-message";
            message.textContent = "Beneficiary saved successfully.";

            document.getElementById("beneficiaryAccountNumber").value = "";
            document.getElementById("beneficiaryNickname").value = "";

            await loadBeneficiaries();

        } catch (error) {
            message.className = "message error-message";
            message.textContent = error.message;
        }
    }

    // USE SAVED BENEFICIARY FOR TRANSFER
    async function useBeneficiary(accountNumber) {
        document.getElementById("recipientAccountNumber").value =
            accountNumber;

        await verifyRecipient();

        document.getElementById("recipientAccountNumber")
            .scrollIntoView({ behavior: "smooth", block: "center" });
    }

    // REMOVE BENEFICIARY
    async function removeBeneficiary(id) {
        if (!confirm("Are you sure you want to remove this beneficiary?")) {
            return;
        }

        const message =
            document.getElementById("beneficiaryMessage");

        try {
            await apiRequest(
                API + "/customer/beneficiaries/" +
                encodeURIComponent(id),
                {
                    method: "DELETE"
                }
            );

            message.className = "message success-message";
            message.textContent = "Beneficiary removed successfully.";

            await loadBeneficiaries();

        } catch (error) {
            message.className = "message error-message";
            message.textContent = error.message;
        }
    }


    // INITIAL LOAD
    loadBeneficiaries();
    loadProfile();
    loadAccounts();
    loadTickets();

</script>

</body>
</html>
