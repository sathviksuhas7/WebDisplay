package com.epdcl.webdisplay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.repo.EmployeeRepository;

@Controller
public class DashboardController {

    private final EmployeeRepository employeeRepository;

    public DashboardController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard() {
        return "employee-dashboard";
    }

    @GetMapping("/bank-manager/dashboard")
    public String bankManagerDashboard() {
        return "bank-manager-dashboard";
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard() {
        return "customer-dashboard";
    }

    @GetMapping("/helpdesk/dashboard")
    public String helpdeskDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        boolean manager = user != null && employeeRepository.findByUserId(user.getId())
                .map(employee -> "HELPDESK_MANAGER".equalsIgnoreCase(employee.getDesignation()))
                .orElse(false);
        model.addAttribute("isHelpdeskManager", manager);
        return "helpdesk-dashboard";
    }
}