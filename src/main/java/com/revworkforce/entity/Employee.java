package com.revworkforce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long empId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String emergencyContact;

    private LocalDate dob;
    private LocalDate joiningDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DEPARTMENT_ID")
    private Department department;

    private String designation; // Keeping varchar as decided

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;

    private Double salary;

    private String status; // ACTIVE / INACTIVE
}