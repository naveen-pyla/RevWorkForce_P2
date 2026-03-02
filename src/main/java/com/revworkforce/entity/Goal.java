package com.revworkforce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;

    @ManyToOne
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee;

    @NotBlank(message = "Goal title is required")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "Due date is required")
    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;

    private LocalDateTime assignedAt;

    private String status; // ASSIGNED, COMPLETED

    @ManyToOne
    @JoinColumn(name = "assigned_by", nullable = false)
    private Employee assignedBy;
}