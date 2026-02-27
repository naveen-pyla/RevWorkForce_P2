package com.revworkforce.repository;

import com.revworkforce.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByManager_EmpId(Long managerId);

    @Query("SELECT e FROM Employee e WHERE e.user.role = 'MANAGER'")
    List<Employee> findAllManagers();

    @Query("SELECT e FROM Employee e WHERE e.user.role = 'ADMIN'")
    List<Employee> findAllAdmins();}