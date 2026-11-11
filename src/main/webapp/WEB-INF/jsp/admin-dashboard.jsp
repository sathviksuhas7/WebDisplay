
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
</head>

<body>

<header>
    <h2>Admin Dashboard</h2>
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

    <!-- CREATE STAFF -->
    <div class="card">

        <h3>Create Staff Account</h3>

        <form id="staffForm">

            <div class="form-grid">

                <input type="text" id="employeeName"
                       placeholder="Employee Name" required>

                <input type="text" id="username"
                       placeholder="Username" required>

                <input type="email" id="email"
                       placeholder="Email">

                <input type="password" id="password"
                       placeholder="Password" required>

                <select id="role" required>
                    <option value="">Select Staff Role</option>
                    <option value="EMPLOYEE">Employee</option>
                    <option value="HELPDESK">Helpdesk</option>
                </select>

                <input type="text" id="department"
                       placeholder="Department">

                <input type="text" id="designation"
                       placeholder="Designation">

                <input type="text" id="phone"
                       placeholder="Phone Number">

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

                <input type="text"
                       id="editPhone"
                       placeholder="Phone Number">

                <select id="editRole" required>
                    <option value="EMPLOYEE">Employee</option>
                    <option value="HELPDESK">Helpdesk</option>
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
    async function loadStaff() {

        const container = document.getElementById("staffTableContainer");

        try {

            allStaff = await apiRequest(API + "/staff");

            filterStaff();

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


            const updatedStaff = {

                employeeName: document.getElementById("editEmployeeName").value.trim(),

                department: document.getElementById("editDepartment").value.trim(),

                designation: document.getElementById("editDesignation").value.trim(),

                phone: document.getElementById("editPhone").value.trim(),

                role: document.getElementById("editRole").value

            };


            try {

                await apiRequest(API + "/staff/" + employeeId, {

                    method: "PUT",

                    body: JSON.stringify(updatedStaff)

                });


                showMessage(
                    "editMessage",
                    "Staff details updated successfully!",
                    "success"
                );


                await loadStaff();


            } catch (error) {

                showMessage(
                    "editMessage",
                    error.message,
                    "error"
                );

            }

        });


    // LOAD STAFF WHEN DASHBOARD OPENS
    window.addEventListener("DOMContentLoaded", loadStaff);

</script>

</body>
</html>

