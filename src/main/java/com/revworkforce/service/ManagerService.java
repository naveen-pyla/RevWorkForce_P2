package com.revworkforce.service;

import com.revworkforce.entity.Employee;
import com.revworkforce.entity.LeaveApplication;
import com.revworkforce.entity.LeaveBalance;
import com.revworkforce.entity.Notification;
import com.revworkforce.entity.User;
import com.revworkforce.repository.EmployeeRepository;
import com.revworkforce.repository.LeaveApplicationRepository;
import com.revworkforce.repository.LeaveBalanceRepository;
import com.revworkforce.repository.NotificationRepository;
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
}
