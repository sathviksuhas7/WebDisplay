
package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.entity.Employee;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.repo.EmployeeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee createEmployeeProfile(
            User user,
            String employeeName,
            String department,
            String designation,
            String phone) {

        Employee employee = new Employee();

        employee.setUser(user);
        employee.setEmployeeName(employeeName);
        employee.setDepartment(department);
        employee.setDesignation(designation);
        employee.setPhone(phone);

        return employeeRepository.save(employee);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    @Transactional
    public Employee updateEmployee(
            Long employeeId,
            String employeeName,
            String department,
            String designation,
            String phone) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Staff member not found"));

        employee.setEmployeeName(employeeName);
        employee.setDepartment(department);
        employee.setDesignation(designation);
        employee.setPhone(phone);

        return employeeRepository.save(employee);
    }
}
