package com.revworkforce.repository;

import com.revworkforce.entity.Employee;
import com.revworkforce.entity.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    List<LeaveApplication> findByEmployee_EmpId(Long empId);

    List<LeaveApplication> findByEmployee_Manager_EmpId(Long managerId);

    void deleteByEmployee(Employee employee);
}