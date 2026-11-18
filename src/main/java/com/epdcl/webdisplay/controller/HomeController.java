
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.entity.User;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        // Show the public bank homepage to visitors.
        if (user == null) {
            return "bank-home";
        }

        // Redirect authenticated users to their respective portals.
        Role role = user.getRole();

        if (role == null) {
            session.invalidate();
            return "redirect:/login";
        }

        return switch (role) {
            case ADMIN -> "redirect:/admin/dashboard";
            case EMPLOYEE -> "redirect:/employee/dashboard";
            case CUSTOMER -> "redirect:/customer/dashboard";
            case HELPDESK -> "redirect:/helpdesk/dashboard";
            case BANK_MANAGER -> "redirect:/bank-manager/dashboard";
        };
    }
}
