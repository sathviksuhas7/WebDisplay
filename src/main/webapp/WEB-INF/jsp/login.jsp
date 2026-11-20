<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>MyBank - Login</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f9;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
        }

        .login-container {
            background: white;
            padding: 35px;
            width: 350px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
        }

        h2 {
            text-align: center;
            color: #1e293b;
        }

        input { width:100%; padding:12px; margin:10px 0; box-sizing:border-box; border:1px solid #ccc; border-radius:5px; }

        button {
            width: 100%;
            padding: 12px;
            background: #2563eb;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }

        button:hover {
            background: #1d4ed8;
        }

        .message {
            text-align: center;
            margin-top: 15px;
        }

        .error {
            color: red;
        }

        .success {
            color: green;
        }

        .message.error, .message.success, .message.info {
            padding: 10px 12px;
            border-radius: 6px;
        }
        .message.error { background: #fef2f2; color: #b91c1c; }
        .message.success { background: #f0fdf4; color: #15803d; }
        .message.info { background: #eff6ff; color: #1d4ed8; }
    </style>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/site-theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/numeric-inputs.css">
    <script src="${pageContext.request.contextPath}/js/site-theme.js" data-context-path="${pageContext.request.contextPath}"></script>
    <script src="${pageContext.request.contextPath}/js/numeric-inputs.js" defer></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/password-toggle.css">
    <script src="${pageContext.request.contextPath}/js/password-toggle.js" defer></script>
</head>

<body>

<div class="login-container">

    <h2>MyBank Login</h2>

    <form action="${pageContext.request.contextPath}/login"
          method="post">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="text"
               name="username"
               placeholder="Enter Username"
               autocomplete="username"
               autofocus
               required>

        <input type="password" id="loginPassword" name="password"
               placeholder="Enter Password" autocomplete="current-password" required>

        <button type="submit">Login</button>

    </form>

    <div class="message error" role="alert" ${empty error ? 'hidden' : ''}>${error}</div>
    <div class="message success" role="status" ${param.logout == null ? 'hidden' : ''}>You have been logged out successfully.</div>
    <div class="message info" role="status" ${param.expired == null || param.logout != null || not empty error ? 'hidden' : ''}>Your session has ended. Please log in again.</div>

</div>

</body>
</html>