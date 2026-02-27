package com.revworkforce.repository;

import com.revworkforce.entity.Notification;
import com.revworkforce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {


    void deleteByUser(User user);
}