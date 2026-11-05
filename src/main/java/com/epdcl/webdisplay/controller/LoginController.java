package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.entity.User;
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

    // Display login page
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // Display registration page
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    // Register new user
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {

        try {
            userService.registerUser(user);
            return "redirect:/login?registered=true";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // Login user
    @PostMapping("/login")
    public String loginUser(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        User user = userService.authenticateUser(username, password);

        if (user != null) {
            session.setAttribute("loggedInUser", user);
            return "redirect:/";
        }

        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}
