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
    CommandLineRunner initAdmin(UserRepository userRepository,
                                EmployeeRepository employeeRepository,
                                DepartmentRepository departmentRepository,
                                PasswordEncoder passwordEncoder) {
        return args -> {

            if (userRepository.findByEmail("admin@rev.com").isEmpty()) {

                Department department = departmentRepository
                        .findByDepartmentName("Administration")
                        .orElseGet(() -> departmentRepository.save(
                                Department.builder()
                                        .departmentName("Administration")
                                        .build()
                        ));

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
                                .department(department)
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