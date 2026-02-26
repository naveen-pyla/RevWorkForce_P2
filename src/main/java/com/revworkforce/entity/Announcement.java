package com.revworkforce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long announcementId;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String message;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private Employee createdBy;  // Admin employee

    private LocalDateTime createdAt;
}