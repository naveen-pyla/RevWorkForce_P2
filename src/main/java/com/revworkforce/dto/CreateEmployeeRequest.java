package com.revworkforce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateEmployeeRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Role is required")
    private String role; // ADMIN / MANAGER / EMPLOYEE

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotNull(message = "Salary is required")
    @Positive(message = "Salary must be positive")
    private Double salary;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Joining date is required")
    @PastOrPresent(message = "Joining date cannot be in the future")
    private LocalDate joiningDate;

    private Long managerId; // nullable
}