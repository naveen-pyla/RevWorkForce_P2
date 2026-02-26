package com.revworkforce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "performance_review",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"emp_id", "year"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee;

    private Integer year;

    @Column(length = 2000)
    private String achievements;

    @Column(length = 2000)
    private String improvements;

    private Integer selfRating;

    private Integer managerRating;

    @Column(length = 2000)
    private String managerFeedback;

    private String status; // SUBMITTED, REVIEWED
}