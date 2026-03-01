package com.revworkforce.repository;

import com.revworkforce.entity.Notification;
import com.revworkforce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    long countByUserAndIsReadFalse(User user);

    void deleteByUser(User user);
}