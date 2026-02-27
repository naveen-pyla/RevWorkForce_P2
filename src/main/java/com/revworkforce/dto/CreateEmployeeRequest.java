package com.revworkforce.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateEmployeeRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String role; // ADMIN / MANAGER / EMPLOYEE
    private Long departmentId;
    private String designation;
    private Double salary;
    private String phone;
    private String address;
    private LocalDate joiningDate;
    private Long managerId; // nullable
}