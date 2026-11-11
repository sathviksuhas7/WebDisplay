package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.repo.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User findByUsername(String username) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        return userRepository.findByUsername(username.trim())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // AUTHENTICATE USER
    @Override
    public User authenticateUser(String username, String password) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return null;
        }

        User user = userRepository.findByUsername(username.trim())
                .orElse(null);

        if (user == null || user.getPassword() == null) {
            return null;
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }

        return null;
    }

    // CREATE CUSTOMER LOGIN
    @Override
    public User createCustomerLogin(
            String username,
            String email,
            String password) {

        validateUserDetails(username, password);

        if (userRepository.existsByUsername(username.trim())) {
            throw new RuntimeException("Username already exists");
        }

        if (email != null && !email.isBlank()
                && userRepository.existsByEmail(email.trim())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();

        user.setUsername(username.trim());
        user.setEmail(email == null || email.isBlank()
                ? null : email.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.CUSTOMER);

        return userRepository.save(user);
    }

    // CREATE EMPLOYEE OR HELPDESK LOGIN
    @Override
    public User createStaffLogin(
            String username,
            String email,
            String password,
            Role role) {

        validateUserDetails(username, password);

        if (role != Role.EMPLOYEE && role != Role.HELPDESK) {
            throw new RuntimeException(
                    "Only EMPLOYEE and HELPDESK roles can be created by Admin."
            );
        }

        if (userRepository.existsByUsername(username.trim())) {
            throw new RuntimeException("Username already exists");
        }

        if (email != null && !email.isBlank()
                && userRepository.existsByEmail(email.trim())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();

        user.setUsername(username.trim());
        user.setEmail(email == null || email.isBlank()
                ? null : email.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return userRepository.save(user);
    }

    // COMMON VALIDATION
    private void validateUserDetails(String username, String password) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters");
        }
    }
}