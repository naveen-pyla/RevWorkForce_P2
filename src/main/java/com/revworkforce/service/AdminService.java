package com.revworkforce.service;

import com.revworkforce.dto.CreateEmployeeRequest;
import com.revworkforce.entity.*;
import com.revworkforce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createEmployee(CreateEmployeeRequest request) {

        // 1️⃣ Create User
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode("welcome123"))
                .role(request.getRole())
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // 2️⃣ Fetch Department
        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // 3️⃣ Create Employee
        Employee employee = Employee.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .department(department)
                .designation(request.getDesignation())
                .salary(request.getSalary())
                .phone(request.getPhone())
                .address(request.getAddress())
                .joiningDate(request.getJoiningDate())
                .status("ACTIVE")
                .build();

        // Assign manager if exists
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository
                    .findById(request.getManagerId())
                    .orElse(null);
            employee.setManager(manager);
        }

        employee = employeeRepository.save(employee);
        final Employee savedEmployee = employee;

        // 4️⃣ Initialize Leave Balance (skip ADMIN)
        if (!request.getRole().equals("ADMIN")) {

            leaveTypeRepository.findAll().forEach(type -> {

                LeaveBalance lb = LeaveBalance.builder()
                        .employee(savedEmployee)
                        .leaveType(type)
                        .balanceDays(type.getMaxPerYear())
                        .build();

                leaveBalanceRepository.save(lb);
            });
        }
    }
}