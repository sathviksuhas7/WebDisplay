package com.epdcl.webdisplay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard() {
        return "employee-dashboard";
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard() {
        return "customer-dashboard";
    }

    @GetMapping("/helpdesk/dashboard")
    public String helpdeskDashboard() {
        return "helpdesk-dashboard";
    }
}