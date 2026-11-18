
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Bank Manager Dashboard | WebDisplay</title>

    <meta name="viewport" content="width=device-width, initial-scale=1">

    <meta name="csrf-token"
          content="${sessionScope.csrfToken}">

    <style>
        * {
            box-sizing: border-box;
            font-family: Arial, sans-serif;
        }

        body {
            margin: 0;
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
            flex-wrap: wrap;
            gap: 12px;
        }

        header h2 {
            margin: 0;
            color: white;
        }

        .container {
            width: 90%;
            max-width: 1200px;
            margin: 30px auto;
        }

        .section {
            background: white;
            padding: 24px;
            margin-bottom: 24px;
            border-radius: 9px;
            box-shadow: 0 2px 8px rgba(0,0,0,.06);
        }

        h1, h2 {
            color: #172b4d;
        }

        .document-card {
            background: #f7f9fc;
            border: 1px solid #dce3ec;
            border-radius: 8px;
            padding: 20px;
            margin-top: 16px;
        }

        .document-card h3 {
            margin-top: 0;
            color: #172b4d;
        }

        .document-card p {
            margin: 9px 0;
            overflow-wrap: anywhere;
        }

        button {
            background: #1769aa;
            color: white;
            border: 0;
            padding: 10px 16px;
            border-radius: 5px;
            cursor: pointer;
            margin: 5px 5px 5px 0;
        }

        button:hover {
            opacity: .9;
        }

        button:disabled {
            opacity: .6;
            cursor: not-allowed;
        }

        .approve {
            background: #26834a;
        }

        .reject {
            background: #c74646;
        }

        .logout {
            background: #dc2626;
        }

        .message {
            margin-top: 14px;
            padding: 12px;
            border-radius: 5px;
        }

        .success {
            color: #176b36;
            background: #eaf7ee;
        }

        .error {
            color: #c62828;
            background: #fff1f0;
        }

        .muted {
            color: #68778a;
        }

        .status {
            font-weight: bold;
            color: #b7791f;
        }

        .empty-message {
            padding: 18px;
            background: #f7f9fc;
            border-radius: 6px;
            color: #68778a;
        }

        @media(max-width: 600px) {
            .container {
                width: 94%;
            }

            header {
                padding: 16px 4%;
            }

            .section {
                padding: 18px;
            }
        }
    </style>
</head>

<body>

<header>

    <h2>WebDisplay | Bank Manager</h2>

    <form action="<%= request.getContextPath() %>/logout"
          method="post">

        <input type="hidden"
               name="csrfToken"
               value="${sessionScope.csrfToken}">

        <button type="submit" class="logout">
            Logout
        </button>

    </form>

</header>

<div class="container">

    <div class="section">

        <h1>Bank Manager Dashboard</h1>

        <p class="muted">
            Review customer ID documents and manage account verification.
        </p>

    </div>

    <div class="section">

        <h2>Pending ID Document Verification</h2>

        <p class="muted">
            Review the uploaded document before approving or rejecting
            the account application.
        </p>

        <button type="button"
                id="refreshButton"
                onclick="loadPendingDocuments()">

            Refresh Pending Documents

        </button>

        <div id="pendingDocumentsContainer">
            Loading pending documents...
        </div>

        <div id="documentMessage"
             class="message"
             style="display:none;">
        </div>

    </div>

    <div class="section">

        <h2>Pending Customer Address Changes</h2>

        <p class="muted">
            Review the requested address and proof document. The customer's
            existing address changes only after approval.
        </p>

        <button type="button"
                id="refreshAddressRequestsButton"
                onclick="loadPendingAddressRequests()">
            Refresh Address Requests
        </button>

        <div id="pendingAddressRequestsContainer">
            Loading address change requests...
        </div>

        <div id="addressRequestMessage"
             class="message"
             style="display:none;">
        </div>

    </div>

    <div class="section">

        <h2>Verification Instructions</h2>

        <ul>
            <li>Check that the uploaded ID document is readable.</li>
            <li>Verify that the document belongs to the customer.</li>
            <li>Approve only after completing verification.</li>
            <li>When rejecting, provide a clear reason for correction.</li>
            <li>Rejected applications can be corrected by the employee
                through the Employee Portal.</li>
        </ul>

    </div>

</div>

