package com.revworkforce.service;

import com.revworkforce.entity.Employee;
import com.revworkforce.entity.Goal;
import com.revworkforce.entity.LeaveApplication;
import com.revworkforce.entity.LeaveBalance;
import com.revworkforce.entity.Notification;
import com.revworkforce.entity.User;
import com.revworkforce.repository.EmployeeRepository;
import com.revworkforce.repository.GoalRepository;
import com.revworkforce.repository.LeaveApplicationRepository;
import com.revworkforce.repository.LeaveBalanceRepository;
import com.revworkforce.repository.NotificationRepository;
import com.revworkforce.entity.PerformanceReview;
import com.revworkforce.repository.PerformanceReviewRepository;
import com.revworkforce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final NotificationRepository notificationRepository;
    private final GoalRepository goalRepository;
    private final PerformanceReviewRepository performanceReviewRepository;

    public List<LeaveApplication> getPendingLeaveRequests(String managerEmail) {
        User user = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("Manager user not found"));
        Employee manager = employeeRepository.findByUser(user);

        // Fetch leave applications where the employee's manager is the logged-in user
        // and filter by PENDING status
        List<LeaveApplication> allRequests = leaveApplicationRepository.findByEmployee_Manager(manager);
        return allRequests.stream()
                .filter(leave -> "PENDING".equals(leave.getStatus()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveLeave(Long leaveId, String managerEmail) {
        LeaveApplication leave = leaveApplicationRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave application not found"));

        if (!"PENDING".equals(leave.getStatus())) {
            throw new RuntimeException("Leave is not in PENDING state");
        }

        User user = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("Manager user not found"));
        Employee manager = employeeRepository.findByUser(user);

        long leaveDays = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
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
        leave.setApprovedBy(manager);
        leaveApplicationRepository.save(leave);

        Notification notification = Notification.builder()
                .user(leave.getEmployee().getUser())
                .message("Your leave request for " + leave.getLeaveType().getLeaveName() + " has been approved.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void rejectLeave(Long leaveId, String managerComment, String managerEmail) {
        LeaveApplication leave = leaveApplicationRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave application not found"));

        if (!"PENDING".equals(leave.getStatus())) {
            throw new RuntimeException("Leave is not in PENDING state");
        }

        User user = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("Manager user not found"));
        Employee manager = employeeRepository.findByUser(user);

        leave.setStatus("REJECTED");
        leave.setApprovedBy(manager);
        leave.setManagerComment(managerComment);
        leaveApplicationRepository.save(leave);

        Notification notification = Notification.builder()
                .user(leave.getEmployee().getUser())
                .message("Your leave request for " + leave.getLeaveType().getLeaveName()
                        + " has been rejected. Comment: " + managerComment)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void applyLeave(LeaveApplication leaveApplication, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee employee = employeeRepository.findByUser(user);

        leaveApplication.setEmployee(employee);
        leaveApplication.setStatus("PENDING");
        leaveApplication.setAppliedOn(LocalDateTime.now());

        leaveApplicationRepository.save(leaveApplication);

        // Notify Manager/Admin
        Employee manager = employee.getManager();

        if (manager != null) {
            Notification notification = Notification.builder()
                    .user(manager.getUser())
                    .message("New leave request from " + employee.getFirstName())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }
    }

    public List<LeaveApplication> getMyLeaves(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Employee employee = employeeRepository.findByUser(user);
        return leaveApplicationRepository.findByEmployee(employee);
    }

    public List<LeaveBalance> getMyLeaveBalances(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Employee employee = employeeRepository.findByUser(user);
        return leaveBalanceRepository.findAll().stream()
                .filter(lb -> lb.getEmployee().getEmpId().equals(employee.getEmpId())).toList();
    }

    public List<Employee> getMyEmployees(String managerEmail) {
        User user = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("Manager user not found"));
        Employee manager = employeeRepository.findByUser(user);
        return employeeRepository.findByManager_EmpId(manager.getEmpId());
    }

    @Transactional
    public void assignGoal(Goal goal, Long employeeId, String managerEmail) {
        User user = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("Manager user not found"));
        Employee manager = employeeRepository.findByUser(user);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.getManager().getEmpId().equals(manager.getEmpId())) {
            throw new RuntimeException("Employee does not belong to this manager");
        }

        goal.setEmployee(employee);
        goal.setAssignedBy(manager);
        goal.setStatus("ASSIGNED");
        goal.setAssignedAt(LocalDateTime.now());
        goalRepository.save(goal);

        Notification notification = Notification.builder()
                .user(employee.getUser())
                .message("You have been assigned a new goal: " + goal.getTitle())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void createPerformanceReview(PerformanceReview review, Long employeeId, String managerEmail) {
        User user = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("Manager user not found"));
        Employee manager = employeeRepository.findByUser(user);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.getManager().getEmpId().equals(manager.getEmpId())) {
            throw new RuntimeException("Employee does not belong to this manager");
        }

        // optional: enforce unique constraint here or let DB handle it

        review.setEmployee(employee);
        review.setReviewedBy(manager);
        review.setCreatedAt(LocalDateTime.now());

        try {
            performanceReviewRepository.save(review);
        } catch (Exception e) {
            throw new RuntimeException("Review for this year already exists for this employee.");
        }

        Notification notification = Notification.builder()
                .user(employee.getUser())
                .message("Your Performance Review for year " + review.getYear() + " has been published.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }
}
