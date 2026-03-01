package com.revworkforce.controller;

import com.revworkforce.entity.LeaveApplication;
import com.revworkforce.repository.*;
import com.revworkforce.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// unused imports removed
@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final AnnouncementRepository announcementRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeService employeeService;

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

        employeeService.applyLeave(leaveApplication, email);

        return "redirect:/employee/my-leaves";
    }

    @GetMapping("/my-leaves")
    public String viewMyLeaves(Model model,
            Authentication authentication) {

        String email = authentication.getName();

        model.addAttribute("leaves", employeeService.getMyLeaves(email));

        return "employee/my-leaves";
    }
}
