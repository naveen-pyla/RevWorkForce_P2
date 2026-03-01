package com.revworkforce.entity;

import jakarta.persistence.*;
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
public class PerformanceReview {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long reviewId;

        @ManyToOne
        @JoinColumn(name = "emp_id", nullable = false)
        private Employee employee;

        @ManyToOne
        @JoinColumn(name = "reviewed_by", nullable = false)
        private Employee reviewedBy;

        private Integer year;

        private Integer rating;

        @Column(length = 2000)
        private String comments;

        private java.time.LocalDateTime createdAt;
}