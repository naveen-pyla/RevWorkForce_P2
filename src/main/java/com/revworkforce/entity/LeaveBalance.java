package com.revworkforce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "leave_balance",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"emp_id", "leave_type_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long balanceId;

    @ManyToOne
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer balanceDays;
}