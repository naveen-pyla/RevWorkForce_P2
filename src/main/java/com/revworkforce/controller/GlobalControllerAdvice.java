package com.revworkforce.controller;

import com.revworkforce.entity.Employee;
import com.revworkforce.repository.EmployeeRepository;
import com.revworkforce.repository.NotificationRepository;
import com.revworkforce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                String email = auth.getName();
                userRepository.findByEmail(email).ifPresent(user -> {
                    // Safely add unread count
                    try {
                        long unreadCount = notificationRepository.countByUserAndIsReadFalse(user);
                        model.addAttribute("unreadCount", unreadCount);
                    } catch (Exception e) {
                    }

                    // Safely add user first name for global welcome/sidebar
                    try {
                        Employee employee = employeeRepository.findByUser(user);
                        if (employee != null) {
                            model.addAttribute("userFirstName", employee.getFirstName());
                        } else {
                            model.addAttribute("userFirstName", user.getEmail());
                        }
                    } catch (Exception e) {
                        model.addAttribute("userFirstName", user.getEmail());
                    }
                });
            }
        } catch (Exception e) {
            // Silently fail to not break the page rendering if security context is weird
        }
    }
}
