
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.dto.StaffCreateRequest;
import com.epdcl.webdisplay.dto.StaffResponseDto;
import com.epdcl.webdisplay.dto.StaffUpdateRequest;
import com.epdcl.webdisplay.entity.Employee;
import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.service.impl.EmployeeService;
import com.epdcl.webdisplay.service.impl.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final String MANAGER_DESIGNATION = "HELPDESK_MANAGER";

    private final UserService userService;
    private final EmployeeService employeeService;

    public AdminController(
            UserService userService,
            EmployeeService employeeService) {

        this.userService = userService;
        this.employeeService = employeeService;
    }

    private boolean isValidEmployeeType(String employeeType) {
        return employeeType != null
                && (employeeType.trim().equalsIgnoreCase("PRIVATE")
                || employeeType.trim().equalsIgnoreCase("DEPARTMENT"));
    }

    private String normalizeEmployeeType(String employeeType) {
        return employeeType.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isHelpdeskManager(Employee employee) {
        return employee.getUser() != null
                && employee.getUser().getRole() == Role.HELPDESK
                && MANAGER_DESIGNATION.equalsIgnoreCase(
                employee.getDesignation() == null
                        ? ""
                        : employee.getDesignation().trim());
    }

    private boolean managerAlreadyExists(Long excludedEmployeeId) {
        return employeeService.getAllEmployees()
                .stream()
                .anyMatch(employee ->
                        !employee.getEmployeeId().equals(excludedEmployeeId)
                                && isHelpdeskManager(employee));
    }

    @PostMapping("/staff")
    @Transactional
    public ResponseEntity<?> createStaff(
            @RequestBody StaffCreateRequest request) {

        if (request.getUsername() == null
                || request.getUsername().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()
                || request.getEmployeeName() == null
                || request.getEmployeeName().isBlank()
                || request.getRole() == null
                || !isValidEmployeeType(request.getEmployeeType())) {

            return ResponseEntity.badRequest()
                    .body("Username, password, employee name, role and valid employee type (PRIVATE or DEPARTMENT) are required.");
        }

        if (request.getRole() != Role.EMPLOYEE
                && request.getRole() != Role.HELPDESK
                && request.getRole() != Role.BANK_MANAGER) {

            return ResponseEntity.badRequest()
                    .body("Only EMPLOYEE, HELPDESK and BANK_MANAGER accounts can be created.");
        }

        boolean requestingManager =
                request.getRole() == Role.HELPDESK
                        && MANAGER_DESIGNATION.equalsIgnoreCase(
                        request.getDesignation() == null
                                ? ""
                                : request.getDesignation().trim());

        if (requestingManager && managerAlreadyExists(null)) {
            return ResponseEntity.badRequest()
                    .body("A Helpdesk Manager already exists. Only one manager is allowed.");
        }

        User user = userService.createStaffLogin(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );

        String designation = request.getDesignation();

        if (request.getRole() == Role.HELPDESK
                && (designation == null || designation.isBlank())) {
            designation = "HELPDESK_EMPLOYEE";
        }

        Employee employee = employeeService.createEmployeeProfile(
                user,
                request.getEmployeeName(),
                request.getDepartment(),
                designation,
                request.getPhone(),
                normalizeEmployeeType(request.getEmployeeType())
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new StaffResponseDto(employee));
    }

    @GetMapping("/staff")
    public ResponseEntity<List<StaffResponseDto>> getAllStaff() {

        List<StaffResponseDto> staff = employeeService.getAllEmployees()
                .stream()
                .map(StaffResponseDto::new)
                .toList();

        return ResponseEntity.ok(staff);
    }

    @PutMapping("/staff/{employeeId}")
    @Transactional
    public ResponseEntity<?> updateStaff(
            @PathVariable Long employeeId,
            @RequestBody StaffUpdateRequest request) {

        if (request.getRole() != Role.EMPLOYEE
                && request.getRole() != Role.HELPDESK
                && request.getRole() != Role.BANK_MANAGER) {

            return ResponseEntity.badRequest()
                    .body("Only EMPLOYEE, HELPDESK and BANK_MANAGER accounts can be created.");
        }

        if (request.getEmployeeName() == null
                || request.getEmployeeName().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Employee name is required.");
        }

        if (request.getEmployeeType() != null
                && !isValidEmployeeType(request.getEmployeeType())) {

            return ResponseEntity.badRequest()
                    .body("Employee type must be PRIVATE or DEPARTMENT.");
        }

        Employee existingEmployee = employeeService.getAllEmployees()
                .stream()
                .filter(employee ->
                        employee.getEmployeeId().equals(employeeId))
                .findFirst()
                .orElse(null);

        if (existingEmployee == null) {
            return ResponseEntity.notFound().build();
        }

        Role resultingRole = request.getRole() != null
                ? request.getRole()
                : existingEmployee.getUser().getRole();

        String resultingDesignation = request.getDesignation() != null
                ? request.getDesignation()
                : existingEmployee.getDesignation();

        boolean requestingManager =
                resultingRole == Role.HELPDESK
                        && MANAGER_DESIGNATION.equalsIgnoreCase(
                        resultingDesignation == null
                                ? ""
                                : resultingDesignation.trim());

        if (requestingManager && managerAlreadyExists(employeeId)) {
            return ResponseEntity.badRequest()
                    .body("Another Helpdesk Manager already exists.");
        }

        String employeeType = request.getEmployeeType() != null
                ? normalizeEmployeeType(request.getEmployeeType())
                : null;

        Employee employee = employeeService.updateEmployee(
                employeeId,
                request.getEmployeeName(),
                request.getDepartment(),
                request.getDesignation(),
                request.getPhone(),
                employeeType
        );

        if (request.getRole() != null) {
            User user = employee.getUser();
            user.setRole(request.getRole());
        }

        return ResponseEntity.ok(new StaffResponseDto(employee));
    }
}
