package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import com.epdcl.webdisplay.entity.Employee;
import java.util.Optional;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUserId(Long userId);
}