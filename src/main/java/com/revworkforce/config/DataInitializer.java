package com.revworkforce.config;

import com.revworkforce.entity.*;
import com.revworkforce.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository,
                               EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               LeaveTypeRepository leaveTypeRepository,
                               PasswordEncoder passwordEncoder) {

        return args -> {

            /*
             * ---------------------------------------------------
             * 1️⃣ Ensure Default Department Exists
             * ---------------------------------------------------
             */
            Department adminDept = departmentRepository
                    .findByDepartmentName("Administration")
                    .orElseGet(() -> departmentRepository.save(
                            Department.builder()
                                    .departmentName("Administration")
                                    .build()
                    ));

            /*
             * ---------------------------------------------------
             * 2️⃣ Ensure Leave Types Exist
             * ---------------------------------------------------
             */
            if (leaveTypeRepository.count() == 0) {

                LeaveType casual = LeaveType.builder()
                        .leaveName("Casual Leave")
                        .maxPerYear(10)
                        .build();

                LeaveType sick = LeaveType.builder()
                        .leaveName("Sick Leave")
                        .maxPerYear(8)
                        .build();

                leaveTypeRepository.save(casual);
                leaveTypeRepository.save(sick);

                System.out.println("✅ Default Leave Types initialized");
            }

            /*
             * ---------------------------------------------------
             * 3️⃣ Ensure Default ADMIN Exists
             * ---------------------------------------------------
             */
            if (userRepository.findByEmail("admin@rev.com").isEmpty()) {

                User adminUser = userRepository.save(
                        User.builder()
                                .email("admin@rev.com")
                                .passwordHash(passwordEncoder.encode("admin123"))
                                .role("ADMIN")
                                .isActive(true)
                                .createdAt(LocalDateTime.now())
                                .build()
                );

                employeeRepository.save(
                        Employee.builder()
                                .user(adminUser)
                                .firstName("System")
                                .lastName("Admin")
                                .department(adminDept)
                                .designation("Administrator")
                                .joiningDate(LocalDate.now())
                                .salary(0.0)
                                .status("ACTIVE")
                                .manager(null)
                                .build()
                );

                System.out.println("✅ Default ADMIN initialized");
            }
        };
    }
}