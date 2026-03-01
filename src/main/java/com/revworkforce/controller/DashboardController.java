package com.revworkforce.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.revworkforce.entity.User;
import com.revworkforce.repository.NotificationRepository;
import com.revworkforce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

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
    public String adminDashboard(Model model, Authentication auth) {
        addUnreadCount(model, auth);
        return "admin/dashboard";
    }

    @GetMapping("/manager/dashboard")
    public String managerDashboard(Model model, Authentication auth) {
        addUnreadCount(model, auth);
        return "manager/dashboard";
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard(Model model, Authentication auth) {
        addUnreadCount(model, auth);
        return "employee/dashboard";
    }

    private void addUnreadCount(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            userRepository.findByEmail(auth.getName()).ifPresent(user -> {
                long unreadCount = notificationRepository.countByUserAndIsReadFalse(user);
                model.addAttribute("unreadCount", unreadCount);
            });
        }
    }
}