package com.revworkforce.controller;

import com.revworkforce.entity.Employee;
import com.revworkforce.entity.LeaveApplication;
import com.revworkforce.entity.Notification;
import com.revworkforce.entity.User;
import com.revworkforce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;


    @GetMapping("/announcements")
    public String viewEmployeeAnnouncements(Model model) {

        model.addAttribute("announcements",
                announcementRepository.findAll());

        return "employee/announcements";
    }

    @GetMapping("/apply-leave")
    public String showLeaveForm(Model model) {

        model.addAttribute("leaveApplication", new LeaveApplication());
        model.addAttribute("leaveTypes", leaveTypeRepository.findAll());

        return "employee/apply-leave";
    }
    @PostMapping("/apply-leave")
    public String applyLeave(@ModelAttribute LeaveApplication leaveApplication,
                             Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        Employee employee = employeeRepository.findByUser(user);

        leaveApplication.setEmployee(employee);
        leaveApplication.setStatus("PENDING");
        leaveApplication.setAppliedOn(LocalDateTime.now());

        leaveApplicationRepository.save(leaveApplication);

        // 🔔 Notify Manager
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

        return "redirect:/employee/my-leaves";
    }
    @GetMapping("/my-leaves")
    public String viewMyLeaves(Model model,
                               Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        Employee employee = employeeRepository.findByUser(user);

        model.addAttribute("leaves",
                leaveApplicationRepository.findByEmployee(employee));

        return "employee/my-leaves";
    }
}
