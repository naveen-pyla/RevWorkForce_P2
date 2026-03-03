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
import com.revworkforce.repository.UserRepository;
import com.revworkforce.entity.PerformanceReview;
import com.revworkforce.repository.PerformanceReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final NotificationRepository notificationRepository;
    private final GoalRepository goalRepository;
    private final PerformanceReviewRepository performanceReviewRepository;

    @Transactional
    public void applyLeave(LeaveApplication leaveApplication, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee employee = employeeRepository.findByUser(user);

        leaveApplication.setEmployee(employee);
        leaveApplication.setStatus("PENDING");
        leaveApplication.setAppliedOn(LocalDateTime.now());

        leaveApplicationRepository.save(leaveApplication);
        log.info("Employee {} applied for leave. ID: {}", email, leaveApplication.getLeaveId());

        // Notify Manager
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
        // We'll need a method findByEmployee in LeaveBalanceRepository, let's assume it
        // exists or use findAll + stream or list. I'll check/add if needed... wait,
        // EmployeeService has access to leaveBalanceRepository. We need a method in
        // LeaveBalanceRepository. Let's add it there first instead, or use stream.
        // I checked LeaveBalanceRepository: it has void deleteByEmployee(Employee
        // employee); but not List<LeaveBalance> findByEmployee(Employee employee); I
        // should add that. Let's use stream on all for now or I can update repo. Wait,
        // I'll update Repo.
        return leaveBalanceRepository.findAll().stream()
                .filter(lb -> lb.getEmployee().getEmpId().equals(employee.getEmpId())).toList();
    }

    public List<Goal> getMyGoals(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Employee employee = employeeRepository.findByUser(user);
        return goalRepository.findByEmployee(employee);
    }

    @Transactional
    public void completeGoal(Long goalId, String email) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Employee employee = employeeRepository.findByUser(user);

        if (!goal.getEmployee().getEmpId().equals(employee.getEmpId())) {
            throw new RuntimeException("Not authorized to complete this goal");
        }

        goal.setStatus("COMPLETED");
        goalRepository.save(goal);
        log.info("Employee {} marked goal ID: {} as COMPLETED", email, goalId);

        if (employee.getManager() != null) {
            Notification notification = Notification.builder()
                    .user(employee.getManager().getUser())
                    .message(employee.getFirstName() + " has completed a goal: " + goal.getTitle())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
        }
    }

    public List<PerformanceReview> getMyPerformanceReviews(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Employee employee = employeeRepository.findByUser(user);
        return performanceReviewRepository.findByEmployee_EmpIdOrderByYearDesc(employee.getEmpId());
    }

    public Employee getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return employeeRepository.findByUser(user);
    }

    @Transactional
    public void updateProfile(String email, String phone, String address) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Employee employee = employeeRepository.findByUser(user);
        employee.setPhone(phone);
        employee.setAddress(address);
        employeeRepository.save(employee);
        log.info("Employee {} updated their profile contact information", email);
    }
}