<script>

    const ID_DOCUMENT_API =
        "<%= request.getContextPath() %>/api/id-documents";

    const CSRF_TOKEN =
        document.querySelector('meta[name="csrf-token"]').content;


    // HTML ESCAPING
    function escapeHtml(value) {

        return String(value ?? "").replace(/[&<>"']/g, char => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        })[char]);

    }


    // DISPLAY MESSAGE
    function showDocumentMessage(message, type = "success") {

        const element =
            document.getElementById("documentMessage");

        element.textContent = message;
        element.className = "message " + type;
        element.style.display = "block";

    }


    // CLEAR MESSAGE
    function clearDocumentMessage() {

        const element =
            document.getElementById("documentMessage");

        element.textContent = "";
        element.style.display = "none";

    }


    // READ API RESPONSE SAFELY
    async function readApiResponse(response) {

        const text = await response.text();

        let result = {};

        if (text) {
            try {
                result = JSON.parse(text);
            } catch (error) {
                result = {
                    message: text
                };
            }
        }

        if (!response.ok) {

            if (response.status === 401) {
                throw new Error(
                    "Your session has expired. Please log in again."
                );
            }

            if (response.status === 403) {
                throw new Error(
                    result.message || "You are not authorized to perform this action."
                );
            }

            throw new Error(
                result.message || "Request failed. Please try again."
            );
        }

        return result;

    }


    // LOAD PENDING DOCUMENTS
    async function loadPendingDocuments() {

        const container =
            document.getElementById("pendingDocumentsContainer");

        const refreshButton =
            document.getElementById("refreshButton");

        clearDocumentMessage();

        container.innerHTML =
            "<p>Loading pending documents...</p>";

        refreshButton.disabled = true;

        try {

            const response = await fetch(
                ID_DOCUMENT_API + "/manager/pending",
                {
                    method: "GET",
                    credentials: "same-origin"
                }
            );

            const documents =
                await readApiResponse(response);

            if (!Array.isArray(documents)
                    || documents.length === 0) {

                container.innerHTML = `
                    <div class="empty-message">
                        No pending ID documents for verification.
                    </div>
                `;

                return;
            }

            container.innerHTML = documents.map(doc => `

                <div class="document-card">

                    <h3>
                        \${escapeHtml(doc.customerName)}
                    </h3>

                    <p>
                        <strong>Customer ID:</strong>
                        \${escapeHtml(doc.customerId)}
                    </p>

                    <p>
                        <strong>Account Number:</strong>
                        \${escapeHtml(doc.accountNumber)}
                    </p>

                    <p>
                        <strong>Document:</strong>
                        \${escapeHtml(doc.fileName)}
                    </p>

                    <p>
                        <strong>Uploaded At:</strong>
                        \${escapeHtml(
                            doc.uploadedAt
                                ? String(doc.uploadedAt).replace("T", " ")
                                : "-"
                        )}
                    </p>

                    <p>
                        <strong>Status:</strong>
                        \${escapeHtml(doc.status)}
                    </p>

                    <p class="status">
                        PENDING VERIFICATION
                    </p>

                    <button type="button"
                            onclick="viewDocument(\${Number(doc.id)})">

                        View Document

                    </button>

                    <button type="button"
                            class="approve"
                            onclick="approveDocument(\${Number(doc.id)}, this)">

                        Approve

                    </button>

                    <button type="button"
                            class="reject"
                            onclick="rejectDocument(\${Number(doc.id)}, this)">

                        Reject

                    </button>

                </div>

            `).join("");

        } catch (error) {

            container.innerHTML = "";

            showDocumentMessage(
                error.message,
                "error"
            );

        } finally {

            refreshButton.disabled = false;

        }

    }


    // VIEW DOCUMENT
    function viewDocument(id) {

        if (!Number.isSafeInteger(id) || id <= 0) {
            showDocumentMessage(
                "Invalid document ID.",
                "error"
            );
            return;
        }

        const url =
            ID_DOCUMENT_API + "/manager/" + id + "/file";

        const newWindow = window.open(
            url,
            "_blank"
        );

        if (!newWindow) {
            showDocumentMessage(
                "Please allow pop-ups to view the document.",
                "error"
            );
        }

    }


    // APPROVE DOCUMENT
    async function approveDocument(id, button) {

        if (!confirm(
            "Approve this document and activate the account?"
        )) {
            return;
        }

        await updateDocument(
            id,
            "approve",
            {},
            button
        );

    }


    // REJECT DOCUMENT
    async function rejectDocument(id, button) {

        const reason = prompt(
            "Enter the reason for rejecting this document:"
        );

        if (reason === null) {
            return;
        }

        if (!reason.trim()) {

            showDocumentMessage(
                "A rejection reason is required.",
                "error"
            );

            return;
        }

        await updateDocument(
            id,
            "reject",
            {
                reason: reason.trim()
            },
            button
        );

    }


    // APPROVE / REJECT API REQUEST
    async function updateDocument(
        id,
        action,
        body,
        button
    ) {

        if (!Number.isSafeInteger(id) || id <= 0) {
            showDocumentMessage(
                "Invalid document ID.",
                "error"
            );
            return;
        }

        const card = button.closest(".document-card");

        const buttons = card
            ? card.querySelectorAll("button")
            : [];

        buttons.forEach(item => item.disabled = true);

        clearDocumentMessage();

        try {

            const response = await fetch(
                ID_DOCUMENT_API + "/manager/" + id + "/" + action,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "X-CSRF-TOKEN": CSRF_TOKEN
                    },
                    credentials: "same-origin",
                    body: JSON.stringify(body)
                }
            );

            const result = await readApiResponse(response);

            // Refresh the pending list first.
            await loadPendingDocuments();

            // Display the exact message returned by the backend.
            showDocumentMessage(
                result.message || "Action completed successfully.",
                "success"
            );

        } catch (error) {

            showDocumentMessage(
                error.message || "Unable to process the document.",
                "error"
            );

            buttons.forEach(item => item.disabled = false);

        }

    }




    // ---------------------------------------------------------
    // CUSTOMER ADDRESS CHANGE REVIEW
    // ---------------------------------------------------------

    async function loadPendingAddressRequests() {
        const container =
            document.getElementById("pendingAddressRequestsContainer");
        const refreshButton =
            document.getElementById("refreshAddressRequestsButton");

        refreshButton.disabled = true;
        container.innerHTML = "<p>Loading address change requests...</p>";

        try {
            const response = await fetch(
                ID_DOCUMENT_API + "/manager/address-change/pending",
                { method: "GET", credentials: "same-origin" }
            );

            const requests = await readApiResponse(response);

            if (!Array.isArray(requests) || requests.length === 0) {
                container.innerHTML =
                    '<div class="empty-message">No pending address change requests.</div>';
                return;
            }

            container.innerHTML = requests.map(item => `
                <div class="document-card" id="address-request-\${Number(item.id)}">
                    <h3>\${escapeHtml(item.customerName)} — Customer #\${escapeHtml(item.customerId)}</h3>
                    <p><strong>Current Address:</strong> \${escapeHtml(item.currentAddress || "-")}</p>
                    <p><strong>Requested Address:</strong> \${escapeHtml(item.requestedAddress)}</p>
                    <p><strong>Proof:</strong> \${escapeHtml(item.proofFileName)}</p>
                    <p><strong>Submitted:</strong> \${escapeHtml(item.createdAt || "-")}</p>
                    <p><strong>Status:</strong> PENDING</p>

                    <button type="button"
                            onclick="viewAddressProof(\${Number(item.id)})">
                        View Address Proof
                    </button>
                    <button type="button" class="approve"
                            onclick="reviewAddressRequest(\${Number(item.id)}, 'approve', this)">
                        Approve Address
                    </button>
                    <button type="button" class="reject"
                            onclick="reviewAddressRequest(\${Number(item.id)}, 'reject', this)">
                        Reject Request
                    </button>
                </div>
            `).join("");
        } catch (error) {
            container.innerHTML = "";
            showAddressRequestMessage(error.message, "error");
        } finally {
            refreshButton.disabled = false;
        }
    }

    function showAddressRequestMessage(message, type) {
        const element = document.getElementById("addressRequestMessage");
        element.textContent = message;
        element.className = "message " + type;
        element.style.display = "block";
    }

    function viewAddressProof(id) {
        if (!Number.isSafeInteger(id) || id <= 0) {
            showAddressRequestMessage("Invalid address request ID.", "error");
            return;
        }

        const opened = window.open(
            ID_DOCUMENT_API + "/manager/address-change/" + id + "/proof",
            "_blank"
        );

        if (!opened) {
            showAddressRequestMessage(
                "Allow pop-ups to view the address proof document.",
                "error"
            );
        }
    }

    async function reviewAddressRequest(id, action, button) {
        let body = {};

        if (action === "approve") {
            if (!confirm("Approve this address change? The customer's address will be updated.")) {
                return;
            }
        } else {
            const reason = prompt("Enter the reason for rejecting this address request:");
            if (reason === null) return;

            if (!reason.trim()) {
                showAddressRequestMessage("A rejection reason is required.", "error");
                return;
            }
            body.reason = reason.trim();
        }

        const card = button.closest(".document-card");
        const buttons = card ? card.querySelectorAll("button") : [];
        buttons.forEach(item => item.disabled = true);

        try {
            const response = await fetch(
                ID_DOCUMENT_API + "/manager/address-change/" + id + "/" + action,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "X-CSRF-TOKEN": CSRF_TOKEN
                    },
                    credentials: "same-origin",
                    body: JSON.stringify(body)
                }
            );

            const result = await readApiResponse(response);
            await loadPendingAddressRequests();
            showAddressRequestMessage(
                result.message || "Address request reviewed.",
                "success"
            );
        } catch (error) {
            showAddressRequestMessage(error.message, "error");
            buttons.forEach(item => item.disabled = false);
        }
    }

    // LOAD DOCUMENTS AND ADDRESS REQUESTS WHEN DASHBOARD OPENS
    document.addEventListener("DOMContentLoaded", () => {
        loadPendingDocuments();
        loadPendingAddressRequests();
    });

</script>

</body>
</html>
