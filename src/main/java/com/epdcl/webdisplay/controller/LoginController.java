package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.service.impl.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        User user = userService.authenticateUser(username, password);

        if (user != null) {

            session.setAttribute("loggedInUser", user);
            session.setAttribute("role", user.getRole().name());

            if (user.getRole() == Role.ADMIN) {
                return "redirect:/admin/dashboard";
            }

            if (user.getRole() == Role.EMPLOYEE) {
                return "redirect:/employee/dashboard";
            }

            if (user.getRole() == Role.CUSTOMER) {
                return "redirect:/customer/dashboard";
            }

            if (user.getRole() == Role.HELPDESK) {
                return "redirect:/helpdesk/dashboard";
            }
        }

        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}