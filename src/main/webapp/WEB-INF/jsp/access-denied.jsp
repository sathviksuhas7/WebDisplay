<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Access Denied | WebDisplay</title>
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; min-height: 100vh; display: grid; place-items: center;
               padding: 24px; font-family: Arial, sans-serif; background: #f4f6f9; color: #1f2937; }
        .card { width: min(100%, 460px); padding: 34px; background: #fff; border-radius: 12px;
                box-shadow: 0 8px 30px rgba(15,23,42,.10); text-align: center; }
        .code { font-size: 46px; font-weight: 700; color: #dc2626; margin: 0 0 8px; }
        h1 { margin: 0 0 12px; font-size: 24px; }
        p { color: #64748b; line-height: 1.6; }
        a { display: inline-block; margin-top: 16px; padding: 11px 20px; border-radius: 7px;
            background: #1d4ed8; color: white; text-decoration: none; }
    </style>
</head>
<body>
<div class="card">
    <p class="code">403</p>
    <h1>Access Denied</h1>
    <p>${empty accessDeniedMessage ? 'You do not have permission to view this page.' : accessDeniedMessage}</p>
    <a href="${pageContext.request.contextPath}/">Return to Home</a>
</div>
</body>
</html>
