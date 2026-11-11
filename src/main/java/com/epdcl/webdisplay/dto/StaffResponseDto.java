
package com.epdcl.webdisplay.dto;

import com.epdcl.webdisplay.entity.Employee;
import com.epdcl.webdisplay.entity.Role;

public class StaffResponseDto {

    private Long employeeId;
    private Long userId;
    private String username;
    private String email;
    private Role role;
    private String employeeName;
    private String department;
    private String designation;
    private String phone;

    public StaffResponseDto(Employee employee) {

        this.employeeId = employee.getEmployeeId();

        if (employee.getUser() != null) {
            this.userId = employee.getUser().getId();
            this.username = employee.getUser().getUsername();
            this.email = employee.getUser().getEmail();
            this.role = employee.getUser().getRole();
        }

        this.employeeName = employee.getEmployeeName();
        this.department = employee.getDepartment();
        this.designation = employee.getDesignation();
        this.phone = employee.getPhone();
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getDepartment() {
        return department;
    }

    public String getDesignation() {
        return designation;
    }

    public String getPhone() {
        return phone;
    }
}
