package com.revworkforce.service;

import com.revworkforce.dto.CreateEmployeeRequest;
import com.revworkforce.entity.*;
import com.revworkforce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

        private final UserRepository userRepository;
        private final EmployeeRepository employeeRepository;
        private final DepartmentRepository departmentRepository;
        private final LeaveTypeRepository leaveTypeRepository;
        private final LeaveBalanceRepository leaveBalanceRepository;
        private final LeaveApplicationRepository leaveApplicationRepository;
        private final GoalRepository goalRepository;
        private final PerformanceReviewRepository performanceReviewRepository;
        private final NotificationRepository notificationRepository;
        private final AnnouncementRepository announcementRepository;
        private final PasswordEncoder passwordEncoder;

        @Transactional
        public void createEmployee(CreateEmployeeRequest request) {
                log.info("Admin initiating employee creation for email: {}", request.getEmail());

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
                log.info("Employee successfully created with ID: {} and User Email: {}", savedEmployee.getEmpId(),
                                request.getEmail());
        }

        @Transactional
        public void deleteEmployee(Long empId) {
                log.warn("Admin initiating hard delete for employee ID: {}", empId);
                Employee employee = employeeRepository.findById(empId)
                                .orElseThrow(() -> {
                                        log.error("Failed to delete employee: ID {} not found", empId);
                                        return new RuntimeException("Employee not found");
                                });
                leaveBalanceRepository.deleteByEmployee(employee);
                leaveApplicationRepository.deleteByEmployee(employee);
                goalRepository.deleteByEmployee(employee);
                performanceReviewRepository.deleteByEmployee(employee);
                notificationRepository.deleteByUser(employee.getUser());
                employeeRepository.delete(employee);
                userRepository.delete(employee.getUser());
                log.info("Successfully deleted employee ID: {} and associated user", empId);
        }

        @Transactional
        public void toggleStatus(Long empId) {
                Employee employee = employeeRepository.findById(empId)
                                .orElseThrow(() -> new RuntimeException("Employee not found"));

                if ("ACTIVE".equals(employee.getStatus())) {
                        employee.setStatus("INACTIVE");
                        employee.getUser().setActive(false);
                } else {
                        employee.setStatus("ACTIVE");
                        employee.getUser().setActive(true);
                }

                employeeRepository.save(employee);
                userRepository.save(employee.getUser());
                log.info("Status for employee ID {} toggled to: {}", empId, employee.getStatus());
        }

        @Transactional
        public void updateEmployeeManager(Long empId, Long managerId) {
                Employee employee = employeeRepository.findById(empId)
                                .orElseThrow(() -> new RuntimeException("Employee not found"));

                Employee manager = employeeRepository.findById(managerId)
                                .orElseThrow(() -> new RuntimeException("Manager not found"));

                employee.setManager(manager);
                employeeRepository.save(employee);
        }

        @Transactional
        public void saveAnnouncement(Announcement announcement, String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Employee employee = employeeRepository.findByUser(user);

                announcement.setCreatedBy(employee);
                // LocalDateTime is imported in Service? Let's check... AdminService imports
                // import java.time.LocalDateTime; No, wait. I should add import.
                announcement.setCreatedAt(java.time.LocalDateTime.now());

                announcementRepository.save(announcement);
        }

        public List<LeaveApplication> getAllPendingLeaves() {
                return leaveApplicationRepository.findAll().stream()
                                .filter(leave -> "PENDING".equals(leave.getStatus()))
                                .collect(java.util.stream.Collectors.toList());
        }

        @Transactional
        public void approveLeaveAdmin(Long leaveId, String adminEmail) {
                LeaveApplication leave = leaveApplicationRepository.findById(leaveId)
                                .orElseThrow(() -> new RuntimeException("Leave application not found"));

                if (!"PENDING".equals(leave.getStatus())) {
                        throw new RuntimeException("Leave is not in PENDING state");
                }

                User user = userRepository.findByEmail(adminEmail)
                                .orElseThrow(() -> new RuntimeException("Admin user not found"));
                Employee admin = employeeRepository.findByUser(user);

                long leaveDays = java.time.temporal.ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate())
                                + 1;
                if (leaveDays <= 0) {
                        throw new RuntimeException("End date must be after or same as start date");
                }

                LeaveBalance balance = leaveBalanceRepository
                                .findByEmployeeAndLeaveType(leave.getEmployee(), leave.getLeaveType())
                                .orElseThrow(() -> new RuntimeException("Leave balance record not found"));

                if (balance.getBalanceDays() < leaveDays) {
                        throw new RuntimeException("Insufficient leave balance");
                }

                balance.setBalanceDays((int) (balance.getBalanceDays() - leaveDays));
                leaveBalanceRepository.save(balance);

                leave.setStatus("APPROVED");
                leave.setApprovedBy(admin);
                leaveApplicationRepository.save(leave);
                log.info("Admin {} approved leave ID: {} for employee: {}", adminEmail, leaveId,
                                leave.getEmployee().getFirstName());

                Notification notification = Notification.builder()
                                .user(leave.getEmployee().getUser())
                                .message("Your leave request for " + leave.getLeaveType().getLeaveName()
                                                + " has been approved by the Admin.")
                                .isRead(false)
                                .createdAt(java.time.LocalDateTime.now())
                                .build();
                notificationRepository.save(notification);
        }

        @Transactional
        public void rejectLeaveAdmin(Long leaveId, String adminComment, String adminEmail) {
                LeaveApplication leave = leaveApplicationRepository.findById(leaveId)
                                .orElseThrow(() -> new RuntimeException("Leave application not found"));

                if (!"PENDING".equals(leave.getStatus())) {
                        throw new RuntimeException("Leave is not in PENDING state");
                }

                User user = userRepository.findByEmail(adminEmail)
                                .orElseThrow(() -> new RuntimeException("Admin user not found"));
                Employee admin = employeeRepository.findByUser(user);

                leave.setStatus("REJECTED");
                leave.setApprovedBy(admin);
                leave.setManagerComment(adminComment);
                leaveApplicationRepository.save(leave);
                log.info("Admin {} rejected leave ID: {} for employee: {}. Reason: {}", adminEmail, leaveId,
                                leave.getEmployee().getFirstName(), adminComment);

                Notification notification = Notification.builder()
                                .user(leave.getEmployee().getUser())
                                .message("Your leave request for " + leave.getLeaveType().getLeaveName()
                                                + " has been rejected by the Admin. Comment: " + adminComment)
                                .isRead(false)
                                .createdAt(java.time.LocalDateTime.now())
                                .build();
                notificationRepository.save(notification);
        }
}