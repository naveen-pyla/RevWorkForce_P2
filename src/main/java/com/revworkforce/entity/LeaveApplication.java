package com.revworkforce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaveId;

    @ManyToOne
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee;

    @NotNull(message = "Leave type is required")
    @ManyToOne
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @FutureOrPresent(message = "End date cannot be in the past")
    private LocalDate endDate;

    @NotBlank(message = "Reason for leave is required")
    @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
    @Column(length = 1000)
    private String reason;

    private String status; // PENDING, APPROVED, REJECTED

    private LocalDateTime appliedOn;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;

    @Column(length = 1000)
    private String managerComment;
}