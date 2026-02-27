package com.revworkforce.controller;

import com.revworkforce.dto.CreateEmployeeRequest;
import com.revworkforce.repository.DepartmentRepository;
import com.revworkforce.repository.EmployeeRepository;
import com.revworkforce.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

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

}