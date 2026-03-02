package com.revworkforce.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.revworkforce.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final EventRepository eventRepository;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboardRedirect(Authentication authentication) {

        String role = authentication.getAuthorities().iterator().next().getAuthority();

        if (role.equals("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (role.equals("ROLE_MANAGER")) {
            return "redirect:/manager/dashboard";
        } else {
            return "redirect:/employee/dashboard";
        }
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        addCommonDashboardData(model);
        return "admin/dashboard";
    }

    @GetMapping("/manager/dashboard")
    public String managerDashboard(Model model) {
        addCommonDashboardData(model);
        return "manager/dashboard";
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard(Model model) {
        addCommonDashboardData(model);
        return "employee/dashboard";
    }

    private void addCommonDashboardData(Model model) {
        model.addAttribute("upcomingEvents",
                eventRepository.findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate.now()));
    }
}