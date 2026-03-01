package com.revworkforce.controller;

import com.revworkforce.dto.CreateEmployeeRequest;
import com.revworkforce.entity.Announcement;
import com.revworkforce.entity.Employee;
import com.revworkforce.repository.AnnouncementRepository;
import com.revworkforce.repository.DepartmentRepository;
import com.revworkforce.repository.EmployeeRepository;
import com.revworkforce.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// unused import removed
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AnnouncementRepository announcementRepository;

    @GetMapping("/create-employee")
    public String showCreateEmployee(Model model) {

        model.addAttribute("employeeRequest", new CreateEmployeeRequest());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("managers", employeeRepository.findAll());

        return "admin/create-employee";
    }

    @PostMapping("/create-employee")
    public String createEmployee(@ModelAttribute CreateEmployeeRequest request) {

        adminService.createEmployee(request);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/employees")
    public String listEmployees(Model model) {

        model.addAttribute("employees", employeeRepository.findAll());
        return "admin/employee-list";
    }

    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {

        adminService.deleteEmployee(id);

        return "redirect:/admin/employees";
    }

    @GetMapping("/manager/{id}")
    public String showManagerForm(@PathVariable Long id, Model model) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<Employee> possibleManagers;

        if (employee.getUser().getRole().equals("EMPLOYEE")) {
            possibleManagers = employeeRepository.findAllManagers();
        } else if (employee.getUser().getRole().equals("MANAGER")) {
            possibleManagers = employeeRepository.findAllAdmins();
        } else {
            possibleManagers = List.of();
        }

        model.addAttribute("employee", employee);
        model.addAttribute("managers", possibleManagers);

        return "admin/assign-manager";
    }

    @PostMapping("/manager/update")
    public String updateManager(@RequestParam Long empId,
            @RequestParam Long managerId) {

        adminService.updateEmployeeManager(empId, managerId);

        return "redirect:/admin/employees";
    }

    @GetMapping("/announcements")
    public String viewAnnouncements(Model model) {

        model.addAttribute("announcements",
                announcementRepository.findAll());

        return "admin/announcements";
    }

    @GetMapping("/announcements/create")
    public String showCreateAnnouncementForm(Model model) {

        model.addAttribute("announcement", new Announcement());

        return "admin/create-announcement";
    }

    @PostMapping("/announcements/save")
    public String saveAnnouncement(@ModelAttribute Announcement announcement,
            Authentication authentication) {

        // Logged-in user email
        String email = authentication.getName();

        adminService.saveAnnouncement(announcement, email);

        return "redirect:/admin/announcements";
    }

    @GetMapping("/announcements/delete/{id}")
    public String deleteAnnouncement(@PathVariable Long id) {

        announcementRepository.deleteById(id);

        return "redirect:/admin/announcements";
    }

    @GetMapping("/leave-requests")
    public String viewAllLeaveRequests(Model model) {
        model.addAttribute("leaves", adminService.getAllPendingLeaves());
        return "admin/leave-requests";
    }

    @PostMapping("/leave/approve")
    public String approveLeave(@RequestParam Long leaveId, Authentication authentication) {
        try {
            adminService.approveLeaveAdmin(leaveId, authentication.getName());
        } catch (RuntimeException e) {
            // Error handling
        }
        return "redirect:/admin/leave-requests";
    }

    @PostMapping("/leave/reject")
    public String rejectLeave(@RequestParam Long leaveId,
            @RequestParam(required = false) String managerComment,
            Authentication authentication) {
        try {
            adminService.rejectLeaveAdmin(leaveId, managerComment, authentication.getName());
        } catch (RuntimeException e) {
            // Error handling
        }
        return "redirect:/admin/leave-requests";
    }

}