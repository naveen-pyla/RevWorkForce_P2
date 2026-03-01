package com.revworkforce.service;

import com.revworkforce.entity.Employee;
import com.revworkforce.entity.LeaveApplication;
import com.revworkforce.entity.Notification;
import com.revworkforce.entity.User;
import com.revworkforce.repository.EmployeeRepository;
import com.revworkforce.repository.LeaveApplicationRepository;
import com.revworkforce.repository.NotificationRepository;
import com.revworkforce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void applyLeave(LeaveApplication leaveApplication, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee employee = employeeRepository.findByUser(user);

        leaveApplication.setEmployee(employee);
        leaveApplication.setStatus("PENDING");
        leaveApplication.setAppliedOn(LocalDateTime.now());

        leaveApplicationRepository.save(leaveApplication);

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
}
