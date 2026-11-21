
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Dashboard</title>
    <meta name="csrf-token" content="${sessionScope.csrfToken}">

    <style>
        * {
            box-sizing: border-box;
            font-family: Arial, sans-serif;
        }

        body {
            margin: 0;
            background: #f4f6f9;
            color: #222;
        }

        header {
            background: #172b4d;
            color: white;
            padding: 20px 35px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        header h2 {
            margin: 0;
        }

        header a {
            color: white;
            text-decoration: none;
            background: #d9534f;
            padding: 9px 16px;
            border-radius: 5px;
        }

        .container {
            max-width: 1250px;
            margin: 30px auto;
            padding: 0 20px;
        }

        .card {
            background: white;
            padding: 25px;
            margin-bottom: 25px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.07);
        }

        h3 {
            margin-top: 0;
            color: #172b4d;
        }

        .form-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 15px;
        }

        input, select {
            width: 100%;
            padding: 11px;
            border: 1px solid #ccc;
            border-radius: 5px;
            background: white;
        }

        button {
            padding: 10px 16px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            background: #172b4d;
            color: white;
            margin-top: 15px;
        }

        button:hover {
            opacity: 0.9;
        }

        .edit-btn {
            background: #2874a6;
            margin: 0;
        }

        .secondary {
            background: #6c757d;
        }

        .message {
            display: none;
            margin-top: 15px;
            padding: 10px;
            border-radius: 5px;
        }

        .success {
            display: block;
            background: #d4edda;
            color: #155724;
        }

        .error {
            display: block;
            background: #f8d7da;
            color: #721c24;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
        }

        th, td {
            padding: 12px;
            border-bottom: 1px solid #ddd;
            text-align: left;
            white-space: nowrap;
        }

        th {
            background: #edf1f7;
        }

        .table-wrapper {
            overflow-x: auto;
        }

        .filter-grid {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 15px;
            margin-top: 15px;
        }

        .audit-filters {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 12px;
            margin-top: 18px;
        }

        .audit-filters input,
        .audit-filters select {
            min-width: 0;
        }

        #auditTableBody td {
            white-space: normal;
            vertical-align: top;
        }

        #auditTableBody td:nth-child(5) {
            min-width: 220px;
            max-width: 350px;
            overflow-wrap: anywhere;
        }

        #auditPageInfo {
            display: inline-block;
            font-size: 14px;
        }

        @media(max-width: 650px) {
            .form-grid,
            .filter-grid {
                grid-template-columns: 1fr;
            }

            header {
                padding: 15px;
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/password-toggle.css">
    <script src="${pageContext.request.contextPath}/js/password-toggle.js" defer></script>
</head>

<body data-dashboard="admin">

<header>
    <h2>Admin Dashboard</h2>

    <form action="<%= request.getContextPath() %>/logout"
          method="post"
          style="display:inline;">

        <input type="hidden"
               name="csrfToken"
               value="${sessionScope.csrfToken}">

        <button type="submit"
                style="background:#dc2626; color:white; padding:10px 18px;
                       border:0; border-radius:7px; cursor:pointer;">
            Logout
        </button>
    </form>
</header>

<div class="container">

    <!-- SITE-WIDE THEME SETTINGS -->
    <div class="card" id="siteThemeSettings">
        <h3>Site-Wide Appearance</h3>
        <p>Choose the appearance used across all WebDisplay dashboards.</p>
        <div class="form-grid">
            <select id="siteThemeSelect" aria-label="Site theme">
                <option value="LIGHT">Light Theme</option>
                <option value="DARK">Dark Theme</option>
            </select>
            <button type="button" id="saveSiteThemeButton">Save Theme</button>
        </div>
        <p id="siteThemeMessage" class="message" role="status" aria-live="polite"></p>
    </div>

    <!-- CREATE STAFF -->
    <div class="card">

        <h3>Create Staff Account</h3>

        <form id="staffForm">

            <div class="form-grid">

                <input type="text"
                       id="employeeName"
                       placeholder="Employee Name"
                       required>

                <input type="text"
                       id="username"
                       placeholder="Username"
                       required>

                <input type="email"
                       id="email"
                       placeholder="Email">

                <input type="password"
                       id="password"
                       placeholder="Password"
                       required>

                <select id="role" required>
                    <option value="">Select Staff Role</option>
                    <option value="EMPLOYEE">Employee</option>
                    <option value="HELPDESK">Helpdesk</option>
                    <option value="BANK_MANAGER">Bank Manager</option>
                </select>

                <!-- NEW: EMPLOYEE TYPE -->
                <select id="employeeType" required>
                    <option value="">Select Employee Type</option>
                    <option value="PRIVATE">Private Employee</option>
                    <option value="DEPARTMENT">Department Employee</option>
                </select>

                <input type="text"
                       id="department"
                       placeholder="Department">

                <input type="text"
                       id="designation"
                       placeholder="Designation">

                <input type="tel" inputmode="numeric" maxlength="10"
                       pattern="[6-9][0-9]{9}"
                       oninput="this.value=this.value.replace(/[^0-9]/g,'').slice(0,10)"
                       id="phone" placeholder="Phone Number">

            </div>

            <button type="submit">Create Staff Account</button>

            <div id="formMessage" class="message"></div>

        </form>

    </div>


    <!-- STAFF MANAGEMENT -->
    <div class="card">

        <h3>Staff Management</h3>

        <div class="filter-grid">

            <input type="text"
                   id="staffSearch"
                   placeholder="Search by employee name or username"
                   oninput="filterStaff()">

            <select id="staffRoleFilter"
                    onchange="filterStaff()">

                <option value="">All Roles</option>
                <option value="EMPLOYEE">Employee</option>
                <option value="HELPDESK">Helpdesk</option>
                <option value="BANK_MANAGER">Bank Manager</option>

            </select>

        </div>

        <button type="button" onclick="loadStaff()">
            Refresh Staff List
        </button>

        <button type="button"
                class="secondary"
                onclick="clearStaffFilters()">
            Clear Filters
        </button>

        <div id="staffMessage" class="message"></div>

        <div id="staffTableContainer" style="display:none;">

            <div class="table-wrapper">

                <table>

                    <thead>
                    <tr>
                        <th>Employee ID</th>
                        <th>Name</th>
                        <th>Username</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Employee Type</th>
                        <th>Department</th>
                        <th>Designation</th>
                        <th>Phone</th>
                        <th>Action</th>
                    </tr>
                    </thead>

                    <tbody id="staffTableBody"></tbody>

                </table>

            </div>

        </div>

    </div>



    <!-- EDIT STAFF -->
    <div class="card" id="editCard" style="display:none;">

        <h3>Edit Staff Details</h3>

        <form id="editStaffForm">

            <input type="hidden" id="editEmployeeId">

            <div class="form-grid">

                <input type="text"
                       id="editEmployeeName"
                       placeholder="Employee Name"
                       required>

                <input type="text"
                       id="editDepartment"
                       placeholder="Department">

                <input type="text"
                       id="editDesignation"
                       placeholder="Designation">

                <input type="tel" inputmode="numeric" maxlength="10"
                       pattern="[6-9][0-9]{9}"
                       oninput="this.value=this.value.replace(/[^0-9]/g,'').slice(0,10)"
                       id="editPhone" placeholder="Phone Number">

                <select id="editRole" required>
                    <option value="EMPLOYEE">Employee</option>
                    <option value="HELPDESK">Helpdesk</option>
                    <option value="BANK_MANAGER">Bank Manager</option>
                </select>

                <!-- NEW: EDIT EMPLOYEE TYPE -->
                <select id="editEmployeeType" required>
                    <option value="PRIVATE">Private Employee</option>
                    <option value="DEPARTMENT">Department Employee</option>
                </select>

            </div>

            <button type="submit">Save Changes</button>

            <button type="button"
                    class="secondary"
                    onclick="closeEditForm()">
                Cancel
            </button>

            <div id="editMessage" class="message"></div>

        </form>

    </div>

    <!-- AUDIT HISTORY -->
    <div class="card">

        <h3>Audit History</h3>
        <p>View and filter user login, logout and session activities.</p>

        <div class="audit-filters">

            <input type="text"
                   id="auditUsername"
                   placeholder="Search username">

            <select id="auditRole">
                <option value="">All Roles</option>
                <option value="ADMIN">Admin</option>
                <option value="EMPLOYEE">Employee</option>
                <option value="CUSTOMER">Customer</option>
                <option value="HELPDESK">Helpdesk</option>
                <option value="BANK_MANAGER">Bank Manager</option>
            </select>

            <select id="auditAction">
                <option value="">All Activities</option>
                <option value="LOGIN">Login</option>
                <option value="LOGOUT">Logout</option>
                <option value="FAILED_LOGIN">Failed Login</option>
                <option value="SESSION_REPLACED">Session Replaced</option>
            </select>

            <input type="datetime-local" id="auditFromDate"
                   title="From date">

            <input type="datetime-local" id="auditToDate"
                   title="To date">

        </div>

        <button type="button" onclick="loadAuditLogs(0)">
            Apply Filters
        </button>

        <button type="button" class="secondary"
                onclick="clearAuditFilters()">
            Clear Filters
        </button>

        <button type="button" class="secondary"
                onclick="loadAuditLogs(currentAuditPage)">
            Refresh
        </button>

        <div id="auditMessage" class="message"></div>

        <div class="table-wrapper" id="auditTableContainer"
             style="display:none;">

            <table>
                <thead>
                <tr>
                    <th>Date & Time</th>
                    <th>Username</th>
                    <th>Role</th>
                    <th>Activity</th>
                    <th>Details</th>
                    <th>IP Address</th>
                </tr>
                </thead>

                <tbody id="auditTableBody"></tbody>
            </table>

        </div>

        <div id="auditPagination"
             style="display:none; margin-top:15px;">

            <button type="button" class="secondary"
                    id="auditPrevious"
                    onclick="loadAuditLogs(currentAuditPage - 1)">
                Previous
            </button>

            <span id="auditPageInfo"
                  style="margin:0 12px;"></span>

            <button type="button" class="secondary"
                    id="auditNext"
                    onclick="loadAuditLogs(currentAuditPage + 1)">
                Next
            </button>

        </div>

    </div>

</div>


<script>

    const API = "<%= request.getContextPath() %>/api/admin";

    let allStaff = [];


    // HTML ESCAPING
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


    // DISPLAY MESSAGE
    function showMessage(elementId, message, type) {

        const element = document.getElementById(elementId);

        element.textContent = message;
        element.className = "message " + type;
        element.style.display = "block";

    }


    // API REQUEST
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

        const text = await response.text();

        if (!response.ok) {
            throw new Error(text || "Request failed");
        }

        return text ? JSON.parse(text) : null;

    }


    // CREATE STAFF
    document.getElementById("staffForm")
        .addEventListener("submit", async function(event) {

            event.preventDefault();

            const staff = {

                employeeName: document.getElementById("employeeName").value.trim(),

                username: document.getElementById("username").value.trim(),

                email: document.getElementById("email").value.trim(),

                password: document.getElementById("password").value,

                role: document.getElementById("role").value,

                employeeType: document.getElementById("employeeType").value,

                department: document.getElementById("department").value.trim(),

                designation: document.getElementById("designation").value.trim(),

                phone: document.getElementById("phone").value.trim()

            };


            try {

                await apiRequest(API + "/staff", {

                    method: "POST",

                    body: JSON.stringify(staff)

                });

                showMessage(
                    "formMessage",
                    "Staff account created successfully!",
                    "success"
                );

                this.reset();

                await loadStaff();

            } catch (error) {

                showMessage(
                    "formMessage",
                    error.message,
                    "error"
                );

            }

        });


    // LOAD ALL STAFF
    async function loadStaff(options = {}) {

        const container = document.getElementById("staffTableContainer");

        try {

            allStaff = await apiRequest(API + "/staff");

            filterStaff();
            if (options.preserveMessage) {
                document.getElementById("staffMessage").style.display = "block";
            }

        } catch (error) {

            container.style.display = "none";

            showMessage(
                "staffMessage",
                error.message,
                "error"
            );

        }

    }


    // FILTER STAFF
    function filterStaff() {

        const search = document.getElementById("staffSearch")
            .value.trim().toLowerCase();

        const role = document.getElementById("staffRoleFilter").value;


        const filteredStaff = allStaff.filter(function(staff) {

            const matchesSearch =

                (staff.employeeName || "").toLowerCase().includes(search) ||

                (staff.username || "").toLowerCase().includes(search);

            const matchesRole = !role || staff.role === role;

            return matchesSearch && matchesRole;

        });


        displayStaff(filteredStaff);

    }


    // CLEAR FILTERS
    function clearStaffFilters() {

        document.getElementById("staffSearch").value = "";

        document.getElementById("staffRoleFilter").value = "";

        filterStaff();

    }


    // DISPLAY STAFF TABLE
    function displayStaff(staffList) {

        const container = document.getElementById("staffTableContainer");

        const tbody = document.getElementById("staffTableBody");

        tbody.innerHTML = "";


        if (!staffList || staffList.length === 0) {

            container.style.display = "none";

            showMessage(
                "staffMessage",
                "No matching staff found.",
                "error"
            );

            return;

        }


        staffList.forEach(function(staff) {

            const row = document.createElement("tr");


            row.innerHTML =

                "<td>" + escapeHtml(staff.employeeId) + "</td>" +

                "<td>" + escapeHtml(staff.employeeName) + "</td>" +

                "<td>" + escapeHtml(staff.username) + "</td>" +

                "<td>" + escapeHtml(staff.email) + "</td>" +

                "<td>" + escapeHtml(staff.role) + "</td>" +

                "<td>" + escapeHtml(staff.employeeType || "PRIVATE") + "</td>" +

                "<td>" + escapeHtml(staff.department) + "</td>" +

                "<td>" + escapeHtml(staff.designation) + "</td>" +

                "<td>" + escapeHtml(staff.phone) + "</td>" +

                "<td><button type='button' class='edit-btn'>Edit</button></td>";


            row.querySelector("button").addEventListener("click", function() {

                openEditForm(staff);

            });


            tbody.appendChild(row);

        });


        document.getElementById("staffMessage").style.display = "none";

        container.style.display = "block";

    }


    // OPEN EDIT FORM
    function openEditForm(staff) {

        document.getElementById("editEmployeeId").value = staff.employeeId;

        document.getElementById("editEmployeeName").value = staff.employeeName || "";

        document.getElementById("editDepartment").value = staff.department || "";

        document.getElementById("editDesignation").value = staff.designation || "";

        document.getElementById("editPhone").value = staff.phone || "";

        document.getElementById("editRole").value = staff.role;

        // Load existing employee type; default old records to PRIVATE.
        document.getElementById("editEmployeeType").value =
            staff.employeeType || "PRIVATE";


        document.getElementById("editMessage").style.display = "none";

        document.getElementById("editCard").style.display = "block";


        document.getElementById("editCard").scrollIntoView({
            behavior: "smooth"
        });

    }


    // CLOSE EDIT FORM
    function closeEditForm() {

        document.getElementById("editCard").style.display = "none";

        document.getElementById("editStaffForm").reset();

    }


    // UPDATE STAFF
    document.getElementById("editStaffForm")
        .addEventListener("submit", async function(event) {
            event.preventDefault();

            const employeeId = document.getElementById("editEmployeeId").value;
            const form = this;
            const saveButton = form.querySelector('button[type="submit"]');
            const originalLabel = saveButton.textContent;
            const updatedStaff = {
                employeeName: document.getElementById("editEmployeeName").value.trim(),
                department: document.getElementById("editDepartment").value.trim(),
                designation: document.getElementById("editDesignation").value.trim(),
                phone: document.getElementById("editPhone").value.trim(),
                role: document.getElementById("editRole").value,
                employeeType: document.getElementById("editEmployeeType").value
            };

            saveButton.disabled = true;
            saveButton.textContent = "Saving...";

            try {
                await apiRequest(API + "/staff/" + employeeId, {
                    method: "PUT",
                    body: JSON.stringify(updatedStaff)
                });

                closeEditForm();
                await loadStaff({ preserveMessage: true });
                showMessage("staffMessage", "Staff details updated successfully.", "success");
            } catch (error) {
                showMessage("editMessage", "Unable to update staff details: " + error.message, "error");
            } finally {
                saveButton.disabled = false;
                saveButton.textContent = originalLabel;
            }
        });


    // AUDIT HISTORY

    let currentAuditPage = 0;

    async function loadAuditLogs(page) {

        const message = document.getElementById("auditMessage");
        const tableContainer = document.getElementById("auditTableContainer");
        const pagination = document.getElementById("auditPagination");

        const username = document.getElementById("auditUsername").value.trim();
        const role = document.getElementById("auditRole").value;
        const action = document.getElementById("auditAction").value;
        const fromDate = document.getElementById("auditFromDate").value;
        const toDate = document.getElementById("auditToDate").value;

        if (fromDate && toDate && fromDate > toDate) {
            showMessage(
                "auditMessage",
                "From date cannot be later than To date.",
                "error"
            );
            return;
        }

        const params = new URLSearchParams();

        if (username) params.append("username", username);
        if (role) params.append("role", role);
        if (action) params.append("action", action);
        if (fromDate) params.append("fromDate", fromDate);
        if (toDate) params.append("toDate", toDate);

        params.append("page", page);
        params.append("size", 20);

        try {

            message.style.display = "none";

            const result = await apiRequest(
                API + "/audit-logs?" + params.toString()
            );

            currentAuditPage = result.number || 0;

            const tbody = document.getElementById("auditTableBody");
            tbody.innerHTML = "";

            if (!result.content || result.content.length === 0) {

                tableContainer.style.display = "none";
                pagination.style.display = "none";

                showMessage(
                    "auditMessage",
                    "No audit records found.",
                    "error"
                );

                return;
            }

            result.content.forEach(function(log) {

                const row = document.createElement("tr");

                const dateTime = log.createdAt
                    ? log.createdAt.replace("T", " ")
                    : "-";

                row.innerHTML =
                    "<td>" + escapeHtml(dateTime) + "</td>" +
                    "<td>" + escapeHtml(log.username) + "</td>" +
                    "<td>" + escapeHtml(log.role || "-") + "</td>" +
                    "<td>" + escapeHtml(log.action) + "</td>" +
                    "<td>" + escapeHtml(log.details || "-") + "</td>" +
                    "<td>" + escapeHtml(log.ipAddress || "-") + "</td>";

                tbody.appendChild(row);
            });

            tableContainer.style.display = "block";
            pagination.style.display = "block";

            const totalPages = result.totalPages || 0;
            const currentPage = result.number || 0;

            document.getElementById("auditPageInfo").textContent =
                "Page " + (totalPages === 0 ? 0 : currentPage + 1) +
                " of " + totalPages +
                " | Total records: " + result.totalElements;

            document.getElementById("auditPrevious").disabled =
                currentPage <= 0;

            document.getElementById("auditNext").disabled =
                currentPage >= totalPages - 1;

        } catch (error) {

            tableContainer.style.display = "none";
            pagination.style.display = "none";

            showMessage(
                "auditMessage",
                "Unable to load audit history: " + error.message,
                "error"
            );
        }
    }


    function clearAuditFilters() {

        document.getElementById("auditUsername").value = "";
        document.getElementById("auditRole").value = "";
        document.getElementById("auditAction").value = "";
        document.getElementById("auditFromDate").value = "";
        document.getElementById("auditToDate").value = "";

        loadAuditLogs(0);
    }

    // LOAD DASHBOARD DATA
    window.addEventListener("DOMContentLoaded", function() {
        loadStaff();
        loadAuditLogs(0);
    });

</script>

</body>
</html>
