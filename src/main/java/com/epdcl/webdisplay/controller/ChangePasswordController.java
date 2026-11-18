
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.repo.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChangePasswordController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/change-password")
    public String showChangePasswordPage(HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        if (!user.isMustChangePassword()) {
            return "redirect:/customer/dashboard";
        }

        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            Model model) {

        User sessionUser = (User) session.getAttribute("loggedInUser");

        if (sessionUser == null) {
            return "redirect:/login";
        }

        User user = userRepository.findById(sessionUser.getId())
                .orElse(null);

        if (user == null) {
            session.invalidate();
            return "redirect:/login";
        }

        if (!user.isMustChangePassword()) {
            return "redirect:/customer/dashboard";
        }

        if (currentPassword == null
                || currentPassword.isBlank()
                || !passwordEncoder.matches(
                currentPassword, user.getPassword())) {

            model.addAttribute("error",
                    "Your current password is incorrect.");

            return "change-password";
        }

        if (newPassword == null || newPassword.length() < 8) {
            model.addAttribute("error",
                    "New password must contain at least 8 characters.");

            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error",
                    "New password and confirm password do not match.");

            return "change-password";
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            model.addAttribute("error",
                    "Your new password must be different from your current password.");

            return "change-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);

        User updatedUser = userRepository.save(user);

        // Refresh the authenticated user in the session
        session.setAttribute("loggedInUser", updatedUser);

        model.addAttribute("success",
                "Password changed successfully.");

        return "redirect:/customer/dashboard";
    }
}
