package com.revworkforce.repository;

import com.revworkforce.entity.Employee;
import com.revworkforce.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByEmployee_EmpIdAndLeaveType_LeaveTypeId(Long empId, Long leaveTypeId);

    void deleteByEmployee(Employee employee);
}