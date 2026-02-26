package com.revworkforce.repository;

import com.revworkforce.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByManager_EmpId(Long managerId);
}