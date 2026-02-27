package com.revworkforce.controller;

import com.revworkforce.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final AnnouncementRepository announcementRepository;

    @GetMapping("/announcements")
    public String viewEmployeeAnnouncements(Model model) {

        model.addAttribute("announcements",
                announcementRepository.findAll());

        return "employee/announcements";
    }
}
