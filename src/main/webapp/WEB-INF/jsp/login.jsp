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

        input {
            width: 100%;
            padding: 12px;
            margin: 10px 0;
            box-sizing: border-box;
            border: 1px solid #ccc;
            border-radius: 5px;
        }

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
    </style>
</head>

<body>

<div class="login-container">

    <h2>MyBank Login</h2>

    <form action="${pageContext.request.contextPath}/login"
          method="post">

        <input type="text"
               name="username"
               placeholder="Enter Username"
               required>

        <input type="password"
               name="password"
               placeholder="Enter Password"
               required>

        <button type="submit">Login</button>

    </form>

    <div class="message error">
        ${error}
    </div>

    <div class="message success">
        ${param.registered != null ? 'Registration successful! Please login.' : ''}
    </div>

    <div class="message success">
        ${param.logout != null ? 'You have been logged out successfully!' : ''}
    </div>

    <p style="text-align:center;">
        Don't have an account?
        <a href="${pageContext.request.contextPath}/register">
            Register here
        </a>
    </p>

</div>

</body>
</html>