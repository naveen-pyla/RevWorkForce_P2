package com.revworkforce.repository;

import com.revworkforce.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_UserIdAndIsReadFalse(Long userId);
}