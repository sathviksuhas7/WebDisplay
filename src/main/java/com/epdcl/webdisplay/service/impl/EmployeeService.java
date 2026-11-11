
package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.entity.Employee;
import com.epdcl.webdisplay.entity.User;

import java.util.List;

public interface EmployeeService {

    Employee createEmployeeProfile(
            User user,
            String employeeName,
            String department,
            String designation,
            String phone
    );

    List<Employee> getAllEmployees();

    Employee updateEmployee(
            Long employeeId,
            String employeeName,
            String department,
            String designation,
            String phone
    );
}
