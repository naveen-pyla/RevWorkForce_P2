package com.revworkforce.controller;

import com.revworkforce.repository.AnnouncementRepository;
import com.revworkforce.service.ManagerService;
import com.revworkforce.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.revworkforce.entity.Goal;
import com.revworkforce.entity.LeaveApplication;
import com.revworkforce.entity.PerformanceReview;
import com.revworkforce.repository.LeaveTypeRepository;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {
    private final AnnouncementRepository announcementRepository;
    private final ManagerService managerService;
    private final EmployeeService employeeService;
    private final LeaveTypeRepository leaveTypeRepository;

    @GetMapping("/announcements")
    public String viewEmployeeAnnouncements(Model model) {

        model.addAttribute("announcements",
                announcementRepository.findAll());

        return "manager/announcements";
    }

    @GetMapping("/leave-requests")
    public String viewLeaveRequests(Model model, Authentication authentication) {
        String email = authentication.getName();
        model.addAttribute("leaves", managerService.getPendingLeaveRequests(email));
        return "manager/leave-requests";
    }

    @PostMapping("/leave/approve")
    public String approveLeave(@RequestParam("leaveId") Long leaveId, Authentication authentication) {
        String email = authentication.getName();
        try {
            managerService.approveLeave(leaveId, email);
        } catch (RuntimeException e) {
            // In a real app we might pass an error message to the view
        }
        return "redirect:/manager/leave-requests";
    }

    @PostMapping("/leave/reject")
    public String rejectLeave(@RequestParam("leaveId") Long leaveId,
            @RequestParam(name = "managerComment", required = false) String managerComment,
            Authentication authentication) {
        String email = authentication.getName();
        try {
            managerService.rejectLeave(leaveId, managerComment, email);
        } catch (RuntimeException e) {
            // Fallback
        }
        return "redirect:/manager/leave-requests";
    }

    @GetMapping("/apply-leave")
    public String showLeaveForm(Model model) {
        model.addAttribute("leaveApplication", new LeaveApplication());
        model.addAttribute("leaveTypes", leaveTypeRepository.findAll());
        return "manager/apply-leave";
    }

    @PostMapping("/apply-leave")
    public String applyLeave(@ModelAttribute LeaveApplication leaveApplication, Authentication authentication) {
        String email = authentication.getName();
        managerService.applyLeave(leaveApplication, email);
        return "redirect:/manager/my-leaves";
    }

    @GetMapping("/my-leaves")
    public String viewMyLeaves(Model model, Authentication authentication) {
        String email = authentication.getName();
        model.addAttribute("leaves", managerService.getMyLeaves(email));
        return "manager/my-leaves";
    }

    @GetMapping("/leave-balances")
    public String viewLeaveBalances(Model model, Authentication authentication) {
        String email = authentication.getName();
        model.addAttribute("balances", managerService.getMyLeaveBalances(email));
        return "manager/leave-balances";
    }

    @GetMapping("/assign-goal")
    public String showAssignGoalForm(Model model, Authentication authentication) {
        String email = authentication.getName();
        model.addAttribute("employees", managerService.getMyEmployees(email));
        model.addAttribute("goal", new Goal());
        return "manager/assign-goal";
    }

    @PostMapping("/assign-goal")
    public String assignGoal(@ModelAttribute Goal goal, @RequestParam("employeeId") Long employeeId,
            Authentication authentication) {
        String email = authentication.getName();
        managerService.assignGoal(goal, employeeId, email);
        return "redirect:/manager/dashboard";
    }

    @GetMapping("/write-review")
    public String showWriteReviewForm(Model model, Authentication authentication) {
        String email = authentication.getName();
        model.addAttribute("employees", managerService.getMyEmployees(email));
        model.addAttribute("review", new PerformanceReview());
        return "manager/write-review";
    }

    @PostMapping("/write-review")
    public String writeReview(@ModelAttribute PerformanceReview review, @RequestParam("employeeId") Long employeeId,
            Authentication authentication, Model model) {
        String email = authentication.getName();
        try {
            managerService.createPerformanceReview(review, employeeId, email);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("employees", managerService.getMyEmployees(email));
            return "manager/write-review";
        }
        return "redirect:/manager/dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication authentication) {
        String email = authentication.getName();
        model.addAttribute("employee", employeeService.getProfile(email));
        return "manager/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("phone") String phone,
            @RequestParam("address") String address,
            Authentication authentication) {
        String email = authentication.getName();
        employeeService.updateProfile(email, phone, address);
        return "redirect:/manager/profile";
    }
}
