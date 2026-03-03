package com.revworkforce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "performance_review", uniqueConstraints = {
                @UniqueConstraint(columnNames = { "emp_id", "year" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview extends AbstractAuditEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long reviewId;

        @ManyToOne
        @JoinColumn(name = "emp_id", nullable = false)
        private Employee employee;

        @ManyToOne
        @JoinColumn(name = "reviewed_by", nullable = false)
        private Employee reviewedBy;

        @NotNull(message = "Review year is required")
        @Min(value = 2020, message = "Year must be 2020 or later")
        @Max(value = 2100, message = "Year is invalid")
        private Integer year;

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5")
        private Integer rating;

        @NotBlank(message = "Comments are required")
        @Size(max = 2000, message = "Comments cannot exceed 2000 characters")
        @Column(length = 2000)
        private String comments;

        private java.time.LocalDateTime createdAt;
}