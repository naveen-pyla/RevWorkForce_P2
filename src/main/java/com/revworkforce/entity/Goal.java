package com.revworkforce.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

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

    @Column(length = 1000)
    private String goalDesc;

    private LocalDate deadline;

    private String priority; // HIGH, MEDIUM, LOW

    private String successMetric;

    private Integer progress; // percentage

    private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED
}