package com.revworkforce.service;

import com.revworkforce.dto.CreateEmployeeRequest;
import com.revworkforce.entity.*;
import com.revworkforce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
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

    @Transactional
    public void deleteEmployee(Long empId) {

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // 1️⃣ Delete related data
        leaveBalanceRepository.deleteByEmployee(employee);
        leaveApplicationRepository.deleteByEmployee(employee);
        goalRepository.deleteByEmployee(employee);
        performanceReviewRepository.deleteByEmployee(employee);
        notificationRepository.deleteByUser(employee.getUser());
        // 2️⃣ Delete employee
        employeeRepository.delete(employee);

        // 3️⃣ Delete user
        userRepository.delete(employee.getUser());
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

        long leaveDays = java.time.temporal.ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
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