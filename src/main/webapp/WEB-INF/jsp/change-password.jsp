
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Change Password</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
            font-family: Arial, sans-serif;
            background: #f3f5f9;
        }

        .container {
            width: 100%;
            max-width: 420px;
            padding: 32px;
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        }

        h2 {
            margin-top: 0;
            margin-bottom: 10px;
            color: #1e293b;
        }

        .description {
            margin-bottom: 24px;
            color: #64748b;
            font-size: 14px;
            line-height: 1.5;
        }

        label {
            display: block;
            margin: 15px 0 7px;
            color: #334155;
            font-size: 14px;
            font-weight: 600;
        }

        input {
            width: 100%;
            padding: 12px;
            border: 1px solid #cbd5e1;
            border-radius: 6px;
            font-size: 14px;
        }

        input:focus {
            outline: none;
            border-color: #2563eb;
        }

        button {
            width: 100%;
            margin-top: 24px;
            padding: 13px;
            border: none;
            border-radius: 6px;
            background: #2563eb;
            color: white;
            font-size: 15px;
            font-weight: 600;
            cursor: pointer;
        }

        button:hover {
            background: #1d4ed8;
        }

        .error {
            margin: 15px 0;
            padding: 11px;
            border-radius: 6px;
            background: #fee2e2;
            color: #b91c1c;
            font-size: 14px;
        }

        .success {
            margin: 15px 0;
            padding: 11px;
            border-radius: 6px;
            background: #dcfce7;
            color: #166534;
            font-size: 14px;
        }

        .note {
            margin-top: 15px;
            color: #64748b;
            font-size: 12px;
        }
    </style>
</head>

<body>

<div class="container">

    <h2>Change Your Password</h2>

    <p class="description">
        For security reasons, you must change your initial password
        before accessing your customer account.
    </p>

    <% if (request.getAttribute("error") != null) { %>
        <div class="error">
            <%= request.getAttribute("error") %>
        </div>
    <% } %>

    <% if (request.getAttribute("success") != null) { %>
        <div class="success">
            <%= request.getAttribute("success") %>
        </div>
    <% } %>

    <form method="POST"
          action="${pageContext.request.contextPath}/change-password">

        <input type="hidden"
               name="csrfToken"
               value="${sessionScope.csrfToken}">

        <label for="currentPassword">Initial Password</label>
        <input type="password"
               id="currentPassword"
               name="currentPassword"
               autocomplete="current-password"
               required>

        <label for="newPassword">New Password</label>
        <input type="password"
               id="newPassword"
               name="newPassword"
               minlength="8"
               autocomplete="new-password"
               required>

        <label for="confirmPassword">Confirm New Password</label>
        <input type="password"
               id="confirmPassword"
               name="confirmPassword"
               minlength="8"
               autocomplete="new-password"
               required>

        <button type="submit">Change Password</button>

        <p class="note">
            Your new password must contain at least 8 characters.
        </p>

    </form>

</div>

</body>
</html>
