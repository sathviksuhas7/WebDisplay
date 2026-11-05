
package com.epdcl.webdisplay.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session, HttpServletResponse response) {

        // Prevent browser from caching the account page
        response.setHeader("Cache-Control",
                "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // Check login session
        if (session.getAttribute("loggedInUser") == null) {
            session.invalidate();
            return "redirect:/login";
        }

        return "Account";
    }
}
