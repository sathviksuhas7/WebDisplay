
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MyBank | Banking Made Simple</title>

    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: Arial, sans-serif;
            color: #172554;
            background: #f8fafc;
        }

        header {
            height: 76px;
            padding: 0 8%;
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: white;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
        }

        .logo {
            font-size: 25px;
            font-weight: bold;
            color: #123b72;
        }

        nav {
            display: flex;
            align-items: center;
            gap: 28px;
        }

        nav a {
            text-decoration: none;
            color: #334155;
            font-size: 14px;
        }

        .login-btn {
            background: #123b72;
            color: white;
            padding: 12px 25px;
            border-radius: 6px;
            text-decoration: none;
            font-weight: bold;
        }

        .hero {
            min-height: 520px;
            padding: 90px 10%;
            display: flex;
            align-items: center;
            background: linear-gradient(120deg, #102f59, #1d5b96);
            color: white;
        }

        .hero-content {
            max-width: 650px;
        }

        .hero h1 {
            font-size: 48px;
            line-height: 1.2;
            margin-bottom: 22px;
        }

        .hero p {
            font-size: 17px;
            line-height: 1.8;
            color: #dbeafe;
            margin-bottom: 32px;
        }

        .hero-btn {
            display: inline-block;
            background: white;
            color: #123b72;
            padding: 15px 30px;
            border-radius: 6px;
            text-decoration: none;
            font-weight: bold;
        }

        .section {
            padding: 65px 8%;
            text-align: center;
        }

        .section h2 {
            font-size: 30px;
            margin-bottom: 14px;
        }

        .section-description {
            color: #64748b;
            margin-bottom: 40px;
        }

        .features {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 25px;
        }

        .feature {
            background: white;
            padding: 32px 25px;
            border-radius: 10px;
            box-shadow: 0 3px 15px rgba(0,0,0,0.05);
        }

        .feature h3 {
            color: #123b72;
            margin-bottom: 14px;
        }

        .feature p {
            color: #64748b;
            line-height: 1.7;
            font-size: 14px;
        }

        .cta {
            padding: 55px 8%;
            text-align: center;
            background: #eaf2fb;
        }

        .cta h2 {
            margin-bottom: 15px;
        }

        .cta p {
            color: #64748b;
            margin-bottom: 25px;
        }

        footer {
            padding: 25px;
            text-align: center;
            background: #102f59;
            color: #dbeafe;
            font-size: 13px;
        }

        @media (max-width: 750px) {
            header {
                padding: 0 5%;
            }

            nav a:not(.login-btn) {
                display: none;
            }

            .hero {
                padding: 75px 7%;
            }

            .hero h1 {
                font-size: 36px;
            }

            .features {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>

<body>

<header>
    <div class="logo">MyBank</div>

    <nav>
        <a href="#home">Home</a>
        <a href="#services">Services</a>
        <a href="#about">About</a>
        <a class="login-btn"
           href="${pageContext.request.contextPath}/login">
            Login
        </a>
    </nav>
</header>

<section class="hero" id="home">
    <div class="hero-content">
        <h1>Banking That Moves With You</h1>

        <p>
            Manage your money, transfer funds, track transactions
            and access banking services through one secure platform.
        </p>

        <a class="hero-btn"
           href="${pageContext.request.contextPath}/login">
            Access Your Account
        </a>
    </div>
</section>

<section class="section" id="services">
    <h2>Banking Services</h2>

    <p class="section-description">
        Everything you need to manage your banking in one place.
    </p>

    <div class="features">
        <div class="feature">
            <h3>Account Management</h3>
            <p>
                View your account details, check balances
                and manage your banking information.
            </p>
        </div>

        <div class="feature">
            <h3>Secure Transfers</h3>
            <p>
                Transfer money between accounts and
                verify recipient details before transactions.
            </p>
        </div>

        <div class="feature">
            <h3>Customer Support</h3>
            <p>
                Raise support requests, communicate with
                our helpdesk and track ticket progress.
            </p>
        </div>
    </div>
</section>

<section class="cta" id="about">
    <h2>Your Banking, One Secure Platform</h2>

    <p>
        Sign in to access your personalised banking portal.
    </p>

    <a class="login-btn"
       href="${pageContext.request.contextPath}/login">
        Login to MyBank
    </a>
</section>

<footer>
    © 2026 MyBank. All rights reserved.
</footer>

</body>
</html>
