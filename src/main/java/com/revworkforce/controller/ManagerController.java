package com.revworkforce.controller;

import com.revworkforce.repository.AnnouncementRepository;
import com.revworkforce.service.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.revworkforce.entity.LeaveApplication;
import com.revworkforce.repository.LeaveTypeRepository;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {
    private final AnnouncementRepository announcementRepository;
    private final ManagerService managerService;
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
    public String approveLeave(@RequestParam Long leaveId, Authentication authentication) {
        String email = authentication.getName();
        try {
            managerService.approveLeave(leaveId, email);
        } catch (RuntimeException e) {
            // In a real app we might pass an error message to the view
        }
        return "redirect:/manager/leave-requests";
    }

    @PostMapping("/leave/reject")
    public String rejectLeave(@RequestParam Long leaveId,
            @RequestParam(required = false) String managerComment,
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
}
