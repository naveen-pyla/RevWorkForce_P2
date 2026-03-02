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
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
    public String applyLeave(@Valid @ModelAttribute("leaveApplication") LeaveApplication leaveApplication,
            BindingResult result, Authentication authentication, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("leaveTypes", leaveTypeRepository.findAll());
            return "employee/apply-leave";
        }
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

    @GetMapping("/leave-balances")
    public String viewLeaveBalances(Model model, Authentication authentication) {
        String email = authentication.getName();
        model.addAttribute("balances", employeeService.getMyLeaveBalances(email));
        return "employee/leave-balances";
    }

    @GetMapping("/my-goals")
    public String viewMyGoals(Model model, Authentication authentication) {
        String email = authentication.getName();
        model.addAttribute("goals", employeeService.getMyGoals(email));
        return "employee/my-goals";
    }

    @PostMapping("/goals/complete")
    public String completeGoal(@RequestParam("goalId") Long goalId, Authentication authentication) {
        String email = authentication.getName();
        employeeService.completeGoal(goalId, email);
        return "redirect:/employee/my-goals";
    }

    @GetMapping("/my-reviews")
    public String viewMyReviews(Model model, Authentication authentication) {
        String email = authentication.getName();
        model.addAttribute("reviews", employeeService.getMyPerformanceReviews(email));
        return "employee/my-reviews";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication authentication) {
        String email = authentication.getName();
        model.addAttribute("employee", employeeService.getProfile(email));
        return "employee/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("phone") String phone,
            @RequestParam("address") String address,
            Authentication authentication) {
        String email = authentication.getName();
        employeeService.updateProfile(email, phone, address);
        return "redirect:/employee/profile";
    }
}
